/*
 * Decompiled with CFR 0.152.
 */
package com.fortunepillars.database;

import com.fortunepillars.player.PlayerManager;
import java.util.Map;
import java.util.UUID;

public interface Database {
    public void initialize();

    public void close();

    public PlayerManager.PlayerStats getPlayerStats(UUID var1);

    public void createPlayerData(UUID var1);

    public void createPlayerData(UUID var1, String var2);

    public void updatePlayerName(UUID var1, String var2);

    public String getPlayerName(UUID var1);

    public Map<UUID, Integer> getTopPlayers(String var1, int var2);

    public PlayerManager.PlayerStats[] getTopWins(int var1);

    public PlayerManager.PlayerStats[] getTopKills(int var1);

    public PlayerManager.PlayerStats[] getTopWinstreak(int var1);

    public void addWin(UUID var1);

    public void addKill(UUID var1);

    public void addDeath(UUID var1);

    public void addWinstreak(UUID var1);

    public void resetWinstreak(UUID var1);

    public void addGamePlayed(UUID var1);

    public PlayerManager.GameModeStats getGameModeStats(UUID var1, String var2);

    public void addGameModeWin(UUID var1, String var2);

    public void addGameModeLoss(UUID var1, String var2);

    public void addGameModePlayed(UUID var1, String var2);

    public void addGameModeWinstreak(UUID var1, String var2);

    public void resetGameModeWinstreak(UUID var1, String var2);

    public String getPlayerCageColor(UUID var1);

    public void setPlayerCageColor(UUID var1, String var2);

    public String getPlayerWinEffect(UUID var1);

    public void setPlayerWinEffect(UUID var1, String var2);

    public String getPlayerCosmetic(UUID var1, String var2);

    public void setPlayerCosmetic(UUID var1, String var2, String var3);
}

