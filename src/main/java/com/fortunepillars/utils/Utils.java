/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.format.TextDecoration
 *  net.kyori.adventure.text.minimessage.MiniMessage
 *  org.bukkit.Location
 *  org.bukkit.entity.Player
 */
package com.fortunepillars.utils;

import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Utils {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private Utils() {
    }

    public static Component parse(String message) {
        return MINI_MESSAGE.deserialize(message).decoration(TextDecoration.ITALIC, false);
    }

    public static int randomInt(int min, int max) {
        if (min >= max) {
            return min;
        }
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static double randomDouble(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    public static String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        if (minutes > 0) {
            return String.format("%d:%02d", minutes, secs);
        }
        return String.format("%d", secs);
    }

    public static String formatLocation(Location loc) {
        return String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ());
    }

    public static boolean isLocationSafe(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        return location.getBlock().isPassable() && location.clone().add(0.0, 1.0, 0.0).getBlock().isPassable();
    }

    public static Location getCenterLocation(Location loc) {
        return new Location(loc.getWorld(), (double)loc.getBlockX() + 0.5, (double)loc.getBlockY(), (double)loc.getBlockZ() + 0.5, loc.getYaw(), loc.getPitch());
    }

    public static void sendActionBar(Player player, String message) {
        player.sendActionBar(Utils.parse(message));
    }

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public static String formatMaterial(String material) {
        return material.replace("_", " ").toLowerCase();
    }
}

