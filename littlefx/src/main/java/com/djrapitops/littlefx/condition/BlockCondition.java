package com.djrapitops.littlefx.condition;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.Set;
import java.util.function.Predicate;

public class BlockCondition implements Predicate<Location> {

    private final Set<Material> materials;

    public BlockCondition(Set<Material> materials) {
        this.materials = materials;
    }

    @Override
    public boolean test(Location location) {
        Block block = location.getBlock();
        Block down = block.getRelative(BlockFace.DOWN);
        Block up = block.getRelative(BlockFace.UP);
        Block north = block.getRelative(BlockFace.NORTH);
        Block south = block.getRelative(BlockFace.SOUTH);
        Block east = block.getRelative(BlockFace.EAST);
        Block west = block.getRelative(BlockFace.WEST);

        return test(block, down, up, north, south, east, west);
    }

    private boolean test(Block... blocks) {
        for (Block block : blocks) {
            if (materials.contains(block.getType())) {
                return true;
            }
        }
        return false;
    }
}
