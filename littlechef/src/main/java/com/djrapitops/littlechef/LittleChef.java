package com.djrapitops.littlechef;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main JavaPlugin class.
 *
 * @author Rsl1122
 */
public class LittleChef extends JavaPlugin implements Listener {

    private Logger logger;
    private List<FurnaceRecipe> recipes;

    @Override
    public void onEnable() {
        logger = getLogger();

        reloadEffects();

        getServer().getPluginManager().registerEvents(this, this);

        getCommand("littlechef").setExecutor(this);

        logger.log(Level.INFO, "Enabled LittleChef.");
    }

    private void reloadEffects() {
        saveDefaultConfig();
        reloadConfig();
        recipes = new ChefConfig(logger, getConfig()).loadRecipes();
        for (FurnaceRecipe recipe : recipes) {
            if (!getServer().addRecipe(recipe)) {
                logger.log(Level.WARNING, "Could not add a recipe: " + recipe);
            }
        }
        logger.log(Level.INFO, "Loaded " + recipes.size() + " recipes.");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Plugin) this);
        logger.log(Level.INFO, "Disabled LittleChef.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("littlechef.reload")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission for this command!");
        }
        if (args.length != 0 && args[0].equals("reload")) {
            reloadEffects();
            sender.sendMessage(ChatColor.GREEN + "Loaded " + recipes.size() + " recipes.");
            sender.sendMessage(ChatColor.YELLOW + "Unloading old recipes might require a server restart.");
        } else {
            sender.sendMessage(new String[]{"> " + ChatColor.GRAY + "LittleChef Help:",
                    "",
                    ChatColor.GRAY + "  /littlechef reload " + ChatColor.WHITE + "Reloads recipes from config.",
                    "",
                    ">"
            });
            sender.sendMessage(ChatColor.GRAY + "LittleChef Help:");
        }
        return true;
    }

    @EventHandler
    public void onPlayerEat(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (!item.hasItemMeta()) {
            return;
        }
        ItemMeta itemMeta = item.getItemMeta();
        if (!itemMeta.hasLore()) {
            return;
        }
        List<String> lore = itemMeta.getLore();
        if (lore.isEmpty() || lore.size() < 2) {
            return;
        }
        if (!lore.get(0).equals("Extra Hunger Points:")) {
            return;
        }

        int extraPoints;
        try {
            extraPoints = Integer.parseInt(lore.get(1));
        } catch (NumberFormatException e) {
            return;
        }

        Player player = event.getPlayer();
        float saturation = player.getSaturation();
        saturation += extraPoints;
        player.setSaturation(saturation);
    }
}