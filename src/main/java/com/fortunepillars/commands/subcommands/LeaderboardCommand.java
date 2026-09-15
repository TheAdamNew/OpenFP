/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package com.fortunepillars.commands.subcommands;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.commands.subcommands.BaseSubCommand;
import com.fortunepillars.gui.LeaderboardGUI;
import com.fortunepillars.gui.LeaderboardType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaderboardCommand
extends BaseSubCommand {
    public LeaderboardCommand(FortunePillars plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "leaderboard";
    }

    @Override
    public String getDescription() {
        return "View leaderboards";
    }

    @Override
    public String getUsage() {
        return "/tof leaderboard [type]";
    }

    @Override
    public String getPermission() {
        return "fortunepillars.player";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public int getMinArgs() {
        return 0;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        LeaderboardType type;
        Player player = (Player)sender;
        if (args.length == 0) {
            new LeaderboardGUI(this.plugin, player).open();
            return;
        }
        String typeName = args[0].toUpperCase();
        try {
            type = LeaderboardType.valueOf(typeName);
        }
        catch (IllegalArgumentException e) {
            player.sendMessage(FortunePillars.parseWithPrefix("<red>Invalid leaderboard type! Options: " + Arrays.stream(LeaderboardType.values()).map(t -> t.name().toLowerCase()).collect(Collectors.joining(", "))));
            return;
        }
        this.plugin.getLeaderboardManager().showLeaderboard(player, type.getKey());
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Arrays.stream(LeaderboardType.values()).map(type -> type.name().toLowerCase()).filter(name -> name.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

