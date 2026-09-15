/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.title.Title
 *  net.kyori.adventure.title.Title$Times
 *  org.bukkit.Bukkit
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.potion.PotionEffect
 */
package com.fortunepillars.player;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.player.PlayerData;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;

public class SpectatorManager {
    private final FortunePillars plugin;
    private final Set<UUID> spectators;
    private final Map<UUID, GameMode> previousGameModes;
    private final Map<UUID, Location> previousLocations;

    public SpectatorManager(FortunePillars plugin) {
        this.plugin = plugin;
        this.spectators = new HashSet<UUID>();
        this.previousGameModes = new HashMap<UUID, GameMode>();
        this.previousLocations = new HashMap<UUID, Location>();
    }

    public void makeSpectator(Player player, Arena arena) {
        PlayerData playerData;
        if (player == null) {
            return;
        }
        UUID uuid = player.getUniqueId();
        if (this.spectators.contains(uuid)) {
            if (player.getGameMode() != GameMode.SPECTATOR) {
                player.setGameMode(GameMode.SPECTATOR);
                player.setAllowFlight(true);
                player.setFlying(true);
            }
            return;
        }
        this.previousGameModes.put(uuid, player.getGameMode());
        this.previousLocations.put(uuid, player.getLocation().clone());
        this.spectators.add(uuid);
        if (arena != null && (playerData = arena.getPlayerData(uuid)) != null) {
            playerData.setAsSpectator(player);
        }
        player.getInventory().clear();
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.setGameMode(GameMode.SPECTATOR);
        player.setAllowFlight(true);
        player.setFlying(true);
        if (arena != null) {
            Location spectatorSpawn = arena.getSpectatorSpawn();
            if (spectatorSpawn != null) {
                player.teleport(spectatorSpawn);
            } else if (arena.getCenter() != null) {
                player.teleport(arena.getCenter().clone().add(0.0, 10.0, 0.0));
            }
        }
        for (Player other : this.plugin.getServer().getOnlinePlayers()) {
            if (this.spectators.contains(other.getUniqueId())) continue;
            other.hidePlayer((Plugin)this.plugin, player);
        }
        for (UUID spectatorUUID : this.spectators) {
            Player spectator = this.plugin.getServer().getPlayer(spectatorUUID);
            if (spectator == null || spectator == player) continue;
            player.showPlayer((Plugin)this.plugin, spectator);
            spectator.showPlayer((Plugin)this.plugin, player);
        }
        Title deathTitle = Title.title((Component)FortunePillars.parse("<red><bold>YOU DIED!</bold></red>"), (Component)FortunePillars.parse("<gray>You are now spectating"), (Title.Times)Title.Times.times((Duration)Duration.ZERO, (Duration)Duration.ofSeconds(3L), (Duration)Duration.ofSeconds(1L)));
        player.showTitle(deathTitle);
        player.sendMessage(this.plugin.getMessages().getWithPrefix("now-spectating"));
        player.sendMessage(FortunePillars.parseWithPrefix("<gray>Use <yellow>/tof leave</yellow> to return to lobby."));
        this.plugin.getLogger().info("Made " + player.getName() + " a spectator");
    }

    public void removeSpectator(Player player) {
        if (player == null) {
            return;
        }
        UUID uuid = player.getUniqueId();
        if (!this.spectators.contains(uuid)) {
            return;
        }
        this.plugin.getLogger().info("Removing spectator status from " + player.getName());
        this.spectators.remove(uuid);
        this.plugin.getScoreboardManager().removePlayer(player);
        GameMode previousMode = this.previousGameModes.remove(uuid);
        player.setGameMode(previousMode != null ? previousMode : GameMode.SURVIVAL);
        player.setAllowFlight(false);
        player.setFlying(false);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        player.getInventory().clear();
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        player.setFireTicks(0);
        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
            for (Player other : this.plugin.getServer().getOnlinePlayers()) {
                other.showPlayer((Plugin)this.plugin, player);
                player.showPlayer((Plugin)this.plugin, other);
            }
            this.plugin.getLogger().info("Made " + player.getName() + " visible to all players");
        });
        PlayerData.clearSpectatorData(player);
        this.previousLocations.remove(uuid);
        this.plugin.getLogger().info("\u2713 Removed spectator status from " + player.getName());
    }

    public void showSpectatorToAll(Player spectator) {
        for (Player online : this.plugin.getServer().getOnlinePlayers()) {
            online.showPlayer((Plugin)this.plugin, spectator);
        }
    }

    public void sendToLobby(Player player) {
        if (player == null) {
            return;
        }
        this.plugin.getScoreboardManager().removePlayer(player);
        this.removeSpectator(player);
        Location lobby = this.plugin.getConfigManager().getLobbyLocation();
        if (lobby != null) {
            player.teleport(lobby);
        } else {
            player.teleport(player.getWorld().getSpawnLocation());
        }
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();
        player.sendMessage(FortunePillars.parseWithPrefix("<green>You have been returned to the lobby."));
    }

    public boolean isSpectator(Player player) {
        return player != null && this.spectators.contains(player.getUniqueId());
    }

    public boolean isSpectator(UUID uuid) {
        return uuid != null && this.spectators.contains(uuid);
    }

    public Set<UUID> getSpectators() {
        return Collections.unmodifiableSet(this.spectators);
    }

    public Set<UUID> getAllSpectators() {
        return new HashSet<UUID>(this.spectators);
    }

    public int getSpectatorCount() {
        return this.spectators.size();
    }

    public void handleSpectatorInteraction(Player player) {
    }

    public void cleanupSpectators(Arena arena) {
        if (arena == null) {
            return;
        }
        for (Player spectator : arena.getSpectators()) {
            this.removeSpectator(spectator);
        }
    }

    public void clearArenaSpectators(Arena arena) {
        if (arena == null) {
            return;
        }
        for (UUID uuid : new HashSet<UUID>(this.spectators)) {
            Player player = this.plugin.getServer().getPlayer(uuid);
            if (player == null || !arena.containsPlayer(uuid)) continue;
            this.sendToLobby(player);
        }
    }

    public void shutdown() {
        for (UUID uuid : new HashSet<UUID>(this.spectators)) {
            Player player = this.plugin.getServer().getPlayer(uuid);
            if (player == null) continue;
            this.removeSpectator(player);
        }
        this.spectators.clear();
        this.previousGameModes.clear();
        this.previousLocations.clear();
    }
}

