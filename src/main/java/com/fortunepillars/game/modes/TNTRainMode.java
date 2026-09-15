/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Sound
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.EntityType
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.TNTPrimed
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 *  org.bukkit.util.Vector
 */
package com.fortunepillars.game.modes;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class TNTRainMode {
    private final FortunePillars plugin;
    private final Arena arena;
    private BukkitTask task;
    private int rainInterval = 80;
    private int tntSpawned = 0;
    private final Set<UUID> arenaTNT = new HashSet<UUID>();
    private boolean running = false;

    public TNTRainMode(FortunePillars plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
    }

    public void start() {
        if (this.running) {
            this.plugin.getLogger().warning("TNT Rain already running for " + this.arena.getName());
            return;
        }
        this.running = true;
        this.tntSpawned = 0;
        this.plugin.getLogger().info("========================================");
        this.plugin.getLogger().info("STARTING TNT RAIN MODE");
        this.plugin.getLogger().info("Arena: " + this.arena.getName());
        this.plugin.getLogger().info("Interval: " + this.rainInterval / 20 + " seconds");
        this.plugin.getLogger().info("========================================");
        this.arena.broadcastMessage(FortunePillars.parseWithPrefix("<red><bold>\ud83d\udca3 TNT RAIN MODE ACTIVATED! \ud83d\udca3</bold></red>"));
        this.arena.broadcastMessage(FortunePillars.parse("<gray>TNT will rain from the sky every <red>4 seconds</red>!"));
        this.arena.broadcastMessage(FortunePillars.parse("<yellow>\u26a0 Explosions are ENABLED!"));
        this.task = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, new Runnable(){

            @Override
            public void run() {
                if (!TNTRainMode.this.running) {
                    return;
                }
                TNTRainMode.this.spawnTNT();
            }
        }, (long)this.rainInterval, (long)this.rainInterval);
        this.plugin.getLogger().info("\u2713 TNT Rain task started (Task ID: " + this.task.getTaskId() + ")");
    }

    private void spawnTNT() {
        List<Player> alivePlayers = this.arena.getAlivePlayers();
        if (alivePlayers.isEmpty()) {
            this.plugin.getLogger().warning("No alive players, skipping TNT spawn");
            return;
        }
        this.plugin.getLogger().info("--- TNT SPAWN CYCLE #" + (this.tntSpawned + 1) + " ---");
        int tntCount = ThreadLocalRandom.current().nextInt(2, 5);
        int successfulSpawns = 0;
        for (int i = 0; i < tntCount; ++i) {
            Player target = alivePlayers.get(ThreadLocalRandom.current().nextInt(alivePlayers.size()));
            double offsetX = ThreadLocalRandom.current().nextDouble(-10.0, 10.0);
            double offsetZ = ThreadLocalRandom.current().nextDouble(-10.0, 10.0);
            int heightAbove = ThreadLocalRandom.current().nextInt(20, 31);
            Location spawnLoc = target.getLocation().clone().add(offsetX, (double)heightAbove, offsetZ);
            this.plugin.getLogger().info("Attempting TNT spawn #" + (i + 1) + ":");
            this.plugin.getLogger().info("  Target: " + target.getName());
            this.plugin.getLogger().info("  Location: " + spawnLoc.getBlockX() + ", " + spawnLoc.getBlockY() + ", " + spawnLoc.getBlockZ());
            this.plugin.getLogger().info("  In arena bounds: " + this.arena.isInArena(spawnLoc));
            try {
                TNTPrimed tnt = (TNTPrimed)spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.TNT);
                tnt.setFuseTicks(Integer.MAX_VALUE);
                tnt.setYield(3.5f);
                tnt.setIsIncendiary(false);
                tnt.setGravity(true);
                Vector velocity = new Vector(ThreadLocalRandom.current().nextDouble(-0.1, 0.1), -0.8, ThreadLocalRandom.current().nextDouble(-0.1, 0.1));
                tnt.setVelocity(velocity);
                this.arenaTNT.add(tnt.getUniqueId());
                ++this.tntSpawned;
                ++successfulSpawns;
                this.plugin.getLogger().info("  \u2713 TNT spawned successfully (UUID: " + String.valueOf(tnt.getUniqueId()) + ")");
                this.scheduleGroundDetection(tnt);
                continue;
            }
            catch (Exception e) {
                this.plugin.getLogger().severe("  \u2717 FAILED to spawn TNT: " + e.getMessage());
                e.printStackTrace();
            }
        }
        if (successfulSpawns > 0) {
            this.plugin.getLogger().info("\u2713 Spawned " + successfulSpawns + "/" + tntCount + " TNT entities");
            this.arena.broadcastMessage(FortunePillars.parse("<red><bold>\ud83d\udca3 " + successfulSpawns + " TNT INCOMING!</bold></red>"));
            for (Player player : this.arena.getAlivePlayers()) {
                player.playSound(player.getLocation(), Sound.ENTITY_TNT_PRIMED, 2.0f, 0.8f);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 2.0f);
            }
        } else {
            this.plugin.getLogger().severe("\u2717 FAILED to spawn any TNT!");
        }
    }

    private void scheduleGroundDetection(final TNTPrimed tnt) {
        BukkitTask detectionTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, new Runnable(){
            int ticks = 0;

            @Override
            public void run() {
                ++this.ticks;
                if (!tnt.isValid() || tnt.isDead()) {
                    TNTRainMode.this.arenaTNT.remove(tnt.getUniqueId());
                    return;
                }
                Location tntLoc = tnt.getLocation();
                if (tnt.isOnGround() || tntLoc.getBlock().getType().isSolid()) {
                    TNTRainMode.this.explodeTNT(tnt);
                    return;
                }
                for (Player player : TNTRainMode.this.arena.getAlivePlayers()) {
                    if (!(player.getLocation().distance(tntLoc) < 1.5)) continue;
                    TNTRainMode.this.explodeTNT(tnt);
                    return;
                }
                if (this.ticks > 300) {
                    TNTRainMode.this.plugin.getLogger().warning("TNT exceeded lifetime, removing");
                    tnt.remove();
                    TNTRainMode.this.arenaTNT.remove(tnt.getUniqueId());
                }
            }
        }, 0L, 1L);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> ((BukkitTask)detectionTask).cancel(), 300L);
    }

    private void explodeTNT(TNTPrimed tnt) {
        if (!tnt.isValid() || tnt.isDead()) {
            return;
        }
        Location loc = tnt.getLocation();
        this.plugin.getLogger().info("\ud83d\udca5 TNT exploding at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        loc.getWorld().createExplosion(loc, 3.5f, false, true);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);
        tnt.remove();
        this.arenaTNT.remove(tnt.getUniqueId());
    }

    public void stop() {
        if (!this.running) {
            return;
        }
        this.running = false;
        this.plugin.getLogger().info("========================================");
        this.plugin.getLogger().info("STOPPING TNT RAIN MODE");
        this.plugin.getLogger().info("Arena: " + this.arena.getName());
        this.plugin.getLogger().info("Total TNT spawned: " + this.tntSpawned);
        this.plugin.getLogger().info("========================================");
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
        int removed = 0;
        for (UUID tntId : new HashSet<UUID>(this.arenaTNT)) {
            Entity entity = Bukkit.getEntity((UUID)tntId);
            if (!(entity instanceof TNTPrimed)) continue;
            entity.remove();
            ++removed;
        }
        this.arenaTNT.clear();
        this.plugin.getLogger().info("\u2713 Removed " + removed + " remaining TNT entities");
        this.tntSpawned = 0;
    }

    public boolean isRunning() {
        return this.running;
    }

    public int getTNTSpawned() {
        return this.tntSpawned;
    }
}

