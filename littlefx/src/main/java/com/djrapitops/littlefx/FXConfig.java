package com.djrapitops.littlefx;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for getting Effects out of config file.
 *
 * @author Rsl1122
 */
public class FXConfig {

    private final Logger logger;
    private final FileConfiguration config;

    public FXConfig(Logger logger, FileConfiguration config) {
        this.logger = logger;
        this.config = config;
    }

    public List<Effect> loadEffects() {
        ConfigurationSection effects = config.getConfigurationSection("Effects");
        return getEffects(effects);
    }

    private List<Effect> getEffects(ConfigurationSection effects) {
        List<Effect> list = new ArrayList<Effect>();
        for (String effectName : effects.getKeys(false)) {
            try {
                ConfigurationSection effect = effects.getConfigurationSection(effectName);
                list.add(getEffect(effect));
            } catch (InvalidConfigurationException e) {
                logger.log(Level.WARNING, "Misconfigured Effect (" + effectName + "): " + e.getMessage());
            }
        }
        return list;
    }

    private Effect getEffect(ConfigurationSection effect) throws InvalidConfigurationException {

        boolean hasLength = effect.contains("Length");
        boolean hasStrength = effect.contains("Strength");
        boolean requiresPermission = effect.contains("Permission");
        List<String> blockNames = effect.getStringList("Blocks");
        List<String> potionEffectNames = effect.getStringList("Effects");

        int length = hasLength ? effect.getInt("Length") : 7;
        int strength = hasStrength ? effect.getInt("Strength") : 1;
        Set<Material> blocks = getMaterials(blockNames);
        Set<PotionEffectType> potionEffects = getPotionEffects(potionEffectNames);
        String permission = requiresPermission ? effect.getString("Permission") : null;
        return new Effect(length, strength, potionEffects, blocks, permission);
    }

    private Set<PotionEffectType> getPotionEffects(List<String> potionEffectNames) throws InvalidConfigurationException {
        Set<PotionEffectType> potionEffectTypes = new HashSet<PotionEffectType>();
        for (String potionEffectName : potionEffectNames) {
            PotionEffectType type = PotionEffectType.getByName(potionEffectName);
            if (type == null) {
                throw new InvalidConfigurationException("Potion effect does not exist: " + potionEffectName);
            }
            potionEffectTypes.add(type);
        }
        return potionEffectTypes;
    }

    private Set<Material> getMaterials(List<String> blockNames) throws InvalidConfigurationException {
        Set<Material> materials = new HashSet<Material>();
        for (String blockName : blockNames) {
            Material material = Material.getMaterial(blockName);
            if (material == null) {
                throw new InvalidConfigurationException("Material does not exist: " + blockName);
            }
            materials.add(material);
        }
        return materials;
    }

}