/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.minimessage.MiniMessage
 *  net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 */
package com.fortunepillars.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BedrockCompat {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static Boolean isFloodgatePresent = null;

    public static boolean isFloodgateInstalled() {
        if (isFloodgatePresent == null) {
            isFloodgatePresent = Bukkit.getPluginManager().getPlugin("floodgate") != null;
        }
        return isFloodgatePresent;
    }

    public static String toLegacy(String miniMessage) {
        if (miniMessage == null || miniMessage.isEmpty()) {
            return "";
        }
        try {
            Component component = MINI_MESSAGE.deserialize(miniMessage);
            return LEGACY.serialize(component);
        }
        catch (Exception e) {
            return miniMessage;
        }
    }

    public static boolean isBedrockPlayer(Player player) {
        if (!BedrockCompat.isFloodgateInstalled()) {
            return false;
        }
        try {
            return player.getUniqueId().getMostSignificantBits() == 0L;
        }
        catch (Exception e) {
            return false;
        }
    }

    public static String formatForPlayer(Player player, String miniMessage) {
        if (BedrockCompat.isBedrockPlayer(player)) {
            return BedrockCompat.toLegacy(miniMessage);
        }
        return miniMessage;
    }
}

