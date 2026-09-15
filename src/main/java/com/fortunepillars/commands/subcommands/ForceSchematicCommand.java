/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandSender
 *  org.bukkit.plugin.Plugin
 */
package com.fortunepillars.commands.subcommands;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.commands.subcommands.SubCommand;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class ForceSchematicCommand
implements SubCommand {
    private final FortunePillars plugin;

    public ForceSchematicCommand(FortunePillars plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "forcesaveschematic";
    }

    @Override
    public String getDescription() {
        return "Force save arena schematic (blocks main thread)";
    }

    @Override
    public String getUsage() {
        return "<arena>";
    }

    @Override
    public String getPermission() {
        return "fortunepillars.admin";
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String arenaName = args[0];
        Arena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>Arena not found: " + arenaName));
            return;
        }
        if (arena.getPos1() == null || arena.getPos2() == null) {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>Arena positions not set!"));
            return;
        }
        sender.sendMessage(FortunePillars.parseWithPrefix("<yellow>\u26a0 WARNING: This will freeze the server temporarily!"));
        sender.sendMessage(FortunePillars.parseWithPrefix("<yellow>Force-saving schematic in 3 seconds..."));
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            sender.sendMessage(FortunePillars.parseWithPrefix("<gold>Saving schematic... Server may lag!"));
            long startTime = System.currentTimeMillis();
            arena.forceSaveSchematicSync();
            long duration = System.currentTimeMillis() - startTime;
            sender.sendMessage(FortunePillars.parseWithPrefix("<green>\u2713 Schematic force-saved in " + duration + "ms"));
            sender.sendMessage(FortunePillars.parseWithPrefix("<gray>File: schematics/" + arenaName + ".fpschem"));
            if (this.plugin.getSchematicManager().hasSchematic(arenaName)) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<green>\u2713 Verified: Schematic file exists!"));
            } else {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>\u2717 ERROR: Schematic file not found!"));
            }
        }, 60L);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return this.plugin.getArenaManager().getArenaNames();
        }
        return Collections.emptyList();
    }
}

