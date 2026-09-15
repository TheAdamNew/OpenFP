/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 *  org.bukkit.scoreboard.Criteria
 *  org.bukkit.scoreboard.DisplaySlot
 *  org.bukkit.scoreboard.Objective
 *  org.bukkit.scoreboard.Scoreboard
 *  org.bukkit.scoreboard.ScoreboardManager
 */
package com.fortunepillars.scoreboard;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.arena.GameState;
import com.fortunepillars.player.PlayerData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class GameScoreboard {
    private final FortunePillars plugin;
    private final Arena arena;
    private final Map<UUID, Scoreboard> playerScoreboards;
    private BukkitTask updateTask;
    private int gameTime = 0;

    public GameScoreboard(FortunePillars plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
        this.playerScoreboards = new HashMap<UUID, Scoreboard>();
    }

    public void start() {
        if (!this.plugin.getConfigManager().isScoreboardEnabled()) {
            return;
        }
        for (Player player : this.arena.getOnlinePlayers()) {
            this.createScoreboard(player);
        }
        long updateInterval = this.plugin.getConfigManager().getScoreboardUpdateInterval();
        this.updateTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            ++this.gameTime;
            this.update();
        }, updateInterval, updateInterval);
    }

    public void stop() {
        if (this.updateTask != null) {
            this.updateTask.cancel();
            this.updateTask = null;
        }
        for (Player player : this.arena.getOnlinePlayers()) {
            this.removeScoreboard(player);
        }
        this.playerScoreboards.clear();
        this.gameTime = 0;
    }

    public void createScoreboard(Player player) {
        if (!this.plugin.getConfigManager().isScoreboardEnabled()) {
            return;
        }
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) {
            return;
        }
        Scoreboard scoreboard = manager.getNewScoreboard();
        String title = this.plugin.getConfigManager().getScoreboardTitle();
        Objective objective = scoreboard.registerNewObjective("fp_game", Criteria.DUMMY, FortunePillars.parse(title));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.playerScoreboards.put(player.getUniqueId(), scoreboard);
        player.setScoreboard(scoreboard);
        this.updateScoreboard(player);
    }

    public void removeScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager != null) {
            player.setScoreboard(manager.getMainScoreboard());
        }
        this.playerScoreboards.remove(player.getUniqueId());
    }

    public void update() {
        for (Player player : this.arena.getOnlinePlayers()) {
            this.updateScoreboard(player);
        }
    }

    private void updateScoreboard(Player player) {
        Scoreboard scoreboard = this.playerScoreboards.get(player.getUniqueId());
        if (scoreboard == null) {
            return;
        }
        Objective objective = scoreboard.getObjective("fp_game");
        if (objective == null) {
            return;
        }
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }
        List<String> configLines = this.plugin.getConfigManager().getScoreboardLines();
        int lineNumber = configLines.size();
        for (String line : configLines) {
            String processedLine = this.replacePlaceholders(line, player);
            if (line.contains("{spectator_text}") && !this.arena.isSpectator(player.getUniqueId())) {
                --lineNumber;
                continue;
            }
            if (line.contains("{kills}") && this.arena.isSpectator(player.getUniqueId())) {
                --lineNumber;
                continue;
            }
            this.setLine(objective, lineNumber--, processedLine);
        }
    }

    private String replacePlaceholders(String line, Player player) {
        GameState state = this.arena.getState();
        PlayerData playerData = this.arena.getPlayerData(player.getUniqueId());
        int mins = this.gameTime / 60;
        int secs = this.gameTime % 60;
        String timeStr = String.format("%02d:%02d", mins, secs);
        String spectatorText = this.arena.isSpectator(player.getUniqueId()) ? "<gray><italic>\ud83d\udc41 Spectating</italic>" : "";
        int kills = playerData != null && !playerData.isSpectator() ? playerData.getKills() : 0;
        return line.replace("{arena}", this.arena.getName()).replace("{state}", state.getColoredName()).replace("{mode}", this.arena.getGameMode().getDisplayName()).replace("{time}", timeStr).replace("{alive}", String.valueOf(this.arena.getAliveCount())).replace("{spectators}", String.valueOf(this.arena.getSpectators().size())).replace("{total}", String.valueOf(this.arena.getPlayerCount())).replace("{kills}", String.valueOf(kills)).replace("{spectator_text}", spectatorText);
    }

    private void setLine(Objective objective, int score, String text) {
        Component component = FortunePillars.parse(text);
        String legacyText = LegacyComponentSerializer.legacySection().serialize(component);
        String uniqueText = legacyText + this.getUniqueChars(score);
        objective.getScore(uniqueText).setScore(score);
    }

    private String getUniqueChars(int score) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < score; ++i) {
            sb.append("\u00a7r");
        }
        return sb.toString();
    }

    public void addPlayer(Player player) {
        this.createScoreboard(player);
    }

    public void removePlayer(Player player) {
        this.removeScoreboard(player);
    }

    public int getGameTime() {
        return this.gameTime;
    }

    public Arena getArena() {
        return this.arena;
    }
}

