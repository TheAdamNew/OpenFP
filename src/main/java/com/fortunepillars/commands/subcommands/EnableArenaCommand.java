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
import com.fortunepillars.commands.subcommands.BaseSubCommand;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.CommandSender;

public class EnableArenaCommand
extends BaseSubCommand {
    public EnableArenaCommand(FortunePillars plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "enable";
    }

    @Override
    public String getDescription() {
        return "Enable an arena";
    }

    @Override
    public String getUsage() {
        return "/tof enable <arena>";
    }

    @Override
    public String getPermission() {
        return "fortunepillars.admin.enable";
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
        String arenaName = args[0];
        Arena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>Arena '<yellow>" + arenaName + "</yellow>' not found!"));
            return;
        }
        if (arena.getState() == GameState.WAITING) {
            sender.sendMessage(FortunePillars.parseWithPrefix("<yellow>Arena '<gold>" + arena.getName() + "</gold>' is already enabled!"));
            return;
        }
        if (!arena.isSetupComplete()) {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>Arena is not fully configured! Use /tof info " + arena.getName() + " to check."));
            return;
        }
        arena.setState(GameState.WAITING);
        sender.sendMessage(FortunePillars.parseWithPrefix("<green>Arena '<yellow>" + arena.getName() + "</yellow>' has been enabled!"));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return this.plugin.getArenaManager().getAllArenas().stream().filter(arena -> arena.getState() == GameState.DISABLED).map(Arena::getName).filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

