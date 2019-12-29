package com.djrapitops.littlecomposter;

import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for getting Compost items out of config file.
 *
 * @author Rsl1122
 */
public class ComposterConfig {

    private final Logger logger;
    private final FileConfiguration config;

    public ComposterConfig(Logger logger, FileConfiguration config) {
        this.logger = logger;
        this.config = config;
    }

    public Map<Material, Double> loadCompostableMaterials() {
        Map<Material, Double> chances = new EnumMap<>(Material.class);
        addMaterials(chances, "30_percent", 0.3);
        addMaterials(chances, "50_percent", 0.5);
        addMaterials(chances, "65_percent", 0.65);
        addMaterials(chances, "80_percent", 0.80);
        addMaterials(chances, "100_percent", 1.0);
        return chances;
    }

    private void addMaterials(Map<Material, Double> chances, String level, double chance) {
        for (String material : config.getStringList("Compostable." + level)) {
            try {
                chances.put(getMaterial(material), chance);
            } catch (InvalidConfigurationException e) {
                logger.log(Level.WARNING, "Misconfiguration: " + e.getMessage());
            }
        }
    }

    private Material getMaterial(String blockName) throws InvalidConfigurationException {
        Material material = Material.getMaterial(blockName);
        if (material == null) {
            throw new InvalidConfigurationException("Unknown material: " + blockName);
        }
        return material;
    }
}