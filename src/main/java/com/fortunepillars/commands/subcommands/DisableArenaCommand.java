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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.CommandSender;

public class DisableArenaCommand
implements SubCommand {
    private final FortunePillars plugin;

    public DisableArenaCommand(FortunePillars plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "disable";
    }

    @Override
    public String getDescription() {
        return "Disable an arena";
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
            sender.sendMessage(this.plugin.getMessages().getWithPrefix("arena-not-found", "arena", arenaName));
            return;
        }
        if (arena.getState() == GameState.DISABLED) {
            sender.sendMessage(FortunePillars.parseWithPrefix("<yellow>Arena '<gold>" + arena.getName() + "</gold>' is already disabled!"));
            return;
        }
        if (arena.getState() == GameState.IN_GAME || arena.getState() == GameState.STARTING) {
            arena.forceEnd();
            sender.sendMessage(FortunePillars.parseWithPrefix("<yellow>Ended active game in arena."));
        } else if (arena.getState() == GameState.WAITING) {
            arena.forceEnd();
        }
        arena.setEnabled(false);
        arena.save();
        sender.sendMessage(this.plugin.getMessages().getWithPrefix("arena-disabled", "arena", arena.getName()));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return this.plugin.getArenaManager().getAllArenas().stream().filter(arena -> arena.getState() != GameState.DISABLED).map(Arena::getName).filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

