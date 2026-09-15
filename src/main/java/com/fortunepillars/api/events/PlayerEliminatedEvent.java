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

public class PlayerEliminatedEvent
extends FortunePillarsGameEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player eliminated;
    private final Player killer;
    private final EliminationCause cause;
    private final int remainingPlayers;

    public PlayerEliminatedEvent(Arena arena, Player eliminated, @Nullable Player killer, EliminationCause cause, int remainingPlayers) {
        super(arena);
        this.eliminated = eliminated;
        this.killer = killer;
        this.cause = cause;
        this.remainingPlayers = remainingPlayers;
    }

    public Player getEliminated() {
        return this.eliminated;
    }

    @Nullable
    public Player getKiller() {
        return this.killer;
    }

    public boolean hasKiller() {
        return this.killer != null;
    }

    public EliminationCause getCause() {
        return this.cause;
    }

    public int getRemainingPlayers() {
        return this.remainingPlayers;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public static enum EliminationCause {
        PVP,
        VOID,
        FALL,
        ENVIRONMENT,
        DISCONNECT,
        OTHER;

    }
}

