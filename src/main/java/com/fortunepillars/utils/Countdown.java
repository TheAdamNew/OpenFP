/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package com.fortunepillars.utils;

import com.fortunepillars.FortunePillars;
import java.util.function.IntConsumer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class Countdown {
    private final FortunePillars plugin;
    private final int totalSeconds;
    private int currentSeconds;
    private BukkitTask task;
    private IntConsumer onTick;
    private Runnable onComplete;
    private Runnable onCancel;
    private boolean running = false;

    public Countdown(FortunePillars plugin, int seconds) {
        this.plugin = plugin;
        this.totalSeconds = seconds;
        this.currentSeconds = seconds;
    }

    public Countdown onTick(IntConsumer onTick) {
        this.onTick = onTick;
        return this;
    }

    public Countdown onComplete(Runnable onComplete) {
        this.onComplete = onComplete;
        return this;
    }

    public Countdown onCancel(Runnable onCancel) {
        this.onCancel = onCancel;
        return this;
    }

    public void start() {
        if (this.running) {
            return;
        }
        this.running = true;
        this.currentSeconds = this.totalSeconds;
        this.task = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            if (this.currentSeconds <= 0) {
                this.stop();
                if (this.onComplete != null) {
                    this.onComplete.run();
                }
                return;
            }
            if (this.onTick != null) {
                this.onTick.accept(this.currentSeconds);
            }
            --this.currentSeconds;
        }, 0L, 20L);
    }

    public void cancel() {
        if (!this.running) {
            return;
        }
        this.stop();
        if (this.onCancel != null) {
            this.onCancel.run();
        }
    }

    private void stop() {
        this.running = false;
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
    }

    public boolean isRunning() {
        return this.running;
    }

    public int getCurrentSeconds() {
        return this.currentSeconds;
    }

    public int getTotalSeconds() {
        return this.totalSeconds;
    }

    public void setSeconds(int seconds) {
        this.currentSeconds = seconds;
    }

    public static Countdown create(FortunePillars plugin, int seconds) {
        return new Countdown(plugin, seconds);
    }
}

