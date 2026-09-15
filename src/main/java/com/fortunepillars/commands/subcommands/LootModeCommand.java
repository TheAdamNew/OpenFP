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
import com.fortunepillars.game.loot.LootMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LootModeCommand
extends BaseSubCommand {
    public LootModeCommand(FortunePillars plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "lootmode";
    }

    @Override
    public String getDescription() {
        return "Change loot distribution mode";
    }

    @Override
    public String getUsage() {
        return "<mode>";
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
        LootMode mode;
        Player player = (Player)sender;
        Arena arena = this.plugin.getArenaManager().getPlayerArena(player);
        if (arena == null) {
            player.sendMessage(FortunePillars.parseWithPrefix("<red>You must be in an arena!"));
            return;
        }
        try {
            mode = LootMode.valueOf(args[0].toUpperCase().replace("-", "_"));
        }
        catch (IllegalArgumentException e) {
            player.sendMessage(FortunePillars.parseWithPrefix("<red>Invalid loot mode! Use: FAST_OP, SINGLE_LOOT, or SINGLE_OP"));
            return;
        }
        player.sendMessage(FortunePillars.parseWithPrefix("<green>Loot mode set to: <yellow>" + mode.getDisplayName()));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Arrays.stream(LootMode.values()).map(Enum::name).filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

