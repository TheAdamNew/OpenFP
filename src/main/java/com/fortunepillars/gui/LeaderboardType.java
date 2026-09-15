/*
 * Decompiled with CFR 0.152.
 */
package com.fortunepillars.gui;

public enum LeaderboardType {
    WINS("wins", "Top Wins"),
    KILLS("kills", "Top Kills"),
    WINSTREAK("winstreak", "Highest Winstreak");

    private final String key;
    private final String displayName;

    private LeaderboardType(String key, String displayName) {
        this.key = key;
        this.displayName = displayName;
    }

    public String getKey() {
        return this.key;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public static LeaderboardType fromKey(String key) {
        if (key == null) {
            return null;
        }
        for (LeaderboardType type : LeaderboardType.values()) {
            if (!type.key.equalsIgnoreCase(key)) continue;
            return type;
        }
        return null;
    }
}

