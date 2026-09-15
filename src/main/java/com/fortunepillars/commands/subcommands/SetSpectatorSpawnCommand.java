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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpectatorSpawnCommand
extends BaseSubCommand {
    public SetSpectatorSpawnCommand(FortunePillars plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "setspectatorspawn";
    }

    @Override
    public String getDescription() {
        return "Set the spectator spawn location";
    }

    @Override
    public String getUsage() {
        return "/tof setspectatorspawn <arena>";
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
        return 1;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>Usage: " + this.getUsage()));
            return;
        }
        Player player = (Player)sender;
        String arenaName = args[0].toLowerCase();
        Arena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            player.sendMessage(this.plugin.getMessages().getWithPrefix("arena-not-found", "arena", arenaName));
            return;
        }
        arena.setSpectatorSpawn(player.getLocation());
        player.sendMessage(this.plugin.getMessages().getWithPrefix("spectator-spawn-set", "arena", arenaName));
        if (arena.isSetupComplete()) {
            player.sendMessage(FortunePillars.parseWithPrefix("<green>Arena setup complete! Don't forget to save the schematic with:"));
            player.sendMessage(FortunePillars.parseWithPrefix("<yellow>/tof save " + arenaName));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return this.plugin.getArenaManager().getArenaNames().stream().filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

