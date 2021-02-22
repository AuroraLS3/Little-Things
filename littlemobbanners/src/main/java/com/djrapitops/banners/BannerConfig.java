package com.djrapitops.banners;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

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

    public BannerAward getAward(EntityType entityType, BannerTags tagType) {
        return new BannerAward(
                getRequiredKills(entityType),
                tagType.getTag(),
                getReadableName(entityType)
        );
    }

    public boolean arePublicMessagesEnabled() {
        return config.getBoolean("Announce_banner_rewards");
    }

    public String getRewardMessage(String playerName, int killCount, EntityType entityType) {
        return ChatColor.translateAlternateColorCodes('&',
                config.getString("Reward_message")
                        .replace("%player%", playerName)
                        .replace("%n%", Integer.toString(killCount))
                        .replace("%mob%", getReadableName(entityType))
        );
    }

    private String getReadableName(EntityType entityType) {
        String originalName = entityType.name();
        String name = originalName.toLowerCase().replace('_', ' ');
        return originalName.charAt(0) + name.substring(1);
    }
}