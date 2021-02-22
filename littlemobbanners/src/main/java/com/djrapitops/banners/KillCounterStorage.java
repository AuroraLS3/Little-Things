package com.djrapitops.banners;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class KillCounterStorage {

    private final Path storageFolder;

    public KillCounterStorage(File dataFolder) throws IOException {
        this.storageFolder = dataFolder.toPath().resolve("storage");
        Files.createDirectories(storageFolder);
    }

    public void save(UUID playerUUID, KillCounter killCounter) throws IOException {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<EntityType, Integer> entry : killCounter.getKills().entrySet()) {
            config.set(entry.getKey().name(), entry.getValue());
        }
        config.save(getPlayerFile(playerUUID));
    }

    public File getPlayerFile(UUID playerUUID) {
        return storageFolder.resolve(playerUUID.toString() + ".yml").toFile();
    }

    public KillCounter load(Logger logger, UUID playerUUID) throws IOException, InvalidConfigurationException {
        KillCounter killCounter = new KillCounter(playerUUID);
        File playerFile = getPlayerFile(playerUUID);
        if (!playerFile.exists()) return killCounter;

        YamlConfiguration config = new YamlConfiguration();
        config.load(playerFile);
        for (String key : config.getKeys(false)) {
            try {
                killCounter.setKillCount(EntityType.valueOf(key), config.getInt(key));
            } catch (IllegalArgumentException e) {
                logger.warning("Player file plugins/MobBanners/storage/" + playerUUID + ".yml had nonexisting mob '" + key + "'");
            }
        }
        return killCounter;
    }
}
