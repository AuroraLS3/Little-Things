package com.djrapitops.littlefx.condition;

import org.bukkit.Location;

import java.util.function.Predicate;

public class HeightCondition implements Predicate<Location> {

    private final int below;
    private final int above;
    private final boolean overlapping;

    public HeightCondition(int below, int above) {
        this.below = below;
        this.above = above;
        overlapping = below > above;
    }

    @Override
    public boolean test(Location location) {
        double y = location.getY();
        return overlapping
                ? (above <= y && y <= below)
                : (above <= y || y <= below);
    }
}
