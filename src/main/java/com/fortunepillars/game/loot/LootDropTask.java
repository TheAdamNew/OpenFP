/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 *  org.bukkit.scheduler.BukkitRunnable
 */
package com.fortunepillars.game.loot;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.game.loot.LootMode;
import java.util.List;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class LootDropTask
extends BukkitRunnable {
    private final FortunePillars plugin;
    private final Arena arena;
    private final LootMode mode;
    private final boolean verbose;
    private int totalDrops;
    private int nextDropTicks;
    private boolean isRunning;

    public LootDropTask(FortunePillars plugin, Arena arena, LootMode mode) {
        this.plugin = plugin;
        this.arena = arena;
        this.mode = mode;
        this.totalDrops = 0;
        this.nextDropTicks = mode.getRandomIntervalTicks();
        this.isRunning = false;
        this.verbose = plugin.getConfigManager().getConfig().getBoolean("debug.log-loot-drops", false);
        if (this.verbose) {
            plugin.getLogger().info("Loot drop task created for " + arena.getName() + " | Mode: " + mode.name() + " | First drop in: " + this.nextDropTicks / 20 + "s");
        }
    }

    public void run() {
        if (!this.isRunning) {
            this.isRunning = true;
        }
        if (this.arena == null || !this.arena.isActive()) {
            this.cancel();
            return;
        }
        List<Player> alivePlayers = this.arena.getAlivePlayers();
        if (alivePlayers.isEmpty()) {
            this.cancel();
            return;
        }
        --this.nextDropTicks;
        if (this.nextDropTicks <= 0) {
            this.distributeLoot(alivePlayers);
            this.nextDropTicks = this.mode.getRandomIntervalTicks();
            ++this.totalDrops;
            if (this.verbose) {
                this.plugin.getLogger().info("Loot drop #" + this.totalDrops + " in " + this.arena.getName() + " | Next in " + this.nextDropTicks / 20 + "s");
            }
            return;
        }
        if (this.nextDropTicks == 200 || this.nextDropTicks == 100 || this.nextDropTicks == 60 || this.nextDropTicks == 40 || this.nextDropTicks == 20) {
            int secondsLeft = this.nextDropTicks / 20;
            if (this.plugin.getConfigManager().isChatNotificationEnabled("loot-drop-countdown")) {
                this.arena.broadcastMessage(FortunePillars.parseWithPrefix("<yellow>\u23f0 Next loot drop in <white>" + secondsLeft + "s</white>!"));
            }
            for (Player player : alivePlayers) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.3f, 1.0f);
            }
        }
    }

    private void distributeLoot(List<Player> alivePlayers) {
        if (alivePlayers.isEmpty()) {
            return;
        }
        int itemCount = this.mode.getRandomItemCount();
        if (this.plugin.getConfigManager().isChatNotificationEnabled("loot-drop")) {
            String styleEmoji = this.mode.getStyleEmoji();
            String tierColor = this.mode.getTierColor();
            this.arena.broadcastMessage(FortunePillars.parseWithPrefix(styleEmoji + " " + tierColor + "<bold>Loot Drop!</bold> <gray>(" + itemCount + " " + this.mode.getTier().name().toLowerCase() + " item" + (itemCount != 1 ? "s" : "") + ")"));
        }
        for (Player player : alivePlayers) {
            if (player == null || !player.isOnline()) continue;
            try {
                this.plugin.getLootManager().giveLootByTier(player, this.mode.getTier(), this.mode.getStyle(), itemCount);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.7f, 1.2f);
            }
            catch (Exception e) {
                this.plugin.getLogger().severe("Error giving loot to " + player.getName() + ": " + e.getMessage());
            }
        }
        if (this.verbose) {
            this.plugin.getLogger().info("Loot distributed to " + alivePlayers.size() + " players in " + this.arena.getName());
        }
    }

    public void stop() {
        if (this.isCancelled()) {
            return;
        }
        this.cancel();
        this.isRunning = false;
        if (this.verbose) {
            this.plugin.getLogger().info("Loot drop task stopped for " + this.arena.getName() + " | Total drops: " + this.totalDrops);
        }
    }

    public LootMode getMode() {
        return this.mode;
    }

    public int getTotalDrops() {
        return this.totalDrops;
    }

    public int getNextDropTicks() {
        return this.nextDropTicks;
    }

    public int getSecondsUntilNextDrop() {
        return this.nextDropTicks / 20;
    }

    public boolean isTaskRunning() {
        return this.isRunning && !this.isCancelled();
    }

    public void forceDropNow() {
        List<Player> alivePlayers = this.arena.getAlivePlayers();
        if (alivePlayers.isEmpty()) {
            return;
        }
        this.distributeLoot(alivePlayers);
        this.nextDropTicks = this.mode.getRandomIntervalTicks();
        ++this.totalDrops;
    }

    public String getDebugInfo() {
        return "LootDropTask{arena=" + this.arena.getName() + ", mode=" + this.mode.name() + ", drops=" + this.totalDrops + ", nextDrop=" + this.nextDropTicks / 20 + "s, running=" + this.isRunning + "}";
    }
}

