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
 *  org.bukkit.event.block.BlockBreakEvent
 *  org.bukkit.event.block.BlockExplodeEvent
 *  org.bukkit.event.block.BlockPlaceEvent
 *  org.bukkit.event.entity.EntityExplodeEvent
 */
package com.fortunepillars.listeners;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.arena.GameState;
import java.util.Iterator;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BlockListener
implements Listener {
    private final FortunePillars plugin;

    public BlockListener(FortunePillars plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Arena arena = this.plugin.getArenaManager().getPlayerArena(player);
        if (arena == null) {
            return;
        }
        if (!arena.isInArena(event.getBlock().getLocation())) {
            event.setCancelled(true);
            return;
        }
        if (arena.isSpectator(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (!arena.getState().allowsPlayerDamage()) {
            event.setCancelled(true);
            return;
        }
        if (arena.getState() == GameState.IN_GAME && event.getBlock().getType() == Material.BEDROCK) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Arena arena = this.plugin.getArenaManager().getPlayerArena(player);
        if (arena == null) {
            return;
        }
        Block block = event.getBlock();
        if (!arena.isInArena(block.getLocation())) {
            if (arena.isAboveBuildLimit(block.getLocation())) {
                event.setCancelled(true);
                player.sendMessage(FortunePillars.parseWithPrefix("<red>You've reached the build limit!"));
            } else {
                event.setCancelled(true);
                player.sendMessage(FortunePillars.parseWithPrefix("<red>You can't build outside the arena!"));
            }
            return;
        }
        if (arena.isSpectator(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (!arena.getState().allowsPlayerDamage()) {
            event.setCancelled(true);
            return;
        }
        if (arena.getState() == GameState.IN_GAME && this.plugin.getLootManager().isExcludedItem(block.getType())) {
            event.setCancelled(true);
            player.sendMessage(FortunePillars.parseWithPrefix("<red>You cannot place this block!"));
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Arena arena : this.plugin.getArenaManager().getAllArenas()) {
            if (arena.getState() != GameState.IN_GAME || !arena.isInArena(event.getLocation())) continue;
            Iterator iterator = event.blockList().iterator();
            while (iterator.hasNext()) {
                Block block = (Block)iterator.next();
                if (!arena.isInArena(block.getLocation())) {
                    iterator.remove();
                    continue;
                }
                if (block.getType() != Material.BEDROCK) continue;
                iterator.remove();
            }
            return;
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onBlockExplode(BlockExplodeEvent event) {
        for (Arena arena : this.plugin.getArenaManager().getAllArenas()) {
            if (arena.getState() != GameState.IN_GAME || !arena.isInArena(event.getBlock().getLocation())) continue;
            Iterator iterator = event.blockList().iterator();
            while (iterator.hasNext()) {
                Block block = (Block)iterator.next();
                if (!arena.isInArena(block.getLocation())) {
                    iterator.remove();
                    continue;
                }
                if (block.getType() != Material.BEDROCK) continue;
                iterator.remove();
            }
            return;
        }
    }
}

