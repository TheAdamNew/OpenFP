/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 */
package com.fortunepillars.game;

import org.bukkit.Material;

public enum GameModeType {
    NORMAL("Normal", Material.IRON_SWORD, "<gray>Standard loot tables with all items."),
    BALANCED("Balanced", Material.STONE_SWORD, "<gray>Mid-tier items only. No OP gear!"),
    SWAPPER("Swapper", Material.ENDER_PEARL, "<gray>Positions swap every 30 seconds!"),
    SHUFFLE("Shuffle", Material.HOPPER, "<gray>Inventories randomize every 60 seconds!"),
    LAVA_RISING("Lava Rising", Material.LAVA_BUCKET, "<gray>Lava rises from the bottom!"),
    BORDER_SHRINK("Border Shrink", Material.BARRIER, "<gray>World border shrinks over time!"),
    SPEED_UHC("Speed UHC", Material.GOLDEN_APPLE, "<gray>Fast-paced Ultra Hardcore mode!"),
    TNT_RAIN("TNT Rain", Material.TNT, "<gray>TNT randomly falls from the sky!");

    private final String displayName;
    private final Material icon;
    private final String description;

    private GameModeType(String displayName, Material icon, String description) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public Material getIcon() {
        return this.icon;
    }

    public String getDescription() {
        return this.description;
    }

    public static GameModeType fromString(String name) {
        for (GameModeType type : GameModeType.values()) {
            if (!type.name().equalsIgnoreCase(name)) continue;
            return type;
        }
        return NORMAL;
    }

    public boolean hasSpecialTasks() {
        return this == SWAPPER || this == SHUFFLE || this == LAVA_RISING || this == BORDER_SHRINK || this == TNT_RAIN;
    }

    public boolean modifiesLoot() {
        return this == BALANCED || this == SPEED_UHC;
    }
}

