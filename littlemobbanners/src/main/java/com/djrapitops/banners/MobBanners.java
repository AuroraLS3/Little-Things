package com.djrapitops.banners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main JavaPlugin class.
 *
 * @author AuroraLS3
 */
public class MobBanners extends JavaPlugin implements Listener {

    private final Map<UUID, KillCounter> killCounters = new HashMap<>();
    private final Map<EntityType, BannerAward> awards = new EnumMap<>(EntityType.class);
    private Logger logger;
    private BannerConfig config;
    private KillCounterStorage killCounterStorage;

    @Override
    public void onEnable() {
        logger = getLogger();
        config = new BannerConfig(getConfig());
        try {
            killCounterStorage = new KillCounterStorage(getDataFolder());
        } catch (IOException e) {
            logger.severe("Failed to enable MobBanners, could not load storage: " + e.getMessage());
            onDisable();
            return;
        }

        reloadAwards();

        getServer().getPluginManager().registerEvents(this, this);

        getCommand("banners").setExecutor(this);

        logger.log(Level.INFO, "Enabled MobBanners.");
    }

    private NamespacedKey getKey(String key) {
        return new NamespacedKey(this, key);
    }

    private void clearAwards() {
        awards.clear();
    }

    private Optional<NamespacedKey> getKey(Recipe recipe) {
        if (recipe instanceof Keyed) {
            return Optional.of(((Keyed) recipe).getKey());
        }
        return Optional.empty();
    }

    private void reloadAwards() {
        saveDefaultConfig();
        reloadConfig();
        clearAwards();
        loadAwards();
    }

    private void loadAwards() {
        addAward(config, EntityType.BAT, BannerTags.BAT);
        addAward(config, EntityType.BEE, BannerTags.BEE);
        addAward(config, EntityType.BLAZE, BannerTags.BLAZE);
        addAward(config, EntityType.CAT, BannerTags.CAT);
        addAward(config, EntityType.CAVE_SPIDER, BannerTags.CAVE_SPIDER);
        addAward(config, EntityType.CHICKEN, BannerTags.CHICKEN);
        addAward(config, EntityType.COD, BannerTags.COD);
        addAward(config, EntityType.COW, BannerTags.COW);
        addAward(config, EntityType.CREEPER, BannerTags.CREEPER);
        addAward(config, EntityType.DOLPHIN, BannerTags.DOLPHIN);
        addAward(config, EntityType.DONKEY, BannerTags.DONKEY);
        addAward(config, EntityType.DROWNED, BannerTags.DROWNED);
        addAward(config, EntityType.ELDER_GUARDIAN, BannerTags.ELDER_GUARDIAN);
        addAward(config, EntityType.ENDER_DRAGON, BannerTags.ENDER_DRAGON);
        addAward(config, EntityType.ENDERMAN, BannerTags.ENDERMAN);
        addAward(config, EntityType.EVOKER, BannerTags.EVOKER);
        addAward(config, EntityType.FOX, BannerTags.FOX);
        addAward(config, EntityType.GHAST, BannerTags.GHAST);
        addAward(config, EntityType.GUARDIAN, BannerTags.GUARDIAN);
        addAward(config, EntityType.HOGLIN, BannerTags.HOGLIN);
        addAward(config, EntityType.HORSE, BannerTags.HORSE);
        addAward(config, EntityType.HUSK, BannerTags.HUSK);
        addAward(config, EntityType.ILLUSIONER, BannerTags.ILLUSIONER);
        addAward(config, EntityType.IRON_GOLEM, BannerTags.IRON_GOLEM);
        addAward(config, EntityType.LLAMA, BannerTags.LLAMA);
        addAward(config, EntityType.MAGMA_CUBE, BannerTags.MAGMA_CUBE);
        addAward(config, EntityType.MULE, BannerTags.MULE);
        addAward(config, EntityType.MUSHROOM_COW, BannerTags.MUSHROOM_COW);
        addAward(config, EntityType.OCELOT, BannerTags.OCELOT);
        addAward(config, EntityType.PANDA, BannerTags.PANDA);
        addAward(config, EntityType.PARROT, BannerTags.PARROT);
        addAward(config, EntityType.PHANTOM, BannerTags.PHANTOM);
        addAward(config, EntityType.PIG, BannerTags.PIG);
        addAward(config, EntityType.PIGLIN, BannerTags.PIGLIN);
        addAward(config, EntityType.PIGLIN_BRUTE, BannerTags.PIGLIN);
        addAward(config, EntityType.POLAR_BEAR, BannerTags.POLAR_BEAR);
        addAward(config, EntityType.PUFFERFISH, BannerTags.PUFFERFISH);
        addAward(config, EntityType.RABBIT, BannerTags.RABBIT);
        addAward(config, EntityType.RAVAGER, BannerTags.RAVAGER);
        addAward(config, EntityType.SALMON, BannerTags.SALMON);
        addAward(config, EntityType.SHEEP, BannerTags.SHEEP);
        addAward(config, EntityType.SHULKER, BannerTags.SHULKER);
        addAward(config, EntityType.SILVERFISH, BannerTags.SILVERFISH);
        addAward(config, EntityType.SLIME, BannerTags.SLIME);
        addAward(config, EntityType.SNOWMAN, BannerTags.SNOWMAN);
        addAward(config, EntityType.SPIDER, BannerTags.SPIDER);
        addAward(config, EntityType.SQUID, BannerTags.SQUID);
        addAward(config, EntityType.STRAY, BannerTags.STRAY);
        addAward(config, EntityType.STRIDER, BannerTags.STRIDER);
        addAward(config, EntityType.TURTLE, BannerTags.TURTLE);
        addAward(config, EntityType.TROPICAL_FISH, BannerTags.TROPICAL_FISH);
        addAward(config, EntityType.VEX, BannerTags.VEX);
        addAward(config, EntityType.WITCH, BannerTags.WITCH);
        addAward(config, EntityType.WITHER, BannerTags.WITHER);
        addAward(config, EntityType.WITHER_SKELETON, BannerTags.WITHER_SKELETON);
        addAward(config, EntityType.ZOGLIN, BannerTags.ZOGLIN);
        addAward(config, EntityType.ZOMBIE, BannerTags.ZOMBIE);

        logger.log(Level.INFO, "Loaded " + awards.size() + " banners.");
    }

