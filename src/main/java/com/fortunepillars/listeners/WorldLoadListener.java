/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.world.WorldLoadEvent
 */
package com.fortunepillars.listeners;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldLoadListener
implements Listener {
    private final FortunePillars plugin;

    public WorldLoadListener(FortunePillars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        String worldName = event.getWorld().getName();
        this.plugin.getLogger().info("World loaded: " + worldName + " - checking arenas...");
        for (Arena arena : this.plugin.getArenaManager().getAllArenas()) {
            arena.retryPendingDataForWorld(worldName);
        }
    }
}

