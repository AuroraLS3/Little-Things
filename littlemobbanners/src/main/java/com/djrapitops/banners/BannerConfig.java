package com.djrapitops.banners;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Class for getting Effects out of config file.
 *
 * @author AuroraLS3
 */
public class BannerConfig {

    private final FileConfiguration config;

    public BannerConfig(FileConfiguration config) {
        this.config = config;
    }

    public Integer getRequiredKills(EntityType entityType) {
        return config.getInt("Reward_banner_every_x_kill." + entityType);
    }

    public Optional<BannerAward> getAward(EntityType entityType) {
        Integer requiredKills = getRequiredKills(entityType);
        String bannerTag = getBannerTag(entityType);
        String readableName = getReadableEntityName(entityType);
        String extraCommand = getExtraCommand(entityType);

        if (requiredKills <= 0 || bannerTag == null) {
            return Optional.empty();
        }

        return Optional.of(new BannerAward(requiredKills, bannerTag, readableName, extraCommand));
    }

    private String getBannerTag(EntityType entityType) {
        return config.getString("Banner_patterns." + entityType.name());
    }

    public boolean arePublicMessagesEnabled() {
        return config.getBoolean("Announce_banner_rewards", false);
    }

    public String getRewardMessage(String playerName, int killCount, EntityType entityType) {
        return ChatColor.translateAlternateColorCodes('&',
                config.getString("Reward_message", "&2%player% has defeated their %n%th %mob%!")
                        .replace("%player%", playerName)
                        .replace("%n%", Integer.toString(killCount))
                        .replace("%mob%", getReadableEntityName(entityType))
        );
    }

    public String getReadableEntityName(EntityType entityType) {
        String originalName = entityType.name();
        String name = originalName.toLowerCase().replace('_', ' ');
        return originalName.charAt(0) + name.substring(1);
    }

    public String getExtraCommand(EntityType entityType) {
        return config.getString("Run_console_command_on_award." + entityType.name());
    }

    public List<String> getWarnings() {
        List<String> warnings = new ArrayList<>();
        Set<String> killEntities = config.getConfigurationSection("Reward_banner_every_x_kill").getKeys(false);
        Set<String> bannerPatternEntities = config.getConfigurationSection("Banner_patterns").getKeys(false);
        for (String entityName : killEntities) {
            try {
                EntityType.valueOf(entityName);
            } catch (NoSuchFieldError | Exception e) {
                warnings.add("Config: Reward_banner_every_x_kill." + entityName + " - entity type '" + entityName + "' not supported by this version.");
            }
        }
        for (String entityName : bannerPatternEntities) {
            try {
                EntityType.valueOf(entityName);
            } catch (NoSuchFieldError | Exception e) {
                warnings.add("Config: Banner_patterns." + entityName + " - entity type '" + entityName + "' not supported by this version.");
            }
        }

        for (String killEntity : killEntities) {
            if (!bannerPatternEntities.contains(killEntity)) {
                warnings.add("Config: Reward_banner_every_x_kill." + killEntity + " exists, but Banner_patterns." + killEntity + " does not.");
            }
        }
        for (String bannerEntity : bannerPatternEntities) {
            if (!killEntities.contains(bannerEntity)) {
                warnings.add("Config: Banner_patterns." + bannerEntity + " exists, but Reward_banner_every_x_kill." + bannerEntity + " does not.");
            }
        }

        return warnings;
    }
}