package com.djrapitops.banners;

import org.bukkit.entity.EntityType;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class KillCounter {

    private final UUID playerUUID;
    private final Map<EntityType, Integer> kills;

    public KillCounter(UUID playerUUID) {
        this.playerUUID = playerUUID;
        kills = new EnumMap<>(EntityType.class);
    }

    public int killed(EntityType type) {
        int newCount = kills.getOrDefault(type, 0) + 1;
        if (newCount == Integer.MAX_VALUE) newCount = 0;
        kills.put(type, newCount);
        return newCount;
    }

    public void setKillCount(EntityType entityType, int killCount) {
        kills.put(entityType, killCount);
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public Map<EntityType, Integer> getKills() {
        return kills;
    }
}
