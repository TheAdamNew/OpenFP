/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.World
 *  org.bukkit.block.Block
 *  org.bukkit.entity.Player
 */
package com.fortunepillars.arena;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.cosmetics.CageColorType;
import com.fortunepillars.cosmetics.CosmeticType;
import com.fortunepillars.player.PlayerData;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class CageManager {
    private final FortunePillars plugin;
    private final Map<UUID, Set<Location>> playerCageBlocks;
    private final Map<UUID, Material> playerCageColors;

    public CageManager(FortunePillars plugin) {
        this.plugin = plugin;
        this.playerCageBlocks = new HashMap<UUID, Set<Location>>();
        this.playerCageColors = new HashMap<UUID, Material>();
    }

    public Material getPlayerCageColor(Player player) {
        block8: {
            String colorKey;
            block9: {
                Material material;
                String selectedCage;
                PlayerData pd;
                Material cached = this.playerCageColors.get(player.getUniqueId());
                if (cached != null) {
                    return cached;
                }
                Arena arena = this.plugin.getArenaManager().getPlayerArena(player);
                if (arena != null && (pd = arena.getPlayerData(player.getUniqueId())) != null && pd.getCageColor() != null) {
                    this.playerCageColors.put(player.getUniqueId(), pd.getCageColor());
                    return pd.getCageColor();
                }
                if (this.plugin.getCosmeticsManager() != null && (selectedCage = this.plugin.getCosmeticsManager().getSelectedCosmetic(player, CosmeticType.CAGE)) != null && !selectedCage.isEmpty() && (material = this.getCageMaterialById(selectedCage)) != null) {
                    this.playerCageColors.put(player.getUniqueId(), material);
                    return material;
                }
                colorKey = this.plugin.getDatabase().getPlayerCageColor(player.getUniqueId());
                if (colorKey == null || colorKey.isEmpty()) break block8;
                material = this.getCageMaterialById(colorKey);
                if (material == null) break block9;
                this.playerCageColors.put(player.getUniqueId(), material);
                return material;
            }
            try {
                Material enumMaterial = CageColorType.fromString(colorKey).getMaterial();
                this.playerCageColors.put(player.getUniqueId(), enumMaterial);
                return enumMaterial;
            }
            catch (Exception exception) {
                try {
                }
                catch (Exception e) {
                    this.plugin.getLogger().warning("Failed to get cage color from database: " + e.getMessage());
                }
            }
        }
        Material defaultMaterial = Material.WHITE_STAINED_GLASS;
        this.playerCageColors.put(player.getUniqueId(), defaultMaterial);
        return defaultMaterial;
    }

    public Material getPlayerCageMaterial(Player player) {
        return this.getPlayerCageColor(player);
    }

    public void setPlayerCageColor(Player player, String colorKey) {
        Material material = this.getCageMaterialById(colorKey);
        if (material == null) {
            try {
                material = CageColorType.fromString(colorKey).getMaterial();
            }
            catch (Exception e) {
                material = Material.WHITE_STAINED_GLASS;
            }
        }
        this.playerCageColors.put(player.getUniqueId(), material);
        this.plugin.getDatabase().setPlayerCageColor(player.getUniqueId(), colorKey);
    }

    private Material getCageMaterialById(String id) {
        if (id == null) {
            return null;
        }
        return switch (id.toLowerCase()) {
            case "default", "white" -> Material.WHITE_STAINED_GLASS;
            case "red" -> Material.RED_STAINED_GLASS;
            case "orange" -> Material.ORANGE_STAINED_GLASS;
            case "yellow" -> Material.YELLOW_STAINED_GLASS;
            case "lime", "green" -> Material.LIME_STAINED_GLASS;
            case "cyan" -> Material.CYAN_STAINED_GLASS;
            case "light_blue", "lightblue" -> Material.LIGHT_BLUE_STAINED_GLASS;
            case "blue" -> Material.BLUE_STAINED_GLASS;
            case "purple" -> Material.PURPLE_STAINED_GLASS;
            case "magenta" -> Material.MAGENTA_STAINED_GLASS;
            case "pink" -> Material.PINK_STAINED_GLASS;
            case "black" -> Material.BLACK_STAINED_GLASS;
            case "gray", "grey" -> Material.GRAY_STAINED_GLASS;
            case "light_gray", "lightgray" -> Material.LIGHT_GRAY_STAINED_GLASS;
            case "brown" -> Material.BROWN_STAINED_GLASS;
            case "rainbow" -> this.getRandomGlassColor();
            case "gold", "golden" -> Material.YELLOW_STAINED_GLASS;
            case "diamond" -> Material.LIGHT_BLUE_STAINED_GLASS;
            case "emerald" -> Material.LIME_STAINED_GLASS;
            case "iron" -> Material.LIGHT_GRAY_STAINED_GLASS;
            case "clear" -> Material.GLASS;
            case "tinted" -> Material.TINTED_GLASS;
            case "obsidian" -> Material.OBSIDIAN;
            case "bedrock" -> Material.BEDROCK;
            default -> {
                Material material = Material.matchMaterial((String)id);
                if (material != null && material.isBlock()) {
                    yield material;
                }
                yield null;
            }
        };
    }

    private Material getRandomGlassColor() {
        Material[] colors = new Material[]{Material.RED_STAINED_GLASS, Material.ORANGE_STAINED_GLASS, Material.YELLOW_STAINED_GLASS, Material.LIME_STAINED_GLASS, Material.CYAN_STAINED_GLASS, Material.LIGHT_BLUE_STAINED_GLASS, Material.BLUE_STAINED_GLASS, Material.PURPLE_STAINED_GLASS, Material.MAGENTA_STAINED_GLASS, Material.PINK_STAINED_GLASS};
        return colors[new Random().nextInt(colors.length)];
    }

    public void createCage(Player player, Location center) {
        Material cageMaterial = this.getPlayerCageMaterial(player);
        HashSet<Location> blocks = new HashSet<Location>();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        World world = center.getWorld();
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 2; ++y) {
                for (int z = -1; z <= 1; ++z) {
                    Location blockLoc;
                    Block block;
                    if (x == 0 && z == 0 && y >= 0 && y <= 1 || (block = (blockLoc = new Location(world, (double)(cx + x), (double)(cy + y), (double)(cz + z))).getBlock()).getType() != Material.AIR && block.getType() != Material.CAVE_AIR) continue;
                    block.setType(cageMaterial);
                    blocks.add(blockLoc.clone());
                }
            }
        }
        this.playerCageBlocks.put(player.getUniqueId(), blocks);
        this.plugin.getLogger().info("Created tight cage for " + player.getName() + " with material " + cageMaterial.name());
    }

    public void removeCage(Player player) {
        this.removeCage(player.getUniqueId());
    }

    public void removeCage(UUID playerUUID) {
        Set<Location> blocks = this.playerCageBlocks.remove(playerUUID);
        if (blocks != null) {
            for (Location loc : blocks) {
                Block block = loc.getBlock();
                if (!this.isGlassBlock(block.getType()) && !this.isCageMaterial(block.getType())) continue;
                block.setType(Material.AIR);
            }
        }
    }

    public void removeAllCages(Arena arena) {
        for (Player player : arena.getOnlinePlayers()) {
            this.removeCage(player);
        }
    }

    public void removeAllCages() {
        for (UUID uuid : new HashSet<UUID>(this.playerCageBlocks.keySet())) {
            this.removeCage(uuid);
        }
    }

    private boolean isGlassBlock(Material material) {
        return material.name().contains("GLASS");
    }

    private boolean isCageMaterial(Material material) {
        return material == Material.OBSIDIAN || material == Material.BEDROCK || material == Material.DIAMOND_BLOCK || this.isGlassBlock(material);
    }

    public boolean hasCage(Player player) {
        return this.playerCageBlocks.containsKey(player.getUniqueId());
    }

    public Set<Location> getCageBlocks(Player player) {
        return this.playerCageBlocks.getOrDefault(player.getUniqueId(), Collections.emptySet());
    }

    public void clearCache(UUID uuid) {
        this.playerCageColors.remove(uuid);
    }

    public void clearAllCache() {
        this.playerCageColors.clear();
    }

    public void shutdown() {
        this.removeAllCages();
        this.playerCageBlocks.clear();
        this.playerCageColors.clear();
    }
}

