package com.djrapitops.banners;

import org.bukkit.Bukkit;

public class BannerAward {

    private final int everyXKills;
    private final String bannerTag;

    public BannerAward(int everyXKills, String bannerTag) {
        this.everyXKills = everyXKills;
        this.bannerTag = bannerTag;
    }

    public boolean shouldAward(int killCount) {
        return killCount % everyXKills == 0;
    }

    public void award(String playerName) {
        Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                "give " + playerName + "minecraft:banner 1 0 " + bannerTag
        );
    }
}
