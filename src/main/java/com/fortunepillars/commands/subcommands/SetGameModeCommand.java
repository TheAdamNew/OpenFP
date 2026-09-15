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
import com.fortunepillars.game.GameModeType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.CommandSender;

public class SetGameModeCommand
implements SubCommand {
    private final FortunePillars plugin;

    public SetGameModeCommand(FortunePillars plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "setgamemode";
    }

    @Override
    public String getDescription() {
        return "Set default game mode for arena";
    }

    @Override
    public String getUsage() {
        return "<arena> <mode>";
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
        GameModeType mode;
        String arenaName = args[0];
        Arena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>Arena not found: " + arenaName));
            return;
        }
        String modeName = args[1].toUpperCase().replace(" ", "_");
        try {
            mode = GameModeType.valueOf(modeName);
        }
        catch (IllegalArgumentException e) {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>Invalid game mode: " + args[1]));
            sender.sendMessage(FortunePillars.parseWithPrefix("<yellow>Available modes:"));
            for (GameModeType type : GameModeType.values()) {
                sender.sendMessage(FortunePillars.parse("  <gray>- <yellow>" + type.name()));
            }
            return;
        }
        arena.setDefaultGameMode(mode);
        sender.sendMessage(FortunePillars.parseWithPrefix("<green>\u2713 Default game mode set to <gold>" + mode.getDisplayName() + "</gold> for arena <yellow>" + arenaName));
        sender.sendMessage(FortunePillars.parseWithPrefix("<gray>This will be used when there are no votes."));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return this.plugin.getArenaManager().getArenaNames();
        }
        if (args.length == 2) {
            return Arrays.stream(GameModeType.values()).map(Enum::name).filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

