/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 */
package com.fortunepillars.scoreboard;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.scoreboard.GameScoreboard;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public class ScoreboardManager {
    private final FortunePillars plugin;
    private final Map<String, GameScoreboard> arenaScoreboards;
    private final Set<UUID> disabledPlayers;

    public ScoreboardManager(FortunePillars plugin) {
        this.plugin = plugin;
        this.arenaScoreboards = new ConcurrentHashMap<String, GameScoreboard>();
        this.disabledPlayers = ConcurrentHashMap.newKeySet();
    }

    public void startScoreboard(Arena arena) {
        String arenaName = arena.getName();
        if (this.arenaScoreboards.containsKey(arenaName)) {
            return;
        }
        GameScoreboard scoreboard = new GameScoreboard(this.plugin, arena);
        scoreboard.start();
        this.arenaScoreboards.put(arenaName, scoreboard);
    }

    public void stopScoreboard(Arena arena) {
        GameScoreboard scoreboard = this.arenaScoreboards.remove(arena.getName());
        if (scoreboard != null) {
            scoreboard.stop();
        }
    }

    public void addPlayer(Player player, Arena arena) {
        if (this.isScoreboardDisabled(player)) {
            return;
        }
        GameScoreboard scoreboard = this.arenaScoreboards.get(arena.getName());
        if (scoreboard != null) {
            scoreboard.addPlayer(player);
        }
    }

    public void removePlayer(Player player) {
        for (GameScoreboard scoreboard : this.arenaScoreboards.values()) {
            scoreboard.removePlayer(player);
        }
    }

    public boolean toggleScoreboard(Player player) {
        UUID uuid = player.getUniqueId();
        if (this.disabledPlayers.contains(uuid)) {
            this.disabledPlayers.remove(uuid);
            Arena arena = this.plugin.getArenaManager().getPlayerArena(player);
            if (arena != null) {
                this.addPlayer(player, arena);
            }
            return true;
        }
        this.disabledPlayers.add(uuid);
        this.removePlayer(player);
        return false;
    }

    public boolean isScoreboardDisabled(Player player) {
        return this.disabledPlayers.contains(player.getUniqueId());
    }

    public void shutdown() {
        for (GameScoreboard scoreboard : this.arenaScoreboards.values()) {
            scoreboard.stop();
        }
        this.arenaScoreboards.clear();
        this.disabledPlayers.clear();
    }
}

