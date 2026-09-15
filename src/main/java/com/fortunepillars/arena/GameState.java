/*
 * Decompiled with CFR 0.152.
 */
package com.fortunepillars.arena;

public enum GameState {
    DISABLED("Disabled", "<dark_gray>"),
    WAITING("Waiting", "<green>"),
    STARTING("Starting", "<yellow>"),
    IN_GAME("In Game", "<red>"),
    ENDING("Ending", "<gold>"),
    RESETTING("Resetting", "<gray>");

    private final String displayName;
    private final String color;

    private GameState(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getColor() {
        return this.color;
    }

    public String getColoredName() {
        return this.color + this.displayName;
    }

    public boolean isJoinable() {
        return this == WAITING || this == STARTING;
    }

    public boolean isActive() {
        return this == IN_GAME;
    }

    public boolean allowsPlayerDamage() {
        return this == IN_GAME;
    }
}

