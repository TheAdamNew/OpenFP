/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
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
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KickCommand
extends BaseSubCommand {
    public KickCommand(FortunePillars plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "kick";
    }

    @Override
    public String getDescription() {
        return "Kick a player from their arena";
    }

    @Override
    public String getUsage() {
        return "/tof kick <player>";
    }

    @Override
    public String getPermission() {
        return "fortunepillars.admin.kick";
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
        String playerName = args[0];
        Player target = Bukkit.getPlayer((String)playerName);
        if (target == null) {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>Player '<yellow>" + playerName + "</yellow>' not found or not online!"));
            return;
        }
        Arena arena = this.plugin.getArenaManager().getPlayerArena(target);
        if (arena == null) {
            if (arena != null && arena.isSpectator(target.getUniqueId())) {
                arena.removeSpectator(target);
                sender.sendMessage(FortunePillars.parseWithPrefix("<green>Kicked <yellow>" + target.getName() + "</yellow> from spectating!"));
                target.sendMessage(FortunePillars.parseWithPrefix("<red>You have been kicked from spectating by an admin."));
                return;
            }
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>Player '<yellow>" + target.getName() + "</yellow>' is not in any arena!"));
            return;
        }
        arena.removePlayer(target, true);
        sender.sendMessage(FortunePillars.parseWithPrefix("<green>Kicked <yellow>" + target.getName() + "</yellow> from arena '<gold>" + arena.getName() + "</gold>'!"));
        target.sendMessage(FortunePillars.parseWithPrefix("<red>You have been kicked from the arena by an admin."));
        arena.broadcastMessage(FortunePillars.parseWithPrefix("<yellow>" + target.getName() + "</yellow> <gray>was kicked from the arena.</gray>"));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().filter(player -> this.plugin.getArenaManager().getPlayerArena((Player)player) != null).map(Player::getName).filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

