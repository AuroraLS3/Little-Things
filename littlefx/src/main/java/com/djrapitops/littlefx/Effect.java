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
 * @author Rsl1122
 */
public class Effect {

    private final int length;
    private final int strength;
    private final Set<PotionEffectType> effects;
    private final Set<Predicate<Location>> conditions;
    private final String permission;
    private final boolean appliesToMobs;

    private Map<UUID, Long> lastApplied = new HashMap<>();

    public Effect(int length, int strength, Set<PotionEffectType> effects, Set<Predicate<Location>> conditions, String permission, boolean appliesToMobs) {
        this.length = length;
        this.effects = effects;
        this.conditions = conditions;
        this.strength = strength;
        this.permission = permission;
        this.appliesToMobs = appliesToMobs;
    }

    public boolean shouldApplyToPlayer(Player player) {
        if (permission != null && !player.hasPermission(permission)) {
            return false;
        }
        Long lastApplied = this.lastApplied.get(player.getUniqueId());
        long delay = length - 2;
        if (delay <= 0) {
            delay = 1;
        }
        if (lastApplied != null && System.currentTimeMillis() - lastApplied < delay * 1000) {
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
        for (PotionEffectType effect : effects) {
            PotionEffect currentEffect = entity.getPotionEffect(effect);
            if (currentEffect != null && currentEffect.getDuration() > length * 20 && currentEffect.getAmplifier() > strength - 1) {
                continue;
            }
            entity.addPotionEffect(effect.createEffect(length * 20, strength - 1), true);
        }
        lastApplied.put(entity.getUniqueId(), System.currentTimeMillis());
    }
}