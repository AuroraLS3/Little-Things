package com.djrapitops.littlechef;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for getting Effects out of config file.
 *
 * @author Rsl1122
 */
public class ChefConfig {

    private final Logger logger;
    private final FileConfiguration config;

    public ChefConfig(Logger logger, FileConfiguration config) {
        this.logger = logger;
        this.config = config;
    }

    public List<FurnaceRecipe> loadRecipes() {
        ConfigurationSection recipes = config.getConfigurationSection("Recipes");
        return getRecipes(recipes);
    }

    private List<FurnaceRecipe> getRecipes(ConfigurationSection recipes) {
        List<FurnaceRecipe> list = new ArrayList<FurnaceRecipe>();
        for (String recipeName : recipes.getKeys(false)) {
            try {
                ConfigurationSection recipe = recipes.getConfigurationSection(recipeName);
                if (!recipe.contains("In")) {
                    throw new InvalidConfigurationException("No 'In' section");
                }
                if (!recipe.contains("Out")) {
                    throw new InvalidConfigurationException("No 'Out' section");
                }
                list.add(getFurnaceRecipe(recipe));
            } catch (InvalidConfigurationException e) {
                logger.log(Level.WARNING, "Misconfigured Recipe (" + recipeName + "): " + e.getMessage());
            }
        }
        return list;
    }

    private FurnaceRecipe getFurnaceRecipe(ConfigurationSection recipe) throws InvalidConfigurationException {
        ConfigurationSection in = recipe.getConfigurationSection("In");
        Material ingredient = getMaterial(in.getString("Item"));

        ConfigurationSection out = recipe.getConfigurationSection("Out");
        Material outputItem = getMaterial(out.getString("Item"));
        int amount = out.contains("Amount") ? out.getInt("Amount") : 1;

        ItemStack output = new ItemStack(outputItem, amount);

        if (out.contains("Name")) {
            ItemMeta meta = output.getItemMeta();
            meta.setDisplayName(ChatColor.RESET + out.getString("Name"));
            output.setItemMeta(meta);
        }
        if (out.contains("Saturation")) {
            ItemMeta meta = output.getItemMeta();
            meta.setLore(Arrays.asList(
                    "Extra Hunger Points:",
                    Integer.toString(out.getInt("Saturation"))
            ));
            output.setItemMeta(meta);
        }

        return new FurnaceRecipe(output, ingredient);
    }

    private Material getMaterial(String blockName) throws InvalidConfigurationException {
        Material material = Material.getMaterial(blockName);
        if (material == null) {
            throw new InvalidConfigurationException("Material does not exist: " + blockName);
        }
        return material;
    }

}