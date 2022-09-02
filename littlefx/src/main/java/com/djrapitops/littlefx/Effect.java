package com.djrapitops.littlefx;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Defines a config entry for a potion effect.
 *
 * @author AuroraLS3
 */
public class Effect {

    private final int length;
    private final int strength;
    private final boolean showParticles;
    private final Set<PotionEffectType> effects;
    private final Set<Predicate<Location>> conditions;
    private final String permission;
    private final boolean appliesToMobs;

    private final Map<UUID, Long> lastApplied = new HashMap<>();

    public Effect(int length, int strength, boolean showParticles, Set<PotionEffectType> effects, Set<Predicate<Location>> conditions, String permission, boolean appliesToMobs) {
        this.length = length;
        this.effects = effects;
        this.showParticles = showParticles;
        this.conditions = conditions;
        this.strength = strength;
        this.permission = permission;
        this.appliesToMobs = appliesToMobs;
    }

    public boolean shouldApplyToPlayer(Player player) {
        if (permission != null && !player.hasPermission(permission)) {
            return false;
        }
        Long lastAppliedToPlayer = this.lastApplied.get(player.getUniqueId());
        long delay = length - 2L;
        if (delay <= 0) {
            delay = 1;
        }
        if (lastAppliedToPlayer != null && System.currentTimeMillis() - lastAppliedToPlayer < delay * 1000) {
            return false;
        }

        for (Predicate<Location> condition : conditions) {
            if (!condition.test(player.getLocation())) return false;
        }
        return true;
    }

    public boolean shouldApplyToMob(LivingEntity entity) {
        if (!appliesToMobs) return false;
        for (Predicate<Location> condition : conditions) {
            if (!condition.test(entity.getLocation())) return false;
        }
        return true;
    }

    public boolean appliesToMobs() {
        return appliesToMobs;
    }

    public void apply(LivingEntity entity) {
        for (PotionEffectType effectType : effects) {
            PotionEffect currentEffect = entity.getPotionEffect(effectType);
            if (currentEffect != null && currentEffect.getDuration() > length * 20 && currentEffect.getAmplifier() > strength - 1) {
                continue;
            }
            boolean ambient = false;
            entity.addPotionEffect(new PotionEffect(effectType, length * 20, strength - 1, ambient, showParticles));
        }
        lastApplied.put(entity.getUniqueId(), System.currentTimeMillis());
    }
}