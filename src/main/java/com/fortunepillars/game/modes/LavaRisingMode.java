/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.World
 *  org.bukkit.block.Block
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package com.fortunepillars.game.modes;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class LavaRisingMode {
    private final FortunePillars plugin;
    private final Arena arena;
    private BukkitTask task;
    private int currentY;
    private int maxY;
    private final int riseInterval;
    private final Set<Location> lavaBlocks;

    public LavaRisingMode(FortunePillars plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
        this.lavaBlocks = new HashSet<Location>();
        int configInterval = plugin.getConfigManager().getConfig().getInt("game-modes.lava-rising.rise-interval", 10);
        this.riseInterval = configInterval * 20;
        plugin.getLogger().info("Lava Rising Mode: Using interval of " + configInterval + " seconds (" + this.riseInterval + " ticks)");
        Location pos1 = arena.getPos1();
        Location pos2 = arena.getPos2();
        if (pos1 != null && pos2 != null) {
            this.currentY = Math.min(pos1.getBlockY(), pos2.getBlockY());
            this.maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        } else {
            plugin.getLogger().warning("Lava Rising mode created with null arena positions!");
        }
    }

    public void start() {
        this.plugin.getLogger().info("Starting Lava Rising mode for arena: " + this.arena.getName());
        Location pos1 = this.arena.getPos1();
        Location pos2 = this.arena.getPos2();
        if (pos1 == null || pos2 == null) {
            this.plugin.getLogger().warning("Cannot start Lava Rising - arena positions not set!");
            return;
        }
        this.arena.broadcastMessage(FortunePillars.parseWithPrefix("<red><bold>\ud83d\udd25 LAVA RISING MODE! \ud83d\udd25</bold></red>"));
        int intervalSeconds = this.riseInterval / 20;
        this.arena.broadcastMessage(FortunePillars.parse("<gray>Lava will rise from <yellow>Y=" + this.currentY + "</yellow> every <red>" + intervalSeconds + " seconds</red>!"));
        this.plugin.getLogger().info("Lava Rising: Starting at Y=" + this.currentY + ", Max Y=" + this.maxY + ", Interval=" + intervalSeconds + "s");
        this.task = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            if (this.currentY >= this.maxY) {
                this.plugin.getLogger().info("Lava reached max height (Y=" + this.maxY + "), stopping");
                this.stop();
                this.arena.broadcastMessage(FortunePillars.parse("<dark_red><bold>\ud83d\udd25 LAVA HAS REACHED THE TOP! \ud83d\udd25</bold></dark_red>"));
                return;
            }
            this.riseLava();
            ++this.currentY;
            if (this.currentY % 5 == 0) {
                this.arena.broadcastMessage(FortunePillars.parse("<red>\ud83d\udd25 Lava rising! Current level: <yellow>Y=" + this.currentY + "</yellow></red>"));
                this.arena.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.BLOCK_LAVA_POP, 1.0f, 0.8f));
            }
        }, (long)this.riseInterval, (long)this.riseInterval);
        this.plugin.getLogger().info("Lava Rising mode started successfully");
    }

    private void riseLava() {
        Location pos1 = this.arena.getPos1();
        Location pos2 = this.arena.getPos2();
        if (pos1 == null || pos2 == null) {
            return;
        }
        World world = pos1.getWorld();
        if (world == null) {
            return;
        }
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        int blocksPlaced = 0;
        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                Location blockLoc = new Location(world, (double)x, (double)this.currentY, (double)z);
                Block block = world.getBlockAt(blockLoc);
                if (block.getType() != Material.AIR && block.getType() != Material.CAVE_AIR && !block.getType().name().contains("GRASS") && !block.getType().name().contains("FLOWER") && !block.getType().name().contains("SAPLING")) continue;
                block.setType(Material.LAVA);
                this.lavaBlocks.add(blockLoc.clone());
                ++blocksPlaced;
            }
        }
    }

    public void stop() {
        this.plugin.getLogger().info("Stopping Lava Rising mode for arena: " + this.arena.getName());
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
        this.plugin.getLogger().info("Removing " + this.lavaBlocks.size() + " lava blocks...");
        int removed = 0;
        for (Location loc : this.lavaBlocks) {
            if (loc.getBlock().getType() != Material.LAVA) continue;
            loc.getBlock().setType(Material.AIR);
            ++removed;
        }
        this.plugin.getLogger().info("Removed " + removed + " lava blocks from arena " + this.arena.getName());
        this.lavaBlocks.clear();
    }
}

