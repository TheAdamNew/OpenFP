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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.bukkit.command.CommandSender;

public class SetMaxPlayersCommand
extends BaseSubCommand {
    public SetMaxPlayersCommand(FortunePillars plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "setmaxplayers";
    }

    @Override
    public String getDescription() {
        return "Set maximum players for an arena";
    }

    @Override
    public String getUsage() {
        return "/tof setmaxplayers <arena> <amount>";
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
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>Usage: " + this.getUsage()));
            return;
        }
        String arenaName = args[0].toLowerCase();
        Arena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            sender.sendMessage(this.plugin.getMessages().getWithPrefix("arena-not-found", "arena", arenaName));
            return;
        }
        int maxCages = this.plugin.getConfigManager().getMaxCages();
        try {
            amount = Integer.parseInt(args[1]);
            if (amount < 2 || amount > maxCages) {
                throw new NumberFormatException();
            }
        }
        catch (NumberFormatException e) {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>Amount must be between 2 and " + maxCages + "!"));
            return;
        }
        if (amount < arena.getMinPlayers()) {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>Maximum players cannot be less than minimum players (" + arena.getMinPlayers() + ")!"));
            return;
        }
        arena.setMaxPlayers(amount);
        sender.sendMessage(FortunePillars.parseWithPrefix("<green>Maximum players for arena <yellow>" + arenaName + "</yellow> set to <yellow>" + amount + "</yellow>!"));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return this.plugin.getArenaManager().getArenaNames().stream().filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2) {
            int maxCages = this.plugin.getConfigManager().getMaxCages();
            return IntStream.rangeClosed(2, maxCages).mapToObj(String::valueOf).filter(num -> num.startsWith(args[1])).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

