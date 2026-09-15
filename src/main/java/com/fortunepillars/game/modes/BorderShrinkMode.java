/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.WorldBorder
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package com.fortunepillars.game.modes;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class BorderShrinkMode {
    private final FortunePillars plugin;
    private final Arena arena;
    private WorldBorder border;
    private Location originalCenter;
    private double originalSize;
    private final List<BukkitTask> warningTasks;

    public BorderShrinkMode(FortunePillars plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
        this.warningTasks = new ArrayList<BukkitTask>();
    }

    public void start() {
        this.plugin.getLogger().info("Starting Border Shrink mode for arena: " + this.arena.getName());
        Location center = this.arena.getCenter();
        if (center == null) {
            this.plugin.getLogger().warning("Cannot start Border Shrink - arena center is null!");
            return;
        }
        if (center.getWorld() == null) {
            this.plugin.getLogger().warning("Cannot start Border Shrink - world is null!");
            return;
        }
        try {
            this.border = center.getWorld().getWorldBorder();
            this.originalCenter = this.border.getCenter().clone();
            this.originalSize = this.border.getSize();
            this.border.setCenter(center);
            Location pos1 = this.arena.getPos1();
            Location pos2 = this.arena.getPos2();
            if (pos1 == null || pos2 == null) {
                this.plugin.getLogger().warning("Cannot start Border Shrink - arena positions not set!");
                return;
            }
            double arenaSize = Math.max(Math.abs(pos1.getX() - pos2.getX()), Math.abs(pos1.getZ() - pos2.getZ()));
            double initialSize = arenaSize + 20.0;
            double finalSize = 20.0;
            long shrinkTime = 300L;
            this.border.setSize(initialSize);
            this.plugin.getLogger().info("Border Shrink setup - Initial: " + initialSize + ", Final: " + finalSize + ", Time: " + shrinkTime + "s");
            this.arena.broadcastMessage(FortunePillars.parseWithPrefix("<red><bold>\u26a0 BORDER SHRINKING MODE! \u26a0</bold></red>"));
            this.arena.broadcastMessage(FortunePillars.parse("<gray>Border will shrink from <yellow>" + (int)initialSize + "</yellow> to <red>" + (int)finalSize + "</red> blocks over <yellow>" + shrinkTime / 60L + " minutes</yellow>!"));
            this.border.setSize(finalSize, shrinkTime);
            this.scheduleWarnings();
            this.plugin.getLogger().info("Border Shrink mode started successfully for " + this.arena.getName());
        }
        catch (Exception e) {
            this.plugin.getLogger().severe("Failed to start Border Shrink mode: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void scheduleWarnings() {
        BukkitTask task1 = Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.arena.broadcastMessage(FortunePillars.parse("<yellow>\u26a0 Border shrinking! 4 minutes remaining!")), 1200L);
        this.warningTasks.add(task1);
        BukkitTask task2 = Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.arena.broadcastMessage(FortunePillars.parse("<yellow>\u26a0 Border shrinking! 3 minutes remaining!")), 2400L);
        this.warningTasks.add(task2);
        BukkitTask task3 = Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.arena.broadcastMessage(FortunePillars.parse("<gold>\u26a0 Border shrinking! 2 minutes remaining!")), 3600L);
        this.warningTasks.add(task3);
        BukkitTask task4 = Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.arena.broadcastMessage(FortunePillars.parse("<red>\u26a0 Border shrinking! 1 minute remaining!")), 4800L);
        this.warningTasks.add(task4);
        BukkitTask task5 = Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.arena.broadcastMessage(FortunePillars.parse("<dark_red><bold>\u26a0 Border at minimum! 30 seconds!</bold></dark_red>")), 5400L);
        this.warningTasks.add(task5);
        BukkitTask task6 = Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.arena.broadcastMessage(FortunePillars.parse("<dark_red><bold>\u26a0 BORDER FULLY CLOSED! 10 SECONDS!</bold></dark_red>")), 5800L);
        this.warningTasks.add(task6);
        this.plugin.getLogger().info("Scheduled " + this.warningTasks.size() + " border warning messages");
    }

    public void stop() {
        this.plugin.getLogger().info("Stopping Border Shrink mode for arena: " + this.arena.getName());
        for (BukkitTask task : this.warningTasks) {
            if (task == null) continue;
            task.cancel();
        }
        this.warningTasks.clear();
        if (this.border != null && this.originalCenter != null) {
            try {
                this.border.setCenter(this.originalCenter);
                this.border.setSize(this.originalSize);
                this.plugin.getLogger().info("Border restored to original state");
            }
            catch (Exception e) {
                this.plugin.getLogger().warning("Failed to restore border: " + e.getMessage());
            }
        }
    }
}