    private void addAward(BannerConfig config, EntityType entityType, BannerTags tagType) {
        BannerAward award = config.getAward(entityType, tagType);
        if (award != null) awards.put(entityType, award);
    }

    @Override
    public void onDisable() {
        if (!killCounters.isEmpty()) {
            logger.log(Level.INFO, "Storing unsaved kill counts..");
            for (UUID playerUUID : killCounters.keySet()) {
                saveKillCounter(playerUUID);
            }
            logger.log(Level.INFO, "Complete!");
        }

        HandlerList.unregisterAll((Plugin) this);
        logger.log(Level.INFO, "Disabled MobBanners.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banners.reload")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission for this command!");
        }
        if (args.length == 0) {
            for (BannerAward value : awards.values()) {
                value.award(sender.getName());
            }
            return true;
        }
        if (args.length != 0 && args[0].equals("reload")) {
            reloadAwards();
            sender.sendMessage(ChatColor.GREEN + "Loaded " + awards.size() + " banners.");
        } else {
            sender.sendMessage(new String[]{"> " + ChatColor.GRAY + "MobBanners Help:",
                    "",
                    ChatColor.GRAY + "  /banners reload " + ChatColor.WHITE + "Reloads awards from config.",
                    "",
                    ">"
            });
        }
        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMobKill(EntityDeathEvent event) {
        LivingEntity dead = event.getEntity();
        EntityDamageEvent entityDamageEvent = dead.getLastDamageCause();
        if (!(entityDamageEvent instanceof EntityDamageByEntityEvent)) {
            return;
        }

        EntityType killedType = dead.getType();
        if (killedType == EntityType.ZOMBIE_VILLAGER) killedType = EntityType.ZOMBIE;

        EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) entityDamageEvent;
        Entity killerEntity = entityDamageByEntityEvent.getDamager();

        if (!(killerEntity instanceof Player)) {
            return;
        }
        UUID playerUUID = killerEntity.getUniqueId();
        String playerName = killerEntity.getName();

        KillCounter killCounter = killCounters.get(playerUUID);
        if (killCounter == null) {
            killCounter = loadKillCounter(playerUUID);
            killCounters.put(playerUUID, killCounter);
        }

        int totalKilled = killCounter.killed(killedType);
        BannerAward award = awards.get(killedType);
        if (award == null) return;

        if (!award.shouldAward(totalKilled)) return;

        award.award(playerName);

        String rewardMessage = config.getRewardMessage(playerName, totalKilled, killedType);
        if (config.arePublicMessagesEnabled()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(rewardMessage);
            }
            Bukkit.getConsoleSender().sendMessage(rewardMessage);
        } else {
            killerEntity.sendMessage(rewardMessage);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        saveKillCounter(playerUUID);
    }

    private void saveKillCounter(UUID playerUUID) {
        KillCounter killCounter = killCounters.get(playerUUID);
        try {
            killCounterStorage.save(playerUUID, killCounter);
        } catch (IOException e) {
            logger.severe("Failed to save kills to plugins/MobBanners/storage/" + playerUUID + ".yml, " + e.getMessage());
        }
        killCounters.remove(playerUUID);
    }

    private KillCounter loadKillCounter(UUID playerUUID) {
        try {
            return killCounterStorage.load(logger, playerUUID);
        } catch (IOException | InvalidConfigurationException e) {
            logger.severe("Failed to load kills from plugins/MobBanners/storage/" + playerUUID + ".yml (Malformatted file), " + e.getMessage());
            backupBrokenFile(playerUUID);
        }
        return new KillCounter(playerUUID);
    }

    private void backupBrokenFile(UUID playerUUID) {
        Path playerFile = killCounterStorage.getPlayerFile(playerUUID).toPath();
        Path backup = playerFile.resolveSibling(playerUUID.toString() + "-backup.yml");
        try {
            Files.copy(playerFile, backup);
            logger.severe("Created backup of the broken file to " + backup.toFile().getAbsolutePath());
        } catch (IOException ioException) {
            logger.severe("Failed backup plugins/MobBanners/storage/" + playerUUID + ".yml, " + ioException.getMessage());
        }
    }
}