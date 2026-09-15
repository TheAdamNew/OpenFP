/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 */
package com.fortunepillars.api;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.arena.ArenaManager;
import com.fortunepillars.arena.GameState;
import com.fortunepillars.game.GameManager;
import com.fortunepillars.player.PlayerManager;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.entity.Player;

public class FortunePillarsAPI {
    private static FortunePillarsAPI instance;
    private final FortunePillars plugin;

    private FortunePillarsAPI(FortunePillars plugin) {
        this.plugin = plugin;
    }

    public static void init(FortunePillars plugin) {
        if (instance == null) {
            instance = new FortunePillarsAPI(plugin);
        }
    }

    public static FortunePillarsAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException("FortunePillarsAPI not initialized!");
        }
        return instance;
    }

    public static FortunePillars getPlugin() {
        return instance != null ? FortunePillarsAPI.instance.plugin : null;
    }

    public Arena getArena(String name) {
        return this.plugin.getArenaManager().getArena(name);
    }

    public Optional<Arena> findArena(String name) {
        return Optional.ofNullable(this.plugin.getArenaManager().getArena(name));
    }

    public Collection<Arena> getAllArenas() {
        return this.plugin.getArenaManager().getAllArenas();
    }

    public Collection<Arena> getArenasByState(GameState state) {
        return this.plugin.getArenaManager().getAllArenas().stream().filter(arena -> arena.getState() == state).toList();
    }

    public Arena getPlayerArena(Player player) {
        return this.plugin.getArenaManager().getPlayerArena(player);
    }

    public Optional<Arena> findPlayerArena(Player player) {
        return Optional.ofNullable(this.plugin.getArenaManager().getPlayerArena(player));
    }

    public Optional<Arena> findPlayerArena(UUID uuid) {
        return Optional.ofNullable(this.plugin.getArenaManager().getPlayerArena(uuid));
    }

    public boolean isPlayerInArena(Player player) {
        return this.plugin.getArenaManager().isInArena(player);
    }

    public boolean isInGame(Player player) {
        return this.plugin.getArenaManager().isInArena(player);
    }

    public boolean isInGame(UUID uuid) {
        return this.plugin.getArenaManager().getPlayerArena(uuid) != null;
    }

    public boolean isPlayerAlive(UUID uuid) {
        Arena arena = this.plugin.getArenaManager().getPlayerArena(uuid);
        return arena != null && arena.isPlayerAlive(uuid);
    }

    public boolean isAlive(Player player) {
        Arena arena = this.plugin.getArenaManager().getPlayerArena(player);
        return arena != null && arena.isPlayerAlive(player.getUniqueId());
    }

    public boolean isPlayerSpectating(Player player) {
        return this.plugin.getSpectatorManager().isSpectator(player);
    }

    public boolean isSpectating(Player player) {
        Arena arena = this.plugin.getArenaManager().getPlayerArena(player);
        return arena != null && arena.isSpectator(player.getUniqueId());
    }

    public PlayerManager.PlayerStats getPlayerStats(UUID uuid) {
        return this.plugin.getDatabase().getPlayerStats(uuid);
    }

    public PlayerManager.PlayerStats getPlayerStats(Player player) {
        return this.getPlayerStats(player.getUniqueId());
    }

    public int getPlayerWins(UUID uuid) {
        PlayerManager.PlayerStats stats = this.getPlayerStats(uuid);
        return stats != null ? stats.getWins() : 0;
    }

    public int getPlayerKills(UUID uuid) {
        PlayerManager.PlayerStats stats = this.getPlayerStats(uuid);
        return stats != null ? stats.getKills() : 0;
    }

    public int getPlayerDeaths(UUID uuid) {
        PlayerManager.PlayerStats stats = this.getPlayerStats(uuid);
        return stats != null ? stats.getDeaths() : 0;
    }

    public double getPlayerKDR(UUID uuid) {
        PlayerManager.PlayerStats stats = this.getPlayerStats(uuid);
        return stats != null ? stats.getKDR() : 0.0;
    }

    public boolean joinArena(Player player, String arenaName) {
        return this.plugin.getArenaManager().joinArena(player, arenaName);
    }

    public void leaveArena(Player player) {
        this.plugin.getArenaManager().leaveArena(player);
    }

    public boolean quickJoin(Player player) {
        return this.plugin.getArenaManager().quickJoin(player);
    }

    public int getTotalPlayersInGames() {
        return this.plugin.getArenaManager().getAllArenas().stream().mapToInt(Arena::getPlayerCount).sum();
    }

    public int getActiveGameCount() {
        return (int)this.plugin.getArenaManager().getAllArenas().stream().filter(arena -> arena.getState() == GameState.IN_GAME).count();
    }

    public ArenaManager getArenaManager() {
        return this.plugin.getArenaManager();
    }

    public GameManager getGameManager() {
        return this.plugin.getGameManager();
    }

    public PlayerManager getPlayerManager() {
        return this.plugin.getPlayerManager();
    }

    public FortunePillars getPluginInstance() {
        return this.plugin;
    }
}

