/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package com.fortunepillars.actionbar;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.arena.GameState;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class ActionBarManager {
    private final FortunePillars plugin;
    private final Map<UUID, BukkitTask> playerTasks;

    public ActionBarManager(FortunePillars plugin) {
        this.plugin = plugin;
        this.playerTasks = new HashMap<UUID, BukkitTask>();
    }

    public void startArenaActionBar(Player player, Arena arena) {
        this.stopPlayerActionBar(player);
        BukkitTask task = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            if (!player.isOnline()) {
                this.stopPlayerActionBar(player);
                return;
            }
            Arena currentArena = this.plugin.getArenaManager().getPlayerArena(player);
            if (currentArena == null || !currentArena.equals(arena)) {
                this.stopPlayerActionBar(player);
                return;
            }
            Component actionBar = this.buildActionBar(player, arena);
            player.sendActionBar(actionBar);
        }, 0L, 10L);
        this.playerTasks.put(player.getUniqueId(), task);
    }

    public void stopPlayerActionBar(Player player) {
        BukkitTask task = this.playerTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        player.sendActionBar((Component)Component.empty());
    }

    public void stopAllActionBars() {
        for (BukkitTask task : this.playerTasks.values()) {
            task.cancel();
        }
        this.playerTasks.clear();
    }

    private Component buildActionBar(Player player, Arena arena) {
        GameState state = arena.getState();
        return switch (state) {
            case GameState.WAITING -> FortunePillars.parse("<yellow>Waiting for players... </yellow><gray>[</gray><green>" + arena.getPlayerCount() + "</green><gray>/</gray><white>" + arena.getMaxPlayers() + "</white><gray>]</gray>");
            case GameState.STARTING -> FortunePillars.parse("<gold>Game starting soon! </gold><gray>Get ready!</gray>");
            case GameState.IN_GAME -> {
                if (arena.isSpectator(player.getUniqueId())) {
                    yield FortunePillars.parse("<gray>Spectating </gray><yellow>" + arena.getAliveCount() + "</yellow><gray> players remaining</gray>");
                }
                if (arena.isGracePeriodActive()) {
                    yield FortunePillars.parse("<aqua>\u2690 Grace Period Active \u2690</aqua> <gray>PvP disabled</gray>");
                }
                yield FortunePillars.parse("<red>\u2694 Fight! </red><gray>Players alive: </gray><green>" + arena.getAliveCount() + "</green>");
            }
            case GameState.ENDING -> FortunePillars.parse("<gold>\u2605 Game Over! \u2605</gold>");
            default -> Component.empty();
        };
    }

    public void sendTemporaryActionBar(Player player, String message, int durationTicks) {
        player.sendActionBar(FortunePillars.parse(message));
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            Arena arena = this.plugin.getArenaManager().getPlayerArena(player);
            if (arena != null) {
                player.sendActionBar(this.buildActionBar(player, arena));
            } else {
                player.sendActionBar((Component)Component.empty());
            }
        }, (long)durationTicks);
    }
}

