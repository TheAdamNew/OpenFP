/*
 * Decompiled with CFR 0.152.
 */
package com.fortunepillars.commands.subcommands;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.commands.subcommands.SubCommand;

public abstract class BaseSubCommand
implements SubCommand {
    protected final FortunePillars plugin;

    public BaseSubCommand(FortunePillars plugin) {
        this.plugin = plugin;
    }

    @Override
    public abstract String getName();

    @Override
    public abstract String getDescription();

    @Override
    public abstract String getUsage();

    @Override
    public abstract String getPermission();

    @Override
    public abstract boolean isPlayerOnly();

    @Override
    public abstract int getMinArgs();
}

