/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  org.bukkit.Bukkit
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package com.fortunepillars.leaderboard;

import com.fortunepillars.FortunePillars;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class LeaderboardManager {
    private final FortunePillars plugin;
    private final Map<String, Map<UUID, Integer>> leaderboards;
    private final Map<String, List<LeaderboardEntry>> cachedLeaderboards;
    private BukkitTask updateTask;
    private long lastUpdate = 0L;
    private static final int DEFAULT_LEADERBOARD_SIZE = 10;

    public LeaderboardManager(FortunePillars plugin) {
        this.plugin = plugin;
        this.leaderboards = new ConcurrentHashMap<String, Map<UUID, Integer>>();
        this.cachedLeaderboards = new ConcurrentHashMap<String, List<LeaderboardEntry>>();
        this.leaderboards.put("wins", new LinkedHashMap());
        this.leaderboards.put("kills", new LinkedHashMap());
        this.leaderboards.put("winstreak", new LinkedHashMap());
        this.startUpdateTask();
    }

    private void startUpdateTask() {
        this.updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously((Plugin)this.plugin, this::updateLeaderboards, 100L, 6000L);
    }

    public void updateLeaderboards() {
        try {
            Map<UUID, Integer> topWins = this.plugin.getDatabase().getTopPlayers("wins", 10);
            this.leaderboards.put("wins", topWins);
            this.cacheLeaderboard("wins", topWins);
            Map<UUID, Integer> topKills = this.plugin.getDatabase().getTopPlayers("kills", 10);
            this.leaderboards.put("kills", topKills);
            this.cacheLeaderboard("kills", topKills);
            Map<UUID, Integer> topWinstreak = this.plugin.getDatabase().getTopPlayers("highest_winstreak", 10);
            this.leaderboards.put("winstreak", topWinstreak);
            this.cacheLeaderboard("winstreak", topWinstreak);
            this.lastUpdate = System.currentTimeMillis();
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Failed to update leaderboards: " + e.getMessage());
        }
    }

    public void forceUpdateSync() {
        try {
            Map<UUID, Integer> topWins = this.plugin.getDatabase().getTopPlayers("wins", 10);
            Map<UUID, Integer> topKills = this.plugin.getDatabase().getTopPlayers("kills", 10);
            Map<UUID, Integer> topWinstreak = this.plugin.getDatabase().getTopPlayers("highest_winstreak", 10);
            this.leaderboards.put("wins", topWins);
            this.leaderboards.put("kills", topKills);
            this.leaderboards.put("winstreak", topWinstreak);
            this.cacheLeaderboard("wins", topWins);
            this.cacheLeaderboard("kills", topKills);
            this.cacheLeaderboard("winstreak", topWinstreak);
            this.lastUpdate = System.currentTimeMillis();
        }
        catch (Exception e) {
            this.plugin.getLogger().severe("Failed to force update leaderboards: " + e.getMessage());
        }
    }

    private void cacheLeaderboard(String type, Map<UUID, Integer> data) {
        ArrayList<LeaderboardEntry> entries = new ArrayList<LeaderboardEntry>();
        int position = 1;
        for (Map.Entry<UUID, Integer> entry : data.entrySet()) {
            String playerName = this.plugin.getDatabase().getPlayerName(entry.getKey());
            if (playerName == null || playerName.equals("Unknown")) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer((UUID)entry.getKey());
                playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : "Unknown";
            }
            entries.add(new LeaderboardEntry(position++, entry.getKey(), playerName, entry.getValue()));
        }
        this.cachedLeaderboards.put(type, entries);
    }

    public List<LeaderboardEntry> getLeaderboard(String type) {
        return this.cachedLeaderboards.getOrDefault(type.toLowerCase(), Collections.emptyList());
    }

    public List<LeaderboardEntry> getTopWins() {
        return this.getLeaderboard("wins");
    }

    public List<LeaderboardEntry> getTopKills() {
        return this.getLeaderboard("kills");
    }

    public List<LeaderboardEntry> getTopWinstreak() {
        return this.getLeaderboard("winstreak");
    }

    public int getPlayerPosition(UUID uuid, String type) {
        List<LeaderboardEntry> leaderboard = this.getLeaderboard(type);
        for (int i = 0; i < leaderboard.size(); ++i) {
            if (!leaderboard.get(i).uuid().equals(uuid)) continue;
            return i + 1;
        }
        return -1;
    }

    public Optional<LeaderboardEntry> getTopPlayer(String type) {
        List<LeaderboardEntry> entries = this.getLeaderboard(type);
        if (!entries.isEmpty()) {
            return Optional.of(entries.get(0));
        }
        return Optional.empty();
    }

    public void showLeaderboard(Player player, String type) {
        List<LeaderboardEntry> entries = this.getLeaderboard(type);
        String displayName = this.getDisplayName(type);
        player.sendMessage((Component)Component.empty());
        player.sendMessage(FortunePillars.parse("<gradient:#FFD700:#FFA500><bold>\u2550\u2550\u2550\u2550\u2550\u2550 " + displayName + " \u2550\u2550\u2550\u2550\u2550\u2550</bold></gradient>"));
        player.sendMessage((Component)Component.empty());
        if (entries.isEmpty()) {
            player.sendMessage(FortunePillars.parse("<gray>No data available yet. Play some games!"));
        } else {
            for (LeaderboardEntry entry : entries) {
                String medal = this.getMedal(entry.position());
                String rankFormat = this.getRankFormat(entry.position());
                player.sendMessage(FortunePillars.parse(rankFormat + medal + "<yellow>" + entry.playerName() + "</yellow> <dark_gray>-</dark_gray> <white>" + this.formatValue(entry.value(), type) + "</white>"));
            }
        }
        player.sendMessage((Component)Component.empty());
        int playerRank = this.getPlayerPosition(player.getUniqueId(), type);
        if (playerRank > 0) {
            player.sendMessage(FortunePillars.parse("<gray>Your rank: <yellow>#" + playerRank + "</yellow>"));
        } else {
            player.sendMessage(FortunePillars.parse("<gray>You are not ranked yet. Start playing to get ranked!"));
        }
        player.sendMessage((Component)Component.empty());
    }

    private String getDisplayName(String type) {
        return switch (type.toLowerCase()) {
            case "wins" -> "Top Wins";
            case "kills" -> "Top Kills";
            case "winstreak" -> "Highest Winstreak";
            default -> "Leaderboard";
        };
    }

    private String getMedal(int position) {
        return switch (position) {
            case 1 -> "\ud83e\udd47 ";
            case 2 -> "\ud83e\udd48 ";
            case 3 -> "\ud83e\udd49 ";
            default -> "#" + position + " ";
        };
    }

    private String getRankFormat(int position) {
        return switch (position) {
            case 1 -> "<gold><bold>";
            case 2 -> "<gray>";
            case 3 -> "<#CD7F32>";
            default -> "<white>";
        };
    }

    private String formatValue(int value, String type) {
        return switch (type.toLowerCase()) {
            case "wins" -> value + " win" + (value != 1 ? "s" : "");
            case "kills" -> value + " kill" + (value != 1 ? "s" : "");
            case "winstreak" -> value + " streak";
            default -> String.valueOf(value);
        };
    }

    public void forceUpdate() {
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, this::updateLeaderboards);
    }

    public long getTimeSinceLastUpdate() {
        return System.currentTimeMillis() - this.lastUpdate;
    }

    public boolean isLoaded() {
        return this.lastUpdate > 0L;
    }

    public void shutdown() {
        if (this.updateTask != null) {
            this.updateTask.cancel();
        }
        this.cachedLeaderboards.clear();
        this.leaderboards.clear();
    }

    public record LeaderboardEntry(int position, UUID uuid, String playerName, int value) {
        public UUID playerUUID() {
            return this.uuid;
        }

        public boolean isPlayer(UUID playerUuid) {
            return this.uuid.equals(playerUuid);
        }
    }
}

