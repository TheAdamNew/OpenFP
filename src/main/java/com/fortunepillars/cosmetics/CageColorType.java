/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 */
package com.fortunepillars.cosmetics;

import org.bukkit.Material;

public enum CageColorType {
    WHITE(Material.WHITE_STAINED_GLASS),
    ORANGE(Material.ORANGE_STAINED_GLASS),
    MAGENTA(Material.MAGENTA_STAINED_GLASS),
    LIGHT_BLUE(Material.LIGHT_BLUE_STAINED_GLASS),
    YELLOW(Material.YELLOW_STAINED_GLASS),
    LIME(Material.LIME_STAINED_GLASS),
    PINK(Material.PINK_STAINED_GLASS),
    GRAY(Material.GRAY_STAINED_GLASS),
    LIGHT_GRAY(Material.LIGHT_GRAY_STAINED_GLASS),
    CYAN(Material.CYAN_STAINED_GLASS),
    PURPLE(Material.PURPLE_STAINED_GLASS),
    BLUE(Material.BLUE_STAINED_GLASS),
    BROWN(Material.BROWN_STAINED_GLASS),
    GREEN(Material.GREEN_STAINED_GLASS),
    RED(Material.RED_STAINED_GLASS),
    BLACK(Material.BLACK_STAINED_GLASS),
    CLEAR(Material.GLASS),
    TINTED(Material.TINTED_GLASS),
    OBSIDIAN(Material.OBSIDIAN),
    DIAMOND(Material.DIAMOND_BLOCK),
    BEDROCK(Material.BEDROCK);

    private final Material material;

    private CageColorType(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        return this.material;
    }

    public static CageColorType fromString(String name) {
        if (name == null) {
            return WHITE;
        }
        try {
            return CageColorType.valueOf(name.toUpperCase().replace(" ", "_"));
        }
        catch (IllegalArgumentException e) {
            return WHITE;
        }
    }

    public static CageColorType fromMaterial(Material material) {
        for (CageColorType type : CageColorType.values()) {
            if (type.material != material) continue;
            return type;
        }
        return WHITE;
    }
}

