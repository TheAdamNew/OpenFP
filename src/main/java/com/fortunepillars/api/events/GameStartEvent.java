/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 *  org.bukkit.event.Cancellable
 *  org.bukkit.event.HandlerList
 *  org.jetbrains.annotations.NotNull
 */
package com.fortunepillars.api.events;

import com.fortunepillars.api.events.FortunePillarsGameEvent;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.game.GameModeType;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GameStartEvent
extends FortunePillarsGameEvent
implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final List<Player> players;
    private final GameModeType gameMode;
    private boolean cancelled = false;

    public GameStartEvent(Arena arena, List<Player> players, GameModeType gameMode) {
        super(arena);
        this.players = players;
        this.gameMode = gameMode;
    }

    public List<Player> getPlayers() {
        return this.players;
    }

    public GameModeType getGameMode() {
        return this.gameMode;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

