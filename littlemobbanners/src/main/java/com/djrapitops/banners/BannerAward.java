package com.djrapitops.banners;

import org.bukkit.Bukkit;

public class BannerAward {

    private final int everyXKills;
    private final String bannerTag;
    private final String readableName;

    public BannerAward(int everyXKills, String bannerTag, String readableName) {
        this.everyXKills = everyXKills;
        this.bannerTag = bannerTag;
        this.readableName = readableName;
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
}
