/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.EntityType
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.TNTPrimed
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.CreatureSpawnEvent
 *  org.bukkit.event.entity.CreatureSpawnEvent$SpawnReason
 *  org.bukkit.event.entity.EntitySpawnEvent
 *  org.bukkit.event.entity.EntityTargetLivingEntityEvent
 *  org.bukkit.event.entity.FoodLevelChangeEvent
 *  org.bukkit.event.player.PlayerDropItemEvent
 *  org.bukkit.event.player.PlayerPickupArrowEvent
 */
package com.fortunepillars.listeners;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.arena.GameState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;

public class EntityListener
implements Listener {
    private final FortunePillars plugin;

    public EntityListener(FortunePillars plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntityType() == EntityType.TNT) {
            this.plugin.getLogger().info("TNT spawn detected, allowing...");
            return;
        }
        if (event.getEntity() instanceof TNTPrimed) {
            this.plugin.getLogger().info("TNTPrimed entity spawn detected, allowing...");
            return;
        }
        for (Arena arena : this.plugin.getArenaManager().getAllArenas()) {
            if (!arena.isInArena(event.getLocation())) continue;
            if (arena.getState().allowsPlayerDamage()) {
                return;
            }
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.DISPENSE_EGG) {
            return;
        }
        for (Arena arena : this.plugin.getArenaManager().getAllArenas()) {
            if (arena.getState() != GameState.IN_GAME || !arena.isInArena(event.getLocation())) continue;
            if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
                event.setCancelled(true);
            }
            return;
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        LivingEntity livingEntity = event.getTarget();
        if (!(livingEntity instanceof Player)) {
            return;
        }
        Player player = (Player)livingEntity;
        Arena arena = this.plugin.getArenaManager().getPlayerArena(player);
        if (arena == null) {
            return;
        }
        if (arena.isSpectator(player.getUniqueId())) {
            event.setCancelled(true);
            event.setTarget(null);
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        HumanEntity humanEntity = event.getEntity();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        Arena arena = this.plugin.getArenaManager().getPlayerArena(player);
        if (arena == null) {
            return;
        }
        if (!arena.getState().allowsPlayerDamage()) {
            event.setCancelled(true);
            player.setFoodLevel(20);
            player.setSaturation(20.0f);
            return;
        }
        if (arena.isSpectator(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Arena arena = this.plugin.getArenaManager().getPlayerArena(player);
        if (arena == null) {
            return;
        }
        if (!arena.getState().allowsPlayerDamage()) {
            event.setCancelled(true);
            return;
        }
        if (arena.isSpectator(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onArrowPickup(PlayerPickupArrowEvent event) {
        Player player = event.getPlayer();
        Arena arena = this.plugin.getArenaManager().getPlayerArena(player);
        if (arena != null && arena.isSpectator(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}

