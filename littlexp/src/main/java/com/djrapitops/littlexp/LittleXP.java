package com.djrapitops.littlexp;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main JavaPlugin class.
 *
 * @author Rsl1122
 */
public class LittleXP extends JavaPlugin implements Listener {

    private Logger logger;
    private Collection<Recipe> recipes = Collections.emptyList();

    private Map<Material, Double> craftingXpAmounts;
    private Map<Material, Double> farmingXpAmounts;
    private Map<Material, Double> toolBreakingXpAmounts;
    private Double advancementsXpAmount;

    @Override
    public void onEnable() {
        logger = getLogger();

        reload();

        getServer().getPluginManager().registerEvents(this, this);

        getCommand("littlexp").setExecutor(this);

        logger.log(Level.INFO, "Enabled LittleXP.");
    }

    private void reload() {
        saveDefaultConfig();
        reloadConfig();
        loadXpAmounts();
    }

    private void loadXpAmounts() {
        XPConfig xpConfig = new XPConfig(logger, getConfig());
        craftingXpAmounts = xpConfig.getCraftingXpAmounts();
        farmingXpAmounts = xpConfig.getFarmingXpAmounts();
        toolBreakingXpAmounts = xpConfig.getToolBreakingXpAmounts();
        advancementsXpAmount = xpConfig.getAdvancementsXpAmount();
        logger.info("Loaded " + craftingXpAmounts.size() + " materials giving xp for crafting");
        logger.info("Loaded " + farmingXpAmounts.size() + " blocks giving xp for farming");
        logger.info("Loaded " + toolBreakingXpAmounts.size() + " tools giving xp for breaking");
        logger.info("Advancement xp: " + (advancementsXpAmount != null ? advancementsXpAmount : 0));
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Plugin) this);
        logger.log(Level.INFO, "Disabled LittleXP.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("littlexp.reload")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission for this command!");
        }
        if (args.length != 0 && args[0].equals("reload")) {
            reload();
            sender.sendMessage(ChatColor.GREEN + "Loaded " + recipes.size() + " recipes.");
        } else {
            sender.sendMessage(new String[]{"> " + ChatColor.GRAY + "LittleXP Help:",
                    "",
                    ChatColor.GRAY + "  /littlexp reload " + ChatColor.WHITE + "Reloads recipes from config.",
                    "",
                    ">"
            });
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Material type = block.getType();
        Double xp = farmingXpAmounts.get(type);
        if (xp == null) return;

        BlockData blockData = block.getBlockData();
        if (blockData instanceof Ageable) {
            Ageable ageable = (Ageable) blockData;
            if (ageable.getAge() < ageable.getMaximumAge()) return;
        }

        int discreteXp = roll(xp);
        if (discreteXp == 0) return;

        event.setExpToDrop(discreteXp);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        ItemStack result = event.getRecipe().getResult();
        Material type = result.getType();
        Double xp = craftingXpAmounts.get(type);
        if (xp == null) return;

        int amount = result.getAmount();
        int discreteXp = roll(xp * amount);

        Location location = event.getInventory().getLocation();
        if (location == null) return;
        dropXp(discreteXp, location);
    }

    @EventHandler(ignoreCancelled = true)
    public void onToolBreak(PlayerItemBreakEvent event) {
        Material type = event.getBrokenItem().getType();
        Double xp = toolBreakingXpAmounts.get(type);
        if (xp == null) return;

        int discreteXp = roll(xp);

        Location location = event.getPlayer().getLocation();
        dropXp(discreteXp, location);
    }

    @EventHandler(ignoreCancelled = true)
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        if (advancementsXpAmount == null) return;
        int discreteXp = roll(advancementsXpAmount);

        Location location = event.getPlayer().getLocation();
        dropXp(discreteXp, location);
    }

    public int roll(double xp) {
        boolean succeeds = ThreadLocalRandom.current().nextDouble() < xp;
        return (int) (succeeds ? Math.ceil(xp) : Math.floor(xp));
    }

    private void dropXp(int discreteXp, Location location) {
        World world = location.getWorld();
        if (world == null) return;
        ExperienceOrb xpOrb = (ExperienceOrb) world.spawnEntity(location, EntityType.EXPERIENCE_ORB);
        xpOrb.setExperience(discreteXp);
    }
}