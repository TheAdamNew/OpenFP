/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package com.fortunepillars.game;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.arena.GameState;
import com.fortunepillars.game.GameModeManager;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class GameManager {
    private final FortunePillars plugin;
    private final Map<String, BukkitTask> arenaTimers;
    private final GameModeManager gameModeManager;

    public GameManager(FortunePillars plugin) {
        this.plugin = plugin;
        this.arenaTimers = new ConcurrentHashMap<String, BukkitTask>();
        this.gameModeManager = new GameModeManager(plugin);
    }

    public GameModeManager getGameModeManager() {
        return this.gameModeManager;
    }

    public void startGame(Arena arena) {
        if (arena.getState() != GameState.WAITING && arena.getState() != GameState.STARTING) {
            return;
        }
        arena.startGame();
    }

    public void endGame(Arena arena, Player winner) {
        arena.setState(GameState.ENDING);
        if (winner != null) {
            arena.broadcastMessage(FortunePillars.parseWithPrefix("<gold><bold>VICTORY!</bold></gold> <yellow>" + winner.getName() + "</yellow> <gray>has won the game!"));
            this.plugin.getDatabase().addWin(winner.getUniqueId());
        }
        this.plugin.getScoreboardManager().stopScoreboard(arena);
        this.plugin.getGameModeManager().stopGameModeTasks(arena);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            for (Player player : new ArrayList<Player>(arena.getOnlinePlayers())) {
                this.plugin.getSpectatorManager().removeSpectator(player);
                arena.removePlayer(player, true);
            }
            this.plugin.getSpectatorManager().clearArenaSpectators(arena);
            this.plugin.getSchematicManager().restoreArena(arena, () -> {
                arena.setState(GameState.WAITING);
                this.plugin.getLogger().info("Arena " + arena.getName() + " has been reset.");
            });
        }, 100L);
    }

    public void forceEndGame(Arena arena) {
        arena.forceEnd();
    }

    public void endAllGames() {
        for (Arena arena : this.plugin.getArenaManager().getAllArenas()) {
            if (arena.getState() != GameState.IN_GAME && arena.getState() != GameState.STARTING) continue;
            arena.forceEnd();
        }
    }

    public void handlePlayerDeath(Player player, Player killer) {
        Arena arena = this.plugin.getArenaManager().getPlayerArena(player);
        if (arena == null) {
            return;
        }
        if (arena.getState() != GameState.IN_GAME) {
            return;
        }
        arena.eliminatePlayer(player, killer);
    }

    public void handlePlayerDisconnect(Player player) {
        Arena arena = this.plugin.getArenaManager().getPlayerArena(player);
        if (arena == null) {
            return;
        }
        if (arena.getState() == GameState.IN_GAME && arena.isPlayerAlive(player.getUniqueId())) {
            arena.eliminatePlayer(player, null);
        }
        this.plugin.getArenaManager().handlePlayerQuit(player);
    }

    public boolean isPlayerInGame(UUID uuid) {
        Arena arena = this.plugin.getArenaManager().getPlayerArena(uuid);
        return arena != null && arena.getState() == GameState.IN_GAME;
    }

    public boolean isPlayerAlive(UUID uuid) {
        Arena arena = this.plugin.getArenaManager().getPlayerArena(uuid);
        return arena != null && arena.isPlayerAlive(uuid);
    }

    public boolean canPlayerTakeDamage(Player player) {
        Arena arena = this.plugin.getArenaManager().getPlayerArena(player);
        if (arena == null) {
            return true;
        }
        if (!arena.getState().allowsPlayerDamage()) {
            return false;
        }
        if (arena.isGracePeriodActive()) {
            return false;
        }
        return !arena.isSpectator(player.getUniqueId());
    }

    public boolean canPlayerPvP(Player attacker, Player victim) {
        Arena arena = this.plugin.getArenaManager().getPlayerArena(attacker);
        if (arena == null) {
            return true;
        }
        Arena victimArena = this.plugin.getArenaManager().getPlayerArena(victim);
        if (victimArena == null || !arena.getName().equals(victimArena.getName())) {
            return false;
        }
        if (arena.getState() != GameState.IN_GAME) {
            return false;
        }
        if (arena.isGracePeriodActive()) {
            return false;
        }
        return arena.isPlayerAlive(attacker.getUniqueId()) && arena.isPlayerAlive(victim.getUniqueId());
    }

    public void shutdown() {
        this.endAllGames();
        this.arenaTimers.values().forEach(BukkitTask::cancel);
        this.arenaTimers.clear();
    }
}

