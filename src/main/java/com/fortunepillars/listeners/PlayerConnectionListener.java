/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerJoinEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 */
package com.fortunepillars.listeners;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener
implements Listener {
    private final FortunePillars plugin;

    public PlayerConnectionListener(FortunePillars plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (this.plugin.getConfigManager().isWorldBlacklisted(player.getWorld())) {
            this.plugin.getLogger().info(player.getName() + " joined in blacklisted world: " + player.getWorld().getName());
            return;
        }
        this.plugin.getArenaManager().forceRemovePlayer(player);
        this.plugin.getDatabase().createPlayerData(player.getUniqueId(), player.getName());
        this.plugin.getDatabase().updatePlayerName(player.getUniqueId(), player.getName());
        this.plugin.getLogger().info(player.getName() + " joined the server");
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Arena arena = this.plugin.getArenaManager().getPlayerArena(player);
        if (arena != null) {
            this.plugin.getLogger().info(player.getName() + " quit while in arena " + arena.getName());
            this.plugin.getArenaManager().handlePlayerQuit(player);
        } else {
            this.plugin.getArenaManager().forceRemovePlayer(player);
        }
        this.plugin.getGuiManager().closeGUI(player);
        this.plugin.getLogger().info(player.getName() + " left the server");
    }
}

