package com.djrapitops.banners;

import org.bukkit.Bukkit;

import java.util.Optional;

public class BannerAward {

    private final int everyXKills;
    private final String bannerTag;
    private final String readableName;
    private final String extraCommand;

    public BannerAward(int everyXKills, String bannerTag, String readableName, String extraCommand) {
        this.everyXKills = everyXKills;
        this.bannerTag = bannerTag;
        this.readableName = readableName;
        this.extraCommand = extraCommand;
    }

    public boolean shouldAward(int killCount) {
        return killCount % everyXKills == 0;
    }

    public boolean award(String playerName) {
        return Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                "minecraft:give " + playerName + " minecraft:" + bannerTag.replace("_banner{", "_banner{display:{Name:\"{\\\"text\\\":\\\"" + readableName + " banner" + "\\\"}\"},") + " 1"
        );
    }

    public Optional<String> getExtraCommand() {
        return Optional.ofNullable(extraCommand);
    }
}
