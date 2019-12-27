package com.djrapitops.littlefx.condition;

import org.bukkit.Location;

import java.util.Set;
import java.util.function.Predicate;

// Hardcoded class names to prevent exception when RegionCondition class is imported.
public class RegionCondition implements Predicate<Location> {

    private final String regionName;

    public RegionCondition(String regionName) {
        this.regionName = regionName;
    }

    @Override
    public boolean test(Location location) {
        for (com.sk89q.worldguard.protection.regions.ProtectedRegion region : getRegions(location)) {
            if (regionName.equals(region.getId())) return true;
        }
        return false;
    }

    private Set<com.sk89q.worldguard.protection.regions.ProtectedRegion> getRegions(Location location) {
        return getApplicableRegions(location, createQuery()).getRegions();
    }

    private com.sk89q.worldguard.protection.regions.RegionQuery createQuery() {
        return com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
    }

    private com.sk89q.worldguard.protection.ApplicableRegionSet getApplicableRegions(Location location, com.sk89q.worldguard.protection.regions.RegionQuery query) {
        return query.getApplicableRegions(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(location));
    }
}
