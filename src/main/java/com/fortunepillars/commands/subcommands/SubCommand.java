/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 */
package com.fortunepillars.commands.subcommands;

import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;

public interface SubCommand {
    public String getName();

    public String getDescription();

    public String getUsage();

    public String getPermission();

    public boolean isPlayerOnly();

    public int getMinArgs();

    public void execute(CommandSender var1, String[] var2);

    default public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}

