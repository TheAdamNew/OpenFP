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
package com.fortunepillars.game.loot;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class LootBossBar {
    private final FortunePillars plugin;
    private final Arena arena;
    private final Map<UUID, BossBar> playerBossBars;
    private BukkitTask updateTask;
    private int maxTime;
    private int currentTime;

    public LootBossBar(FortunePillars plugin, Arena arena, int intervalSeconds) {
        this.plugin = plugin;
        this.arena = arena;
        this.playerBossBars = new HashMap<UUID, BossBar>();
        this.maxTime = intervalSeconds;
        this.currentTime = 0;
    }

    public void start() {
        for (Player player : this.arena.getAlivePlayers()) {
            this.createBossBar(player);
        }
        this.updateTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            ++this.currentTime;
            if (this.currentTime >= this.maxTime * 20) {
                this.currentTime = 0;
            }
            this.updateBossBars();
        }, 0L, 1L);
    }

    public void stop() {
        if (this.updateTask != null) {
            this.updateTask.cancel();
            this.updateTask = null;
        }
        for (Map.Entry<UUID, BossBar> entry : this.playerBossBars.entrySet()) {
            Player player = Bukkit.getPlayer((UUID)entry.getKey());
            if (player == null) continue;
            player.hideBossBar(entry.getValue());
        }
        this.playerBossBars.clear();
    }

    private void createBossBar(Player player) {
        BossBar bossBar = BossBar.bossBar((Component)FortunePillars.parse("<gold>Next Loot Drop: <yellow>0s"), (float)1.0f, (BossBar.Color)BossBar.Color.YELLOW, (BossBar.Overlay)BossBar.Overlay.PROGRESS);
        player.showBossBar(bossBar);
        this.playerBossBars.put(player.getUniqueId(), bossBar);
    }

    private void updateBossBars() {
        float progress = (float)this.currentTime / (float)(this.maxTime * 20);
        int secondsLeft = this.maxTime - this.currentTime / 20;
        for (Map.Entry<UUID, BossBar> entry : this.playerBossBars.entrySet()) {
            BossBar bar = entry.getValue();
            bar.name(FortunePillars.parse("<gold>Next Loot Drop: <yellow>" + secondsLeft + "s"));
            bar.progress(1.0f - progress);
            if (secondsLeft <= 3) {
                bar.color(BossBar.Color.RED);
                continue;
            }
            if (secondsLeft <= 5) {
                bar.color(BossBar.Color.YELLOW);
                continue;
            }
            bar.color(BossBar.Color.GREEN);
        }
    }

    public void addPlayer(Player player) {
        this.createBossBar(player);
    }

    public void removePlayer(Player player) {
        BossBar bar = this.playerBossBars.remove(player.getUniqueId());
        if (bar != null) {
            player.hideBossBar(bar);
        }
    }

    public void reset() {
        this.currentTime = 0;
    }

    public void setInterval(int seconds) {
        this.maxTime = seconds;
        this.currentTime = 0;
    }
}

