/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.event.Event
 *  org.bukkit.event.HandlerList
 *  org.jetbrains.annotations.NotNull
 */
package com.fortunepillars.api.events;

import com.fortunepillars.arena.Arena;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class FortunePillarsGameEvent
extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    protected final Arena arena;

    public FortunePillarsGameEvent(Arena arena) {
        this.arena = arena;
    }

    public FortunePillarsGameEvent(Arena arena, boolean async) {
        super(async);
        this.arena = arena;
    }

    public Arena getArena() {
        return this.arena;
    }

    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

