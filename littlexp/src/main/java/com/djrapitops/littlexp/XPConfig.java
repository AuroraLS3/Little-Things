package com.djrapitops.littlexp;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Class for getting stuff out of config file.
 *
 * @author Rsl1122
 */
public class XPConfig {

    private final Logger logger;
    private final FileConfiguration config;

    public XPConfig(Logger logger, FileConfiguration config) {
        this.logger = logger;
        this.config = config;
    }

    public Map<Material, Double> getCraftingXpAmounts() {
        if (!config.getBoolean("More_xp_from.Crafting_expensive_items")) {
            return Collections.emptyMap();
        }
        Map<Material, Double> xpAmounts = new EnumMap<>(Material.class);
        for (String row : config.getStringList("Xp_amounts.Crafted_items")) {
            try {
                String[] got = StringUtils.split(row, ';');
                if (got.length <= 0) continue;
                Material material = getMaterial(got[0]);
                double xp = got.length >= 2 ? parseDouble(got[1]) : 0.1;
                xpAmounts.put(material, xp);
            } catch (InvalidConfigurationException e) {
                logger.warning("Misconfigured crafting xp amount '" + row + "', " + e.getMessage());
            }
        }
        return xpAmounts;
    }

    public Map<Material, Double> getFarmingXpAmounts() {
        if (!config.getBoolean("More_xp_from.Farming")) {
            return Collections.emptyMap();
        }
        Map<Material, Double> xpAmounts = new EnumMap<>(Material.class);
        for (String row : config.getStringList("Xp_amounts.Farming")) {
            try {
                String[] got = StringUtils.split(row, ';');
                if (got.length <= 0) continue;
                Material material = getMaterial(got[0]);
                double xp = got.length >= 2 ? parseDouble(got[1]) : 0.1;
                xpAmounts.put(material, xp);
            } catch (InvalidConfigurationException e) {
                logger.warning("Misconfigured farming xp amount '" + row + "', " + e.getMessage());
            }
        }
        return xpAmounts;
    }

    public Double getAdvancementsXpAmount() {
        if (!config.getBoolean("More_xp_from.Advancements")) {
            return null;
        }
        return config.getDouble("Xp_amounts.Advancements");
    }

    public Map<Material, Double> getToolBreakingXpAmounts() {
        if (!config.getBoolean("More_xp_from.Tools_breaking")) {
            return Collections.emptyMap();
        }
        Map<Material, Double> xpAmounts = new EnumMap<>(Material.class);
        for (String row : config.getStringList("Xp_amounts.Tools_breaking")) {
            try {
                String[] got = StringUtils.split(row, ';');
                if (got.length <= 0) continue;
                Material material = getMaterial(got[0]);
                double xp = got.length >= 2 ? parseDouble(got[1]) : 0.1;
                xpAmounts.put(material, xp);
            } catch (InvalidConfigurationException e) {
                logger.warning("Misconfigured tool breaking xp amount '" + row + "', " + e.getMessage());
            }
        }
        return xpAmounts;
    }

    private double parseDouble(String from) throws InvalidConfigurationException {
        try {
            return Double.parseDouble(from);
        } catch (NumberFormatException e) {
            throw new InvalidConfigurationException("Invalid number '" + from + "'");
        }
    }

    private Material getMaterial(String blockName) throws InvalidConfigurationException {
        Material material = Material.getMaterial(blockName);
        if (material == null) {
            throw new InvalidConfigurationException("Material does not exist: " + blockName);
        }
        return material;
    }

}