/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.configuration.ConfigurationSection
 */
package com.fortunepillars.utils;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class LocationUtil {
    private LocationUtil() {
    }

    public static String serialize(Location location) {
        if (location == null) {
            return null;
        }
        return String.format("%s;%.2f;%.2f;%.2f;%.2f;%.2f", location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), Float.valueOf(location.getYaw()), Float.valueOf(location.getPitch()));
    }

    public static Location deserialize(String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        try {
            String[] parts = string.split(";");
            World world = Bukkit.getWorld((String)parts[0]);
            if (world == null) {
                return null;
            }
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0.0f;
            float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0.0f;
            return new Location(world, x, y, z, yaw, pitch);
        }
        catch (Exception e) {
            return null;
        }
    }

    public static Map<String, Object> toMap(Location location) {
        if (location == null) {
            return null;
        }
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("world", location.getWorld().getName());
        map.put("x", location.getX());
        map.put("y", location.getY());
        map.put("z", location.getZ());
        map.put("yaw", Float.valueOf(location.getYaw()));
        map.put("pitch", Float.valueOf(location.getPitch()));
        return map;
    }

    public static Location fromConfig(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        String worldName = section.getString("world");
        if (worldName == null) {
            return null;
        }
        World world = Bukkit.getWorld((String)worldName);
        if (world == null) {
            return null;
        }
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = (float)section.getDouble("yaw", 0.0);
        float pitch = (float)section.getDouble("pitch", 0.0);
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static Location center(Location location) {
        if (location == null) {
            return null;
        }
        return new Location(location.getWorld(), (double)location.getBlockX() + 0.5, (double)location.getBlockY(), (double)location.getBlockZ() + 0.5, location.getYaw(), location.getPitch());
    }

    public static double distance2D(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) {
            return Double.MAX_VALUE;
        }
        if (!loc1.getWorld().equals((Object)loc2.getWorld())) {
            return Double.MAX_VALUE;
        }
        double dx = loc1.getX() - loc2.getX();
        double dz = loc1.getZ() - loc2.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static boolean isWithinBounds(Location location, Location corner1, Location corner2) {
        if (location == null || corner1 == null || corner2 == null) {
            return false;
        }
        if (!location.getWorld().equals((Object)corner1.getWorld())) {
            return false;
        }
        double minX = Math.min(corner1.getX(), corner2.getX());
        double maxX = Math.max(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());
        return location.getX() >= minX && location.getX() <= maxX && location.getY() >= minY && location.getY() <= maxY && location.getZ() >= minZ && location.getZ() <= maxZ;
    }

    public static Location randomWithinBounds(Location corner1, Location corner2) {
        if (corner1 == null || corner2 == null) {
            return null;
        }
        double minX = Math.min(corner1.getX(), corner2.getX());
        double maxX = Math.max(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());
        double x = minX + Math.random() * (maxX - minX);
        double y = minY + Math.random() * (maxY - minY);
        double z = minZ + Math.random() * (maxZ - minZ);
        return new Location(corner1.getWorld(), x, y, z);
    }
}

