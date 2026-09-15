/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.PlayerDeathEvent
 *  org.bukkit.event.player.PlayerRespawnEvent
 *  org.bukkit.persistence.PersistentDataContainer
 *  org.bukkit.plugin.Plugin
 */
package com.fortunepillars.listeners;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.arena.GameState;
import com.fortunepillars.player.PlayerData;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;

public class PlayerDeathListener
implements Listener {
    private final FortunePillars plugin;
    private final Map<UUID, UUID> pendingSpectators = new HashMap<UUID, UUID>();

    public PlayerDeathListener(FortunePillars plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getPlayer();
        Player killer = victim.getKiller();
        Arena arena = this.plugin.getArenaManager().getPlayerArena(victim);
        if (arena == null) {
            return;
        }
        if (arena.getState() != GameState.IN_GAME) {
            return;
        }
        event.getDrops().clear();
        event.setDroppedExp(0);
        event.deathMessage(null);
        if (arena.isPlayerAlive(victim.getUniqueId())) {
            this.pendingSpectators.put(victim.getUniqueId(), killer != null ? killer.getUniqueId() : null);
        }
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (victim.isDead()) {
                victim.spigot().respawn();
            }
        }, 2L);
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Arena arena = this.plugin.getArenaManager().getPlayerArena(player);
        if (arena == null) {
            this.pendingSpectators.remove(uuid);
            if (PlayerData.isInGame(player)) {
                this.plugin.getLogger().warning("Player " + player.getName() + " has orphaned FP data but is not in an arena. Clearing PDC.");
                this.cleanupOrphanedPDC(player);
            }
            return;
        }
        if (arena.getState() != GameState.IN_GAME) {
            this.pendingSpectators.remove(uuid);
            this.plugin.getLogger().info("Game ended before " + player.getName() + " respawned, sending to lobby...");
            Location lobby = this.plugin.getConfigManager().getLobbyLocation();
            if (lobby != null) {
                event.setRespawnLocation(lobby);
            }
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.forceCleanupPlayer(player, arena), 1L);
            return;
        }
        if (this.pendingSpectators.containsKey(uuid)) {
            UUID killerUUID = this.pendingSpectators.remove(uuid);
            Player killer = killerUUID != null ? Bukkit.getPlayer((UUID)killerUUID) : null;
            Location spectatorSpawn = arena.getSpectatorSpawn();
            if (spectatorSpawn != null) {
                event.setRespawnLocation(spectatorSpawn);
            } else if (arena.getCenter() != null) {
                event.setRespawnLocation(arena.getCenter().clone().add(0.0, 10.0, 0.0));
            }
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (!player.isOnline()) {
                    return;
                }
                if (arena.getState() == GameState.IN_GAME) {
                    this.plugin.getGameManager().handlePlayerDeath(player, killer);
                } else {
                    this.plugin.getLogger().info("Game ended while " + player.getName() + " was respawning, cleaning up...");
                    this.forceCleanupPlayer(player, arena);
                }
            }, 1L);
            return;
        }
        if (arena.isSpectator(uuid)) {
            Location spectatorSpawn = arena.getSpectatorSpawn();
            if (spectatorSpawn != null) {
                event.setRespawnLocation(spectatorSpawn);
            } else if (arena.getCenter() != null) {
                event.setRespawnLocation(arena.getCenter());
            }
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (arena.getState() == GameState.IN_GAME && arena.isSpectator(uuid)) {
                    player.setGameMode(GameMode.SPECTATOR);
                    player.setAllowFlight(true);
                    player.setFlying(true);
                }
            }, 1L);
        }
    }

    private void cleanupOrphanedPDC(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.remove(PlayerData.KEY_IN_GAME);
        pdc.remove(PlayerData.KEY_ARENA);
        pdc.remove(PlayerData.KEY_SPECTATOR);
        this.plugin.getLogger().info("Cleared orphaned FP PDC keys for " + player.getName());
    }

    private void forceCleanupPlayer(Player player, Arena arena) {
        this.pendingSpectators.remove(player.getUniqueId());
        this.plugin.getSpectatorManager().removeSpectator(player);
        this.plugin.getScoreboardManager().removePlayer(player);
        this.plugin.getArenaManager().setPlayerArena(player, null);
        this.cleanupOrphanedPDC(player);
        player.setGameMode(GameMode.SURVIVAL);
        Location lobby = this.plugin.getConfigManager().getLobbyLocation();
        if (lobby != null) {
            player.teleport(lobby);
        }
        this.plugin.getLogger().info("Force-cleaned FP state for: " + player.getName());
    }
}

