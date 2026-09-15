/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.block.Block
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.player.PlayerDropItemEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.event.player.PlayerPickupArrowEvent
 *  org.bukkit.inventory.ItemStack
 */
package com.fortunepillars.listeners;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.arena.GameState;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener
implements Listener {
    private final FortunePillars plugin;

    public PlayerInteractListener(FortunePillars plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Material item;
        Player player = event.getPlayer();
        if (this.plugin.getConfigManager().isWorldBlacklisted(player.getWorld())) {
            return;
        }
        Arena arena = this.plugin.getArenaManager().getPlayerArena(player);
        if (arena == null) {
            return;
        }
        if (arena.isSpectator(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (!arena.getState().allowsPlayerDamage()) {
            Block block;
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && (block = event.getClickedBlock()) != null && this.isContainer(block.getType())) {
                event.setCancelled(true);
                return;
            }
            if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) && ((item = event.getMaterial()).isBlock() || item.name().contains("BUCKET") || item == Material.FLINT_AND_STEEL || item == Material.FIRE_CHARGE)) {
                event.setCancelled(true);
                return;
            }
        }
        if (arena.getState() == GameState.IN_GAME) {
            ItemStack heldItem = event.getItem();
            if (heldItem != null && this.plugin.getLootManager().isExcludedItem(heldItem.getType())) {
                event.setCancelled(true);
                player.sendMessage(FortunePillars.parseWithPrefix("<red>You cannot use this item!"));
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Arena arena = this.plugin.getArenaManager().getPlayerArena(player);
        if (arena == null) {
            return;
        }
        if (arena.isSpectator(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (!arena.getState().allowsPlayerDamage()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onArrowPickup(PlayerPickupArrowEvent event) {
        Player player = event.getPlayer();
        Arena arena = this.plugin.getArenaManager().getPlayerArena(player);
        if (arena == null) {
            return;
        }
        if (arena.isSpectator(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    private boolean isContainer(Material material) {
        return material == Material.CHEST || material == Material.TRAPPED_CHEST || material == Material.BARREL || material == Material.FURNACE || material == Material.BLAST_FURNACE || material == Material.SMOKER || material == Material.DISPENSER || material == Material.DROPPER || material == Material.HOPPER || material == Material.SHULKER_BOX || material.name().contains("SHULKER_BOX");
    }
}

