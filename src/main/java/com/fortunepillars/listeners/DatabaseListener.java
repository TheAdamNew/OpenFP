/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerJoinEvent
 *  org.bukkit.plugin.Plugin
 */
package com.fortunepillars.listeners;

import com.fortunepillars.FortunePillars;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class DatabaseListener
implements Listener {
    private final FortunePillars plugin;

    public DatabaseListener(FortunePillars plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            this.plugin.getDatabase().createPlayerData(player.getUniqueId(), player.getName());
            this.plugin.getDatabase().updatePlayerName(player.getUniqueId(), player.getName());
            this.plugin.getLogger().info("Ensured database entry for player: " + player.getName());
        });
    }
}

