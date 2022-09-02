package com.djrapitops.littlefx;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Main JavaPlugin class.
 *
 * @author AuroraLS3
 */
public class LittleFX extends JavaPlugin implements Listener {

    private Logger logger;
    private List<Effect> effects;
    private List<Effect> mobEffects;

    @Override
    public void onEnable() {
        logger = getLogger();

        reloadEffects();

        getServer().getPluginManager().registerEvents(this, this);

        getCommand("littlefx").setExecutor(this);

        logger.log(Level.INFO, "Enabled LittleFX.");
    }

    private void reloadEffects() {
        saveDefaultConfig();
        reloadConfig();
        effects = new FXConfig(logger, getConfig()).loadEffects();
        mobEffects = effects.stream().filter(Effect::appliesToMobs).collect(Collectors.toList());
        logger.log(Level.INFO, "Loaded " + effects.size() + " effects.");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Plugin) this);
        logger.log(Level.INFO, "Disabled LittleFX.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("littlefx.reload")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission for this command!");
        }
        if (args.length != 0 && args[0].equals("reload")) {
            reloadEffects();
            sender.sendMessage(ChatColor.GREEN + "Loaded " + effects.size() + " effects.");
        } else {
            sender.sendMessage(new String[]{"> " + ChatColor.GRAY + "LittleFX Help:",
                    "",
                    ChatColor.GRAY + "  /littlefx reload " + ChatColor.WHITE + "Reloads effects from config.",
                    "",
                    ">"
            });
        }
        return true;
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        for (Effect effect : effects) {
            if (effect.shouldApplyToPlayer(player)) {
                effect.apply(player);
            }
        }
    }

    public void onMobMoveEvent(EntityEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player || !(entity instanceof LivingEntity)) return;

        LivingEntity living = (LivingEntity) entity;
        for (Effect effect : mobEffects) {
            if (effect.shouldApplyToMob(living)) {
                effect.apply(living);
            }
        }
    }

    @EventHandler
    public void event(AreaEffectCloudApplyEvent event) {
        onMobMoveEvent(event);
    }

    @EventHandler
    public void event(EntityChangeBlockEvent event) {
        onMobMoveEvent(event);
    }

    @EventHandler
    public void event(EntityCombustEvent event) {
        onMobMoveEvent(event);
    }

    @EventHandler
    public void event(EntityDamageEvent event) {
        onMobMoveEvent(event);
    }

    @EventHandler
    public void event(EntityInteractEvent event) {
        onMobMoveEvent(event);
    }

    @EventHandler
    public void event(EntityRegainHealthEvent event) {
        onMobMoveEvent(event);
    }

    @EventHandler
    public void event(EntityShootBowEvent event) {
        onMobMoveEvent(event);
    }

    @EventHandler
    public void event(EntityTargetEvent event) {
        onMobMoveEvent(event);
    }

    @EventHandler
    public void event(HorseJumpEvent event) {
        onMobMoveEvent(event);
    }

    @EventHandler
    public void event(SheepRegrowWoolEvent event) {
        onMobMoveEvent(event);
    }

    @EventHandler
    public void event(SlimeSplitEvent event) {
        onMobMoveEvent(event);
    }
}