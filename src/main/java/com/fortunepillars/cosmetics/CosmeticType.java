/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 */
package com.fortunepillars.cosmetics;

import org.bukkit.Material;

public enum CosmeticType {
    CAGE("Cage", "cage", Material.GLASS, "Customize your cage appearance"),
    WIN_EFFECT("Win Effect", "win_effect", Material.FIREWORK_ROCKET, "Effects when you win"),
    KILL_EFFECT("Kill Effect", "kill_effect", Material.REDSTONE, "Effects when you get a kill"),
    DEATH_CRY("Death Cry", "death_cry", Material.NOTE_BLOCK, "Sound when you die"),
    TRAIL("Trail", "trail", Material.BLAZE_POWDER, "Particle trail behind you");

    private final String displayName;
    private final String configKey;
    private final Material icon;
    private final String description;

    private CosmeticType(String displayName, String configKey, Material icon, String description) {
        this.displayName = displayName;
        this.configKey = configKey;
        this.icon = icon;
        this.description = description;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getConfigKey() {
        return this.configKey;
    }

    public Material getIcon() {
        return this.icon;
    }

    public String getDescription() {
        return this.description;
    }

    public static CosmeticType fromString(String name) {
        if (name == null) {
            return null;
        }
        try {
            return CosmeticType.valueOf(name.toUpperCase().replace(" ", "_"));
        }
        catch (IllegalArgumentException e) {
            for (CosmeticType type : CosmeticType.values()) {
                if (!type.configKey.equalsIgnoreCase(name)) continue;
                return type;
            }
            return null;
        }
    }
}

