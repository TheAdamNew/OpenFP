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

public class JoinCommand
extends BaseSubCommand {
    public JoinCommand(FortunePillars plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String getDescription() {
        return "Join an arena";
    }

    @Override
    public String getUsage() {
        return "/tof join [arena]";
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
        Player player = (Player)sender;
        if (this.plugin.getArenaManager().isInArena(player)) {
            player.sendMessage(this.plugin.getMessages().getWithPrefix("already-in-game"));
            return;
        }
        if (args.length == 0) {
            Arena arena = this.plugin.getArenaManager().findBestArena();
            if (arena == null) {
                player.sendMessage(this.plugin.getMessages().getWithPrefix("no-arenas-available"));
                return;
            }
            player.sendMessage(this.plugin.getMessages().getWithPrefix("quick-join-found", "arena", arena.getName()));
            this.plugin.getArenaManager().joinArena(player, arena.getName());
        } else {
            String arenaName = args[0].toLowerCase();
            Arena arena = this.plugin.getArenaManager().getArena(arenaName);
            if (arena == null) {
                player.sendMessage(this.plugin.getMessages().getWithPrefix("arena-not-found", "arena", arenaName));
                return;
            }
            if (!arena.isSetupComplete()) {
                player.sendMessage(FortunePillars.parseWithPrefix("<red>This arena is not fully set up yet!"));
                return;
            }
            if (!arena.getState().isJoinable()) {
                player.sendMessage(FortunePillars.parseWithPrefix("<red>This arena is currently " + arena.getState().getDisplayName().toLowerCase() + "!"));
                return;
            }
            this.plugin.getArenaManager().joinArena(player, arenaName);
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return this.plugin.getArenaManager().getJoinableArenas().stream().map(Arena::getName).filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

