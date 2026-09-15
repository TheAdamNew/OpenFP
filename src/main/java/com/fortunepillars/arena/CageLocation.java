/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.World
 *  org.bukkit.block.Block
 *  org.bukkit.entity.Player
 */
package com.fortunepillars.arena;

import com.fortunepillars.FortunePillars;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class CageLocation {
    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final List<Location> cageBlockLocations = new ArrayList<Location>();

    public CageLocation(Location location) {
        this.worldName = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    public CageLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Location getLocation() {
        World world = Bukkit.getWorld((String)this.worldName);
        if (world == null) {
            return null;
        }
        return new Location(world, this.x, this.y, this.z, this.yaw, this.pitch);
    }

    public void teleportPlayer(Player player) {
        Location loc = this.getLocation();
        if (loc != null) {
            player.teleport(loc);
        }
    }

    public void buildCage(int size, Material cageMaterial) {
        Location loc = this.getLocation();
        if (loc == null) {
            return;
        }
        World world = loc.getWorld();
        int cx = loc.getBlockX();
        int cy = loc.getBlockY();
        int cz = loc.getBlockZ();
        this.removeCage();
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 2; ++y) {
                for (int z = -1; z <= 1; ++z) {
                    Location blockLoc;
                    Block block;
                    if (x == 0 && z == 0 && y >= 0 && y <= 1 || (block = (blockLoc = new Location(world, (double)(cx + x), (double)(cy + y), (double)(cz + z))).getBlock()).getType() != Material.AIR && block.getType() != Material.CAVE_AIR) continue;
                    block.setType(cageMaterial);
                    this.cageBlockLocations.add(blockLoc.clone());
                }
            }
        }
    }

    public void buildCage(int size, Player player, FortunePillars plugin) {
        Material cageMaterial = plugin.getCageManager().getPlayerCageColor(player);
        this.buildCage(size, cageMaterial);
    }

    @Deprecated
    public void buildCage(int size) {
        this.buildCage(size, Material.GLASS);
    }

    public void removeCage() {
        if (this.cageBlockLocations.isEmpty()) {
            return;
        }
        int removed = 0;
        for (Location loc : this.cageBlockLocations) {
            Block block = loc.getBlock();
            if (block.getType().isAir()) continue;
            block.setType(Material.AIR);
            ++removed;
        }
        this.cageBlockLocations.clear();
        if (removed > 0) {
            Bukkit.getLogger().info("Removed " + removed + " cage blocks");
        }
    }

    public boolean hasCage() {
        return !this.cageBlockLocations.isEmpty();
    }

    public Map<String, Object> serialize() {
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("world", this.worldName);
        data.put("x", this.x);
        data.put("y", this.y);
        data.put("z", this.z);
        data.put("yaw", Float.valueOf(this.yaw));
        data.put("pitch", Float.valueOf(this.pitch));
        return data;
    }

    public static CageLocation deserialize(Map<String, Object> data) {
        String world = (String)data.get("world");
        double x = ((Number)data.get("x")).doubleValue();
        double y = ((Number)data.get("y")).doubleValue();
        double z = ((Number)data.get("z")).doubleValue();
        float yaw = ((Number)data.get("yaw")).floatValue();
        float pitch = ((Number)data.get("pitch")).floatValue();
        return new CageLocation(world, x, y, z, yaw, pitch);
    }

    public String getWorldName() {
        return this.worldName;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }
}

