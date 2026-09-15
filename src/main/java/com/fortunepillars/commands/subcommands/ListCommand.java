/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 */
package com.fortunepillars.commands.subcommands;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.commands.subcommands.BaseSubCommand;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;

public class ListCommand
extends BaseSubCommand {
    public ListCommand(FortunePillars plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "List all arenas";
    }

    @Override
    public String getUsage() {
        return "/tof list";
    }

    @Override
    public String getPermission() {
        return "fortunepillars.player";
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public int getMinArgs() {
        return 0;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Collection<Arena> arenas = this.plugin.getArenaManager().getAllArenas();
        if (arenas.isEmpty()) {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>No arenas have been created yet!"));
            return;
        }
        sender.sendMessage(FortunePillars.parse(""));
        sender.sendMessage(FortunePillars.parse("<gradient:#FFD700:#FFA500><bold>\u2550\u2550 Arena List \u2550\u2550</bold></gradient>"));
        sender.sendMessage(FortunePillars.parse(""));
        for (Arena arena : arenas) {
            String statusColor = arena.getState().getColor();
            String status = arena.getState().getDisplayName();
            String setupStatus = arena.isSetupComplete() ? "<green>\u2713</green>" : "<red>\u2717 (incomplete)</red>";
            sender.sendMessage(FortunePillars.parse("<yellow>" + arena.getName() + "</yellow> <dark_gray>-</dark_gray> " + statusColor + status + "</dark_gray> <gray>[" + arena.getPlayerCount() + "/" + arena.getMaxPlayers() + "]</gray> " + setupStatus));
        }
        sender.sendMessage(FortunePillars.parse(""));
        sender.sendMessage(FortunePillars.parse("<gray>Use <yellow>/tof join <arena></yellow> to join an arena."));
        sender.sendMessage(FortunePillars.parse(""));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}

