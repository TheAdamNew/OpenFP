/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 *  org.bukkit.event.HandlerList
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.fortunepillars.api.events;

import com.fortunepillars.api.events.FortunePillarsGameEvent;
import com.fortunepillars.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GameEndEvent
extends FortunePillarsGameEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player winner;
    private final EndReason reason;

    public GameEndEvent(Arena arena, @Nullable Player winner, EndReason reason) {
        super(arena);
        this.winner = winner;
        this.reason = reason;
    }

    @Nullable
    public Player getWinner() {
        return this.winner;
    }

    public EndReason getReason() {
        return this.reason;
    }

    public boolean hasWinner() {
        return this.winner != null;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public static enum EndReason {
        LAST_PLAYER_STANDING,
        ALL_PLAYERS_LEFT,
        FORCE_ENDED,
        SERVER_SHUTDOWN;

    }
}

