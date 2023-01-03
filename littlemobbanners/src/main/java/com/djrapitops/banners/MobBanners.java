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
        config = new BannerConfig(getConfig());
        clearAwards();
        loadAwards();
    }

    private void loadAwards() {
        for (EntityType entityType : EntityType.values()) {
            addAward(config, entityType);
        }

        for (String warning : config.getWarnings()) {
            logger.log(Level.WARNING, warning);
        }

        logger.log(Level.INFO, "Loaded " + awards.size() + " banners.");
    }

    private void addAward(BannerConfig config, EntityType entityType) {
        config.getAward(entityType)
                .ifPresent(award -> awards.put(entityType, award));
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

        if (!award.award(playerName)) {
            killerEntity.sendMessage("§c[MobBanners] Banner could not be awarded, make sure the server is version 1.16+");
            return;
        }

        final EntityType finalKilledType = killedType;
        award.getExtraCommand().ifPresent(extraCommand ->
                runExtraCommand(finalKilledType, playerName, totalKilled, extraCommand));

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

    private void runExtraCommand(EntityType killedType, String playerName, int totalKilled, String extraCommand) {
        String withoutSlash = extraCommand.startsWith("/") ? extraCommand.substring(1) : extraCommand;
        String placeholdersReplaced = withoutSlash
                .replace("%player%", playerName)
                .replace("%n%", Integer.toString(totalKilled))
                .replace("%mob%", config.getReadableEntityName(killedType));
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), placeholdersReplaced);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        saveKillCounter(playerUUID);
    }

    private void saveKillCounter(UUID playerUUID) {
        KillCounter killCounter = killCounters.get(playerUUID);
        if (killCounter == null) return;
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
        Path backup = playerFile.resolveSibling(playerUUID + "-backup.yml");
        try {
            Files.copy(playerFile, backup);
            logger.severe("Created backup of the broken file to " + backup.toFile().getAbsolutePath());
        } catch (IOException ioException) {
            logger.severe("Failed backup plugins/MobBanners/storage/" + playerUUID + ".yml, " + ioException.getMessage());
        }
    }
}