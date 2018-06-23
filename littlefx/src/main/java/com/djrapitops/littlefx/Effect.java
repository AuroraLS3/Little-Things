package com.djrapitops.littlefx;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Defines a config entry for a potion effect.
 *
 * @author Rsl1122
 */
public class Effect {

    private final int length;
    private final int strength;
    private final Set<PotionEffectType> effects;
    private final Set<Material> blocks;
    private final String permission;

    private Map<UUID, Long> lastApplied = new HashMap<UUID, Long>();

    public Effect(int length, int strength, Set<PotionEffectType> effects, Set<Material> blocks, String permission) {
        this.length = length;
        this.effects = effects;
        this.blocks = blocks;
        this.strength = strength;
        this.permission = permission;
    }

    public boolean shouldApplyTo(Player player) {
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

        Block playerBlock = player.getLocation().getBlock();
        Block down = playerBlock.getRelative(BlockFace.DOWN);
        Block up = playerBlock.getRelative(BlockFace.UP);
        Block north = playerBlock.getRelative(BlockFace.NORTH);
        Block south = playerBlock.getRelative(BlockFace.SOUTH);
        Block east = playerBlock.getRelative(BlockFace.EAST);
        Block west = playerBlock.getRelative(BlockFace.WEST);

        for (Block block : Arrays.asList(playerBlock, down, up, north, south, east, west)) {
            if (blocks.contains(block.getType())) {
                return true;
            }
        }
        return false;
    }

    public void apply(Player player) {
        for (PotionEffectType effect : effects) {
            PotionEffect currentEffect = player.getPotionEffect(effect);
            if (currentEffect != null && currentEffect.getDuration() > length * 20 && currentEffect.getAmplifier() > strength - 1) {
                continue;
            }
            player.addPotionEffect(effect.createEffect(length * 20, strength - 1), true);
        }
        lastApplied.put(player.getUniqueId(), System.currentTimeMillis());
    }
}