/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.Sound
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 */
package com.fortunepillars.utils;

import com.fortunepillars.FortunePillars;
import java.util.Collection;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class SoundUtil {
    private SoundUtil() {
    }

    public static void playClick(Player player) {
        SoundUtil.playConfigSound(player, "sounds.click");
    }

    public static void playSuccess(Player player) {
        SoundUtil.playConfigSound(player, "sounds.success");
    }

    public static void playError(Player player) {
        SoundUtil.playConfigSound(player, "sounds.error");
    }

    public static void playCountdown(Player player) {
        SoundUtil.playConfigSound(player, "sounds.countdown");
    }

    public static void playCountdownFinal(Player player) {
        SoundUtil.playConfigSound(player, "sounds.countdown-final");
    }

    public static void playGameStart(Player player) {
        SoundUtil.playConfigSound(player, "sounds.game-start");
    }

    public static void playElimination(Player player) {
        SoundUtil.playConfigSound(player, "sounds.elimination");
    }

    public static void playVictory(Player player) {
        SoundUtil.playConfigSound(player, "sounds.victory");
    }

    public static void playLootReceived(Player player) {
        SoundUtil.playConfigSound(player, "sounds.loot-received");
    }

    public static void playTeleport(Player player) {
        SoundUtil.playConfigSound(player, "sounds.teleport");
    }

    public static void playWarning(Player player) {
        SoundUtil.playConfigSound(player, "sounds.warning");
    }

    public static void playJoinArena(Player player) {
        SoundUtil.playConfigSound(player, "sounds.join-arena");
    }

    public static void playLeaveArena(Player player) {
        SoundUtil.playConfigSound(player, "sounds.leave-arena");
    }

    public static void playGameEnd(Player player) {
        SoundUtil.playConfigSound(player, "sounds.game-end");
    }

    public static void playCountdownStop(Player player) {
        SoundUtil.playConfigSound(player, "sounds.countdown-stop");
    }

    public static void playGracePeriodEnd(Player player) {
        SoundUtil.playConfigSound(player, "sounds.grace-period-end");
    }

    public static void playLootDrop(Player player) {
        SoundUtil.playConfigSound(player, "sounds.loot-drop");
    }

    public static void playSpectate(Player player) {
        SoundUtil.playConfigSound(player, "sounds.spectate");
    }

    public static void playVote(Player player) {
        SoundUtil.playConfigSound(player, "sounds.vote");
    }

    public static void playConfigSound(Player player, String configPath) {
        if (player == null) {
            return;
        }
        FortunePillars plugin = FortunePillars.getInstance();
        if (plugin == null) {
            return;
        }
        FileConfiguration config = plugin.getConfigManager().getConfig();
        if (!config.getBoolean("sounds.enabled", true)) {
            return;
        }
        if (!config.getBoolean(configPath + ".enabled", true)) {
            return;
        }
        String soundName = config.getString(configPath + ".sound");
        if (soundName == null || soundName.isEmpty() || soundName.equalsIgnoreCase("none")) {
            return;
        }
        float volume = (float)config.getDouble(configPath + ".volume", 1.0);
        float pitch = (float)config.getDouble(configPath + ".pitch", 1.0);
        try {
            Sound sound = Sound.valueOf((String)soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
        catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound '" + soundName + "' at config path '" + configPath + "'. Check https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html for valid sound names.");
        }
    }

    public static void playConfigSoundToAll(Collection<? extends Player> players, String configPath) {
        for (Player player : players) {
            SoundUtil.playConfigSound(player, configPath);
        }
    }

    public static void playConfigSoundAtLocation(Location location, String configPath) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        FortunePillars plugin = FortunePillars.getInstance();
        if (plugin == null) {
            return;
        }
        FileConfiguration config = plugin.getConfigManager().getConfig();
        if (!config.getBoolean("sounds.enabled", true)) {
            return;
        }
        if (!config.getBoolean(configPath + ".enabled", true)) {
            return;
        }
        String soundName = config.getString(configPath + ".sound");
        if (soundName == null || soundName.isEmpty() || soundName.equalsIgnoreCase("none")) {
            return;
        }
        float volume = (float)config.getDouble(configPath + ".volume", 1.0);
        float pitch = (float)config.getDouble(configPath + ".pitch", 1.0);
        try {
            Sound sound = Sound.valueOf((String)soundName.toUpperCase());
            location.getWorld().playSound(location, sound, volume, pitch);
        }
        catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound '" + soundName + "' at config path '" + configPath + "'.");
        }
    }

    public static void playRaw(Player player, Sound sound, float volume, float pitch) {
        if (player == null) {
            return;
        }
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    public static void playToAll(Collection<? extends Player> players, Sound sound, float volume, float pitch) {
        for (Player player : players) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    public static void playAtLocation(Location location, Sound sound, float volume, float pitch) {
        location.getWorld().playSound(location, sound, volume, pitch);
    }

    public static void playDelayed(Player player, Sound sound, float volume, float pitch, long delayTicks) {
        FortunePillars plugin = FortunePillars.getInstance();
        if (plugin == null) {
            return;
        }
        plugin.getServer().getScheduler().runTaskLater((Plugin)plugin, () -> player.playSound(player.getLocation(), sound, volume, pitch), delayTicks);
    }

    public static void playConfigSoundDelayed(Player player, String configPath, long delayTicks) {
        FortunePillars plugin = FortunePillars.getInstance();
        if (plugin == null) {
            return;
        }
        plugin.getServer().getScheduler().runTaskLater((Plugin)plugin, () -> SoundUtil.playConfigSound(player, configPath), delayTicks);
    }
}

