package com.djrapitops.littlechef;

import org.bukkit.ChatColor;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Main JavaPlugin class.
 *
 * @author Rsl1122
 */
public class LittleChef extends JavaPlugin implements Listener {

    private Logger logger;
    private Collection<Recipe> recipes = Collections.emptyList();

    @Override
    public void onEnable() {
        logger = getLogger();

        reloadRecipes();

        getServer().getPluginManager().registerEvents(this, this);

        getCommand("littlechef").setExecutor(this);

        logger.log(Level.INFO, "Enabled LittleChef.");
    }

    private NamespacedKey getKey(String key) {
        return new NamespacedKey(this, key);
    }

    private void clearRecipes() {
        Set<NamespacedKey> keys = recipes.stream()
                .map(this::getKey)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        Iterator<Recipe> present = getServer().recipeIterator();
        while (present.hasNext()) {
            Optional<NamespacedKey> key = getKey(present.next());
            if (key.isPresent() && keys.contains(key.get())) {
                present.remove();
            }
        }
        recipes.clear();
    }

    private Optional<NamespacedKey> getKey(Recipe recipe) {
        if (recipe instanceof Keyed) {
            return Optional.of(((Keyed) recipe).getKey());
        }
        return Optional.empty();
    }

    private void reloadRecipes() {
        saveDefaultConfig();
        reloadConfig();
        clearRecipes();
        loadRecipes();
    }

    private void loadRecipes() {
        recipes = new ChefConfig(logger, getConfig(), this::getKey).loadRecipes();

        for (Recipe recipe : recipes) {
            loadRecipe(recipe);
        }
        logger.log(Level.INFO, "Loaded " + recipes.size() + " recipes.");
    }

    private void loadRecipe(Recipe recipe) {
        try {
            if (!getServer().addRecipe(recipe)) {
                logger.log(Level.WARNING, "Could not add a recipe: " + recipe);
            }
        } catch (IllegalStateException recipeExists) {
            logger.log(Level.WARNING, recipeExists.getMessage());
        }
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
            reloadRecipes();
            sender.sendMessage(ChatColor.GREEN + "Loaded " + recipes.size() + " recipes.");
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