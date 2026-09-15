/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 */
package com.fortunepillars.player;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.player.PlayerData;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PlayerManager {
    private final FortunePillars plugin;
    private final Map<UUID, PlayerStats> statsCache;

    public PlayerManager(FortunePillars plugin) {
        this.plugin = plugin;
        this.statsCache = new ConcurrentHashMap<UUID, PlayerStats>();
    }

    public void loadPlayerStats(Player player) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            PlayerStats stats = this.plugin.getDatabase().getPlayerStats(player.getUniqueId());
            if (stats != null) {
                this.statsCache.put(player.getUniqueId(), stats);
                this.plugin.getLogger().info("Loaded stats for " + player.getName() + ": " + String.valueOf(stats));
            } else {
                this.plugin.getLogger().warning("No stats found for " + player.getName() + ", creating...");
                this.plugin.getDatabase().createPlayerData(player.getUniqueId(), player.getName());
            }
        });
    }

    public void unloadPlayerStats(Player player) {
        this.statsCache.remove(player.getUniqueId());
    }

    public PlayerStats getPlayerStats(UUID uuid) {
        return this.statsCache.get(uuid);
    }

    public void updateStatsCache(UUID uuid, PlayerStats stats) {
        this.statsCache.put(uuid, stats);
    }

    public boolean isPlayerInGame(Player player) {
        return PlayerData.isInGame(player);
    }

    public boolean isPlayerSpectating(Player player) {
        return PlayerData.isSpectator(player);
    }

    public Arena getPlayerArena(Player player) {
        return this.plugin.getArenaManager().getPlayerArena(player);
    }

    public record PlayerStats(int wins, int kills, int deaths, int winstreak, int highestWinstreak, int gamesPlayed) {
        public int getWins() {
            return this.wins;
        }

        public int getKills() {
            return this.kills;
        }

        public int getDeaths() {
            return this.deaths;
        }

        public int getWinstreak() {
            return this.winstreak;
        }

        public int getHighestWinstreak() {
            return this.highestWinstreak;
        }

        public int getGamesPlayed() {
            return this.gamesPlayed;
        }

        public double getKDR() {
            return this.deaths == 0 ? (double)this.kills : (double)this.kills / (double)this.deaths;
        }

        public double getWinRate() {
            return this.gamesPlayed == 0 ? 0.0 : (double)this.wins / (double)this.gamesPlayed * 100.0;
        }

        @Override
        public String toString() {
            return "PlayerStats{wins=" + this.wins + ", kills=" + this.kills + ", deaths=" + this.deaths + ", winstreak=" + this.winstreak + ", gamesPlayed=" + this.gamesPlayed + "}";
        }
    }

    public record GameModeStats(String gameMode, int wins, int losses, int gamesPlayed, int winstreak, int highestWinstreak) {
        public String getGameMode() {
            return this.gameMode;
        }

        public int getWins() {
            return this.wins;
        }

        public int getLosses() {
            return this.losses;
        }

        public int getGamesPlayed() {
            return this.gamesPlayed;
        }

        public int getWinstreak() {
            return this.winstreak;
        }

        public int getHighestWinstreak() {
            return this.highestWinstreak;
        }

        public double getWinRate() {
            return this.gamesPlayed == 0 ? 0.0 : (double)this.wins / (double)this.gamesPlayed * 100.0;
        }

        public static GameModeStats empty(String gameMode) {
            return new GameModeStats(gameMode, 0, 0, 0, 0, 0);
        }

        @Override
        public String toString() {
            return "GameModeStats{gameMode=" + this.gameMode + ", wins=" + this.wins + ", losses=" + this.losses + ", gamesPlayed=" + this.gamesPlayed + ", winstreak=" + this.winstreak + ", highestWinstreak=" + this.highestWinstreak + "}";
        }
    }
}

