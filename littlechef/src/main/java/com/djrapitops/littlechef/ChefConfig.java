package com.djrapitops.littlechef;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
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
    private final Function<String, NamespacedKey> getKey;

    public ChefConfig(Logger logger, FileConfiguration config, Function<String, NamespacedKey> getKey) {
        this.logger = logger;
        this.config = config;
        this.getKey = getKey;
    }

    public Collection<Recipe> loadRecipes() {
        ConfigurationSection recipes = config.getConfigurationSection("Recipes");
        return getRecipes(recipes);
    }

    private Collection<Recipe> getRecipes(ConfigurationSection recipes) {
        Set<Recipe> list = new HashSet<>();
        for (String recipeName : recipes.getKeys(false)) {
            try {
                ConfigurationSection recipe = recipes.getConfigurationSection(recipeName);
                if (!recipe.contains("In")) {
                    throw new InvalidConfigurationException("No 'In' section");
                }
                if (!recipe.contains("Out")) {
                    throw new InvalidConfigurationException("No 'Out' section");
                }
                list.addAll(getRecipe(recipeName, recipe));
            } catch (InvalidConfigurationException e) {
                logger.log(Level.WARNING, "Misconfigured Recipe (" + recipeName + "): " + e.getMessage());
            }
        }
        return list;
    }

    private Collection<Recipe> getRecipe(String key, ConfigurationSection recipe) throws InvalidConfigurationException {
        RecipeChoice.MaterialChoice input = getInput(recipe);
        ItemStack output = getOutput(recipe);

        float experience = recipe.contains("Xp") ? (float) recipe.getDouble("Xp") : 0.1F;
        int cookForSeconds = recipe.contains("Cooking_time_seconds") ? recipe.getInt("Cooking_time_seconds") : 10;

        boolean campfire = recipe.getBoolean("Campfire");
        boolean smoker = recipe.getBoolean("Smoker");
        boolean blast = recipe.getBoolean("Blast");

        Set<Recipe> recipes = new HashSet<>();
        recipes.add(new FurnaceRecipe(getKey.apply("furnace_" + key), output, input, experience, cookForSeconds * 20));

        if (campfire)
            recipes.add(new CampfireRecipe(getKey.apply("campfire_" + key), output, input, experience, cookForSeconds * 60));
        if (smoker)
            recipes.add(new SmokingRecipe(getKey.apply("smoker_" + key), output, input, experience, cookForSeconds * 10));
        if (blast)
            recipes.add(new BlastingRecipe(getKey.apply("blast_" + key), output, input, experience, cookForSeconds * 10));

        return recipes;
    }

    private RecipeChoice.MaterialChoice getInput(ConfigurationSection recipe) throws InvalidConfigurationException {
        ConfigurationSection in = recipe.getConfigurationSection("In");
        Material ingredient = getMaterial(in.getString("Item"));
        return new RecipeChoice.MaterialChoice(ingredient);
    }

    private ItemStack getOutput(ConfigurationSection recipe) throws InvalidConfigurationException {
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
        return output;
    }

    private Material getMaterial(String blockName) throws InvalidConfigurationException {
        Material material = Material.getMaterial(blockName);
        if (material == null) {
            throw new InvalidConfigurationException("Material does not exist: " + blockName);
        }
        return material;
    }

}