/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.bossbar.BossBar
 *  net.kyori.adventure.bossbar.BossBar$Color
 *  net.kyori.adventure.bossbar.BossBar$Overlay
 *  net.kyori.adventure.text.Component
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package com.fortunepillars.bossbar;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.arena.GameState;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class GameBossBar {
    private final FortunePillars plugin;
    private final Arena arena;
    private final BossBar bossBar;
    private BukkitTask updateTask;
    private final Set<UUID> viewers;

    public GameBossBar(FortunePillars plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
        this.viewers = new HashSet<UUID>();
        this.bossBar = BossBar.bossBar((Component)Component.empty(), (float)1.0f, (BossBar.Color)BossBar.Color.YELLOW, (BossBar.Overlay)BossBar.Overlay.PROGRESS);
    }

    public void start() {
        for (Player player : this.arena.getOnlinePlayers()) {
            this.addPlayer(player);
        }
        this.updateTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, this::update, 0L, 20L);
    }

    public void stop() {
        if (this.updateTask != null) {
            this.updateTask.cancel();
            this.updateTask = null;
        }
        for (UUID uuid : new HashSet<UUID>(this.viewers)) {
            Player player = Bukkit.getPlayer((UUID)uuid);
            if (player == null) continue;
            this.removePlayer(player);
        }
        this.viewers.clear();
    }

    public void addPlayer(Player player) {
        if (this.viewers.add(player.getUniqueId())) {
            player.showBossBar(this.bossBar);
        }
    }

    public void removePlayer(Player player) {
        if (this.viewers.remove(player.getUniqueId())) {
            player.hideBossBar(this.bossBar);
        }
    }

    private void update() {
        GameState state = this.arena.getState();
        switch (state) {
            case WAITING: {
                this.updateWaiting();
                break;
            }
            case STARTING: {
                this.updateStarting();
                break;
            }
            case IN_GAME: {
                this.updateInGame();
                break;
            }
            case ENDING: {
                this.updateEnding();
                break;
            }
        }
    }

    private void updateWaiting() {
        int current = this.arena.getPlayerCount();
        int min = this.arena.getMinPlayers();
        int max = this.arena.getMaxPlayers();
        float progress = Math.min(1.0f, (float)current / (float)min);
        this.bossBar.name(FortunePillars.parse("<yellow>Waiting for players </yellow><gray>[</gray><white>" + current + "</white><gray>/</gray><green>" + min + "</green><gray>]</gray>"));
        this.bossBar.progress(progress);
        this.bossBar.color(current >= min ? BossBar.Color.GREEN : BossBar.Color.YELLOW);
    }

    private void updateStarting() {
        this.bossBar.name(FortunePillars.parse("<gold><bold>Game Starting Soon!</bold></gold>"));
        this.bossBar.progress(1.0f);
        this.bossBar.color(BossBar.Color.YELLOW);
    }

    private void updateInGame() {
        int alive = this.arena.getAliveCount();
        int total = this.arena.getPlayerCount();
        float progress = total > 0 ? (float)alive / (float)total : 0.0f;
        String modeInfo = "";
        if (this.arena.isGracePeriodActive()) {
            modeInfo = " <aqua>\u2690 Grace Period</aqua>";
        }
        this.bossBar.name(FortunePillars.parse("<red>\u2694 " + this.arena.getGameMode().getDisplayName() + " \u2694</red> <gray>Alive: </gray><green>" + alive + "</green>" + modeInfo));
        this.bossBar.progress(progress);
        this.bossBar.color(alive <= 3 ? BossBar.Color.RED : BossBar.Color.GREEN);
    }

    private void updateEnding() {
        this.bossBar.name(FortunePillars.parse("<gold><bold>\u2605 Game Over! \u2605</bold></gold>"));
        this.bossBar.progress(1.0f);
        this.bossBar.color(BossBar.Color.PURPLE);
    }

    public BossBar getBossBar() {
        return this.bossBar;
    }
}

