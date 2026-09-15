/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package com.fortunepillars.commands.subcommands;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.commands.subcommands.BaseSubCommand;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetPosCommand
extends BaseSubCommand {
    public SetPosCommand(FortunePillars plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "setpos";
    }

    @Override
    public String getDescription() {
        return "Set arena boundary positions";
    }

    @Override
    public String getUsage() {
        return "/tof setpos <arena> <1|2>";
    }

    @Override
    public String getPermission() {
        return "fortunepillars.admin";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>Usage: " + this.getUsage()));
            return;
        }
        Player player = (Player)sender;
        String arenaName = args[0].toLowerCase();
        String posNumber = args[1];
        Arena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            player.sendMessage(this.plugin.getMessages().getWithPrefix("arena-not-found", "arena", arenaName));
            return;
        }
        if (posNumber.equals("1")) {
            arena.setPos1(player.getLocation());
            player.sendMessage(this.plugin.getMessages().getWithPrefix("pos1-set", "arena", arenaName));
        } else if (posNumber.equals("2")) {
            arena.setPos2(player.getLocation());
            player.sendMessage(this.plugin.getMessages().getWithPrefix("pos2-set", "arena", arenaName));
        } else {
            player.sendMessage(FortunePillars.parseWithPrefix("<red>Position must be 1 or 2!"));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return this.plugin.getArenaManager().getArenaNames().stream().filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2) {
            return Arrays.asList("1", "2");
        }
        return Collections.emptyList();
    }
}

