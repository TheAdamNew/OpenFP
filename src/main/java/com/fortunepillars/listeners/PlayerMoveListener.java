/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Color
 *  org.bukkit.Location
 *  org.bukkit.Particle
 *  org.bukkit.Particle$DustOptions
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerMoveEvent
 */
package com.fortunepillars.listeners;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.arena.CageLocation;
import com.fortunepillars.arena.GameState;
import com.fortunepillars.player.PlayerData;
import com.fortunepillars.utils.EffectUtil;
import java.awt.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener
implements Listener {
    private final FortunePillars plugin;

    public PlayerMoveListener(FortunePillars plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!this.hasActuallyMoved(event.getFrom(), event.getTo())) {
            return;
        }
        Player player = event.getPlayer();
        Arena arena = this.plugin.getArenaManager().getPlayerArena(player);
        if (arena == null) {
            return;
        }
        PlayerData playerData = arena.getPlayerData(player.getUniqueId());
        if (arena.getState() == GameState.WAITING || arena.getState() == GameState.STARTING) {
            double distance;
            Location cageCenter;
            CageLocation cage;
            if (playerData != null && (cage = arena.getCageLocations().get(playerData.getCageNumber())) != null && (cageCenter = cage.getLocation()) != null && (distance = event.getTo().distance(cageCenter)) > 2.0) {
                event.setTo(cageCenter);
            }
            return;
        }
        if (arena.getState() == GameState.IN_GAME && arena.isPlayerAlive(player.getUniqueId())) {
            if (event.getTo().getY() < -64.0) {
                player.setHealth(0.0);
            }
            if (playerData != null) {
                this.spawnPlayerTrail(player, playerData.getTrailEffect());
            }
        }
    }

    private void spawnPlayerTrail(Player player, String trailId) {
        if (trailId == null || trailId.isEmpty() || trailId.equalsIgnoreCase("NONE")) {
            return;
        }
        Location loc = player.getLocation().add(0.0, 0.2, 0.0);
        switch (trailId.toUpperCase()) {
            case "HEARTS": {
                loc.getWorld().spawnParticle(Particle.HEART, loc, 1, 0.2, 0.2, 0.2, 0.0);
                break;
            }
            case "FLAMES": {
                loc.getWorld().spawnParticle(Particle.FLAME, loc, 3, 0.1, 0.1, 0.1, 0.02);
                break;
            }
            case "WATER": {
                loc.getWorld().spawnParticle(Particle.DRIPPING_WATER, loc, 3, 0.2, 0.1, 0.2, 0.0);
                break;
            }
            case "SMOKE": {
                loc.getWorld().spawnParticle(Particle.SMOKE, loc, 3, 0.1, 0.1, 0.1, 0.01);
                break;
            }
            case "MAGIC": {
                loc.getWorld().spawnParticle(Particle.ENCHANT, loc, 4, 0.2, 0.2, 0.2, 0.1);
                break;
            }
            case "RAINBOW": {
                Color javaColor = Color.getHSBColor((float)Math.random(), 1.0f, 1.0f);
                org.bukkit.Color bukkitColor = org.bukkit.Color.fromRGB((int)javaColor.getRed(), (int)javaColor.getGreen(), (int)javaColor.getBlue());
                EffectUtil.spawnColoredDust(loc, bukkitColor, 1.0f, 3);
                break;
            }
            case "ENDER": {
                loc.getWorld().spawnParticle(Particle.PORTAL, loc, 5, 0.2, 0.2, 0.2, 0.1);
                break;
            }
            case "REDSTONE": {
                Particle.DustOptions dust = new Particle.DustOptions(org.bukkit.Color.RED, 1.0f);
                loc.getWorld().spawnParticle(Particle.DUST, loc, 3, 0.1, 0.1, 0.1, (Object)dust);
                break;
            }
            case "SLIME": {
                loc.getWorld().spawnParticle(Particle.ITEM_SLIME, loc, 3, 0.1, 0.1, 0.1, 0.0);
            }
        }
    }

    private boolean hasActuallyMoved(Location from, Location to) {
        if (to == null) {
            return false;
        }
        return from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ();
    }
}

