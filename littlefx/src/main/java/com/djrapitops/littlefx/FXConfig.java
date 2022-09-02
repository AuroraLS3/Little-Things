package com.djrapitops.littlefx;

import com.djrapitops.littlefx.condition.BlockCondition;
import com.djrapitops.littlefx.condition.HeightCondition;
import com.djrapitops.littlefx.condition.RegionCondition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.function.Predicate;
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
        List<Effect> list = new ArrayList<>();
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
        boolean hasParticles = effect.contains("Particles");
        boolean requiresPermission = effect.contains("Permission");
        boolean appliesToMobs = effect.getBoolean("Also_for_Mobs");
        List<String> blockNames = effect.getStringList("Blocks");
        List<String> potionEffectNames = effect.getStringList("Effects");
        Optional<Integer> above = effect.contains("Above_y") ? Optional.of(effect.getInt("Above_y")) : Optional.empty();
        Optional<Integer> below = effect.contains("Below_y") ? Optional.of(effect.getInt("Below_y")) : Optional.empty();
        String regionName = effect.getString("Region");

        int length = hasLength ? effect.getInt("Length") : 7;
        int strength = hasStrength ? effect.getInt("Strength") : 1;
        boolean particles = hasParticles ? effect.getBoolean("Particles") : true;

        Set<Predicate<Location>> conditions = new HashSet<>();
        Set<Material> blocks = getMaterials(blockNames);
        if (!blocks.isEmpty()) {
            conditions.add(new BlockCondition(blocks));
        }

        if (above.isPresent() || below.isPresent()) {
            conditions.add(new HeightCondition(below.orElse(-50), above.orElse(255)));
        }

        if (isWorldGuardEnabled() && regionName != null && !regionName.isEmpty()) {
            conditions.add(new RegionCondition(regionName));
        }

        Set<PotionEffectType> potionEffects = getPotionEffects(potionEffectNames);
        String permission = requiresPermission ? effect.getString("Permission") : null;
        return new Effect(length, strength, particles, potionEffects, conditions, permission, appliesToMobs);
    }

    private Set<PotionEffectType> getPotionEffects(List<String> potionEffectNames) throws InvalidConfigurationException {
        Set<PotionEffectType> potionEffectTypes = new HashSet<>();
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
        if (blockNames.isEmpty()) return Collections.emptySet();

        Set<Material> materials = new HashSet<>();
        for (String blockName : blockNames) {
            Material material = Material.getMaterial(blockName);
            if (material == null) {
                throw new InvalidConfigurationException("Material does not exist: " + blockName);
            }
            materials.add(material);
        }
        return materials;
    }

    private boolean isWorldGuardEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("WorldGuard");
    }
}