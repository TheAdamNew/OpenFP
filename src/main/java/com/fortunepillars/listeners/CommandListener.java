/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerCommandPreprocessEvent
 */
package com.fortunepillars.listeners;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.arena.GameState;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener
implements Listener {
    private final FortunePillars plugin;
    private static final Set<String> ALLOWED_COMMANDS = Set.of("tof leave", "tof l", "tof q", "tof quit", "tof exit", "tof help", "tof scoreboard", "tof sb", "tof stats", "tof s");

    public CommandListener(FortunePillars plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (this.plugin.getConfigManager().isWorldBlacklisted(player.getWorld())) {
            return;
        }
        Arena arena = this.plugin.getArenaManager().getPlayerArena(player);
        if (arena == null) {
            return;
        }
        GameState state = arena.getState();
        if (state != GameState.IN_GAME && state != GameState.STARTING) {
            return;
        }
        if (player.hasPermission("fortunepillars.admin")) {
            return;
        }
        String rawMessage = event.getMessage().toLowerCase().trim();
        String fullCommand = rawMessage.startsWith("/") ? rawMessage.substring(1) : rawMessage;
        for (String allowed : ALLOWED_COMMANDS) {
            if (!fullCommand.equals(allowed) && !fullCommand.startsWith(allowed + " ")) continue;
            return;
        }
        event.setCancelled(true);
        player.sendMessage(FortunePillars.parseWithPrefix("<red>You cannot use commands during a match! <yellow>Use <white>/tof leave <yellow>to exit."));
    }
}

