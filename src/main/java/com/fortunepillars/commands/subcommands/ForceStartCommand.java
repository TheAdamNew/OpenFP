/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 */
package com.fortunepillars.commands.subcommands;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.arena.GameState;
import com.fortunepillars.commands.subcommands.SubCommand;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.CommandSender;

public class ForceStartCommand
implements SubCommand {
    private final FortunePillars plugin;

    public ForceStartCommand(FortunePillars plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "forcestart";
    }

    @Override
    public String getDescription() {
        return "Force start a game";
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
        String arenaName = args[0].toLowerCase();
        Arena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>Arena '<yellow>" + arenaName + "</yellow>' not found!"));
            return;
        }
        if (arena.getState() == GameState.IN_GAME) {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>This arena is already in game!"));
            return;
        }
        if (arena.getPlayerCount() < 1) {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>There are no players in this arena!"));
            return;
        }
        arena.startGame();
        sender.sendMessage(FortunePillars.parseWithPrefix("<green>Force started arena <yellow>" + arenaName + "</yellow>!"));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return this.plugin.getArenaManager().getAllArenas().stream().map(Arena::getName).filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}

