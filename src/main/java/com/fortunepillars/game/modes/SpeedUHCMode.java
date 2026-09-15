/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.GameRule
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.scheduler.BukkitTask
 */
package com.fortunepillars.game.modes;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

public class SpeedUHCMode {
    private final FortunePillars plugin;
    private final Arena arena;
    private boolean originalRegenRule;
    private World world;
    private BukkitTask speedTask;

    public SpeedUHCMode(FortunePillars plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
    }

    public void start() {
        this.plugin.getLogger().info("Starting Speed UHC mode for arena: " + this.arena.getName());
        this.arena.broadcastMessage(FortunePillars.parseWithPrefix("<gold><bold>\u26a1 SPEED UHC MODE! \u26a1</bold></gold>"));
        this.arena.broadcastMessage(FortunePillars.parse("<gray>No natural regeneration! Speed boost active!"));
        this.arena.broadcastMessage(FortunePillars.parse("<yellow>Use golden apples to heal!"));
        Location pos1 = this.arena.getPos1();
        if (pos1 == null) {
            this.plugin.getLogger().warning("Cannot start Speed UHC - arena position 1 is null!");
            return;
        }
        this.world = pos1.getWorld();
        if (this.world == null) {
            this.plugin.getLogger().warning("Cannot start Speed UHC - world is null!");
            return;
        }
        try {
            Boolean currentRegen = (Boolean)this.world.getGameRuleValue(GameRule.NATURAL_REGENERATION);
            this.originalRegenRule = currentRegen != null ? currentRegen : true;
            this.world.setGameRule(GameRule.NATURAL_REGENERATION, false);
            this.plugin.getLogger().info("Disabled natural regeneration (was: " + this.originalRegenRule + ")");
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Failed to set natural regeneration: " + e.getMessage());
        }
        this.plugin.getLogger().info("Speed UHC mode started successfully");
    }

    public void stop() {
        this.plugin.getLogger().info("Stopping Speed UHC mode for arena: " + this.arena.getName());
        if (this.speedTask != null) {
            this.speedTask.cancel();
            this.speedTask = null;
        }
        if (this.world != null) {
            try {
                this.world.setGameRule(GameRule.NATURAL_REGENERATION, this.originalRegenRule);
                this.plugin.getLogger().info("Restored natural regeneration to: " + this.originalRegenRule);
            }
            catch (Exception e) {
                this.plugin.getLogger().warning("Failed to restore natural regeneration: " + e.getMessage());
            }
        }
    }
}

