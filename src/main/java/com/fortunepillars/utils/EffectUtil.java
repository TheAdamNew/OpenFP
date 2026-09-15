/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Color
 *  org.bukkit.Location
 *  org.bukkit.Particle
 *  org.bukkit.Particle$DustOptions
 *  org.bukkit.World
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package com.fortunepillars.utils;

import com.fortunepillars.FortunePillars;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class EffectUtil {
    private EffectUtil() {
    }

    public static void spawnParticle(Location location, Particle particle, int count) {
        location.getWorld().spawnParticle(particle, location, count);
    }

    public static void spawnParticle(Location location, Particle particle, int count, double offsetX, double offsetY, double offsetZ) {
        location.getWorld().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ);
    }

    public static void spawnAtPlayer(Player player, Particle particle, int count) {
        player.getWorld().spawnParticle(particle, player.getLocation().add(0.0, 1.0, 0.0), count);
    }

    public static void spawnCircle(Location center, Particle particle, double radius, int points) {
        World world = center.getWorld();
        double increment = Math.PI * 2 / (double)points;
        for (int i = 0; i < points; ++i) {
            double angle = (double)i * increment;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location point = new Location(world, x, center.getY(), z);
            world.spawnParticle(particle, point, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    public static BukkitTask spawnSpiral(FortunePillars plugin, final Location center, final Particle particle, final double radius, final double height, final int duration) {
        return Bukkit.getScheduler().runTaskTimer((Plugin)plugin, new Runnable(){
            double angle = 0.0;
            double y = 0.0;
            int ticks = 0;

            @Override
            public void run() {
                if (this.ticks >= duration) {
                    return;
                }
                double x = center.getX() + radius * Math.cos(this.angle);
                double z = center.getZ() + radius * Math.sin(this.angle);
                Location point = new Location(center.getWorld(), x, center.getY() + this.y, z);
                center.getWorld().spawnParticle(particle, point, 1, 0.0, 0.0, 0.0, 0.0);
                this.angle += 0.39269908169872414;
                this.y += height / (double)duration;
                ++this.ticks;
            }
        }, 0L, 1L);
    }

    public static void spawnColoredDust(Location location, Color color, float size, int count) {
        Particle.DustOptions dust = new Particle.DustOptions(color, size);
        location.getWorld().spawnParticle(Particle.DUST, location, count, 0.3, 0.3, 0.3, (Object)dust);
    }

    public static BukkitTask spawnRainbowCircle(FortunePillars plugin, final Location center, final double radius, final int duration) {
        final Color[] colors = new Color[]{Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.AQUA, Color.BLUE, Color.PURPLE};
        return Bukkit.getScheduler().runTaskTimer((Plugin)plugin, new Runnable(){
            int colorIndex = 0;
            int ticks = 0;

            @Override
            public void run() {
                if (this.ticks >= duration) {
                    return;
                }
                Color color = colors[this.colorIndex % colors.length];
                for (int i = 0; i < 36; ++i) {
                    double angle = 0.17453292519943295 * (double)i;
                    double x = center.getX() + radius * Math.cos(angle);
                    double z = center.getZ() + radius * Math.sin(angle);
                    Location point = new Location(center.getWorld(), x, center.getY(), z);
                    Particle.DustOptions dust = new Particle.DustOptions(color, 1.0f);
                    center.getWorld().spawnParticle(Particle.DUST, point, 1, 0.0, 0.0, 0.0, (Object)dust);
                }
                ++this.colorIndex;
                ++this.ticks;
            }
        }, 0L, 2L);
    }

    public static void spawnExplosion(Location location) {
        World world = location.getWorld();
        world.spawnParticle(Particle.EXPLOSION_EMITTER, location, 1);
        world.spawnParticle(Particle.FLAME, location, 50, 1.0, 1.0, 1.0, 0.1);
        world.spawnParticle(Particle.SMOKE, location, 30, 1.0, 1.0, 1.0, 0.05);
    }

    public static void spawnHealEffect(Player player) {
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0.0, 2.0, 0.0), 5, 0.5, 0.5, 0.5, 0.0);
    }

    public static void spawnDeathEffect(Location location) {
        World world = location.getWorld();
        world.spawnParticle(Particle.SOUL, location.add(0.0, 1.0, 0.0), 20, 0.5, 0.5, 0.5, 0.02);
        world.spawnParticle(Particle.SMOKE, location, 10, 0.3, 0.3, 0.3, 0.01);
    }

    public static void spawnCageEffect(Location location) {
        World world = location.getWorld();
        for (int i = 0; i < 4; ++i) {
            double angle = 1.5707963267948966 * (double)i;
            double x = location.getX() + 1.5 * Math.cos(angle);
            double z = location.getZ() + 1.5 * Math.sin(angle);
            for (double y = 0.0; y < 3.0; y += 0.5) {
                Location point = new Location(world, x, location.getY() + y, z);
                world.spawnParticle(Particle.END_ROD, point, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }

    public static void spawnTeleportEffect(Location from, Location to) {
        from.getWorld().spawnParticle(Particle.PORTAL, from.add(0.0, 1.0, 0.0), 50, 0.5, 1.0, 0.5, 0.1);
        to.getWorld().spawnParticle(Particle.REVERSE_PORTAL, to.add(0.0, 1.0, 0.0), 50, 0.5, 1.0, 0.5, 0.1);
    }
}

