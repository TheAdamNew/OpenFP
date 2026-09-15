/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 */
package com.fortunepillars.commands.subcommands;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.commands.subcommands.SubCommand;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.bukkit.command.CommandSender;

public class SetMinPlayersCommand
implements SubCommand {
    private final FortunePillars plugin;

    public SetMinPlayersCommand(FortunePillars plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "setminplayers";
    }

    @Override
    public String getDescription() {
        return "Set minimum players for an arena";
    }

    @Override
    public String getUsage() {
        return "<arena> <amount>";
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
        return 2;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        int amount;
        if (args.length < 2) {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>Usage: /tof setminplayers " + this.getUsage()));
            return;
        }
        String arenaName = args[0].toLowerCase();
        Arena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>Arena '<yellow>" + arenaName + "</yellow>' not found!"));
            return;
        }
        try {
            amount = Integer.parseInt(args[1]);
            if (amount < 1 || amount > 8) {
                throw new NumberFormatException();
            }
        }
        catch (NumberFormatException e) {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>Amount must be between 1 and 8!"));
            return;
        }
        if (amount > arena.getMaxPlayers()) {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>Minimum players cannot be greater than maximum players (" + arena.getMaxPlayers() + ")!"));
            return;
        }
        arena.setMinPlayers(amount);
        sender.sendMessage(FortunePillars.parseWithPrefix("<green>Minimum players for arena <yellow>" + arenaName + "</yellow> set to <yellow>" + amount + "</yellow>!"));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return this.plugin.getArenaManager().getAllArenas().stream().map(Arena::getName).filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2) {
            return IntStream.rangeClosed(1, 8).mapToObj(String::valueOf).filter(num -> num.startsWith(args[1])).collect(Collectors.toList());
        }
        return List.of();
    }
}

