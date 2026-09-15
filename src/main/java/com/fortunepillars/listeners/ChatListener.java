/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.papermc.paper.event.player.AsyncChatEvent
 *  net.kyori.adventure.text.BuildableComponent
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TextComponent$Builder
 *  net.kyori.adventure.text.format.NamedTextColor
 *  net.kyori.adventure.text.format.TextColor
 *  net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 */
package com.fortunepillars.listeners;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.arena.GameState;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.BuildableComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ChatListener
implements Listener {
    private final FortunePillars plugin;

    public ChatListener(FortunePillars plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onAsyncChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        Arena arena = this.plugin.getArenaManager().getPlayerArena(player);
        if (arena == null) {
            return;
        }
        if (!this.plugin.getConfigManager().isArenaChatEnabled()) {
            return;
        }
        GameState state = arena.getState();
        if (state == GameState.DISABLED || state == GameState.ENDING || state == GameState.RESETTING) {
            return;
        }
        if (state == GameState.WAITING || state == GameState.STARTING || state == GameState.IN_GAME) {
            event.setCancelled(true);
            String messageText = PlainTextComponentSerializer.plainText().serialize(event.message());
            if (arena.isSpectator(player.getUniqueId())) {
                Component prefix = FortunePillars.parse("<gray>[SPECTATOR] ");
                Component formattedMessage = prefix.append(FortunePillars.parse("<gray>" + player.getName())).append(FortunePillars.parse("<dark_gray> \u00bb </dark_gray>")).append(FortunePillars.parse("<gray>" + messageText));
                for (Player spectator : arena.getSpectators()) {
                    spectator.sendMessage(formattedMessage);
                }
            } else if (arena.isPlayerAlive(player.getUniqueId())) {
                Component prefix = FortunePillars.parse("<green>[ALIVE] ");
                BuildableComponent formattedMessage = ((TextComponent.Builder)((TextComponent.Builder)((TextComponent.Builder)((TextComponent.Builder)((TextComponent.Builder)Component.text().append(FortunePillars.parse("<gold>[" + arena.getName().toUpperCase() + "] "))).append(prefix)).append(FortunePillars.parse("<yellow>" + player.getName()))).append(FortunePillars.parse("<dark_gray> \u00bb </dark_gray>"))).append((Component)Component.text((String)messageText, (TextColor)NamedTextColor.WHITE))).build();
                for (Player arenaPlayer : arena.getOnlinePlayers()) {
                    arenaPlayer.sendMessage((Component)formattedMessage);
                }
            } else {
                Component prefix = FortunePillars.parse("<red>[DEAD] ");
                Component formattedMessage = prefix.append(FortunePillars.parse("<yellow>" + player.getName())).append(FortunePillars.parse("<dark_gray> \u00bb </dark_gray>")).append((Component)Component.text((String)messageText, (TextColor)NamedTextColor.GRAY));
                arena.getOnlinePlayers().forEach(p -> p.sendMessage(formattedMessage));
            }
        }
    }
}

