/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.GameMode
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package com.fortunepillars.commands.subcommands;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.arena.GameState;
import com.fortunepillars.commands.subcommands.SubCommand;
import com.fortunepillars.player.PlayerData;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpectateCommand
implements SubCommand {
    private final FortunePillars plugin;

    public SpectateCommand(FortunePillars plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "spectate";
    }

    @Override
    public String getDescription() {
        return "Spectate a game in progress";
    }

    @Override
    public String getUsage() {
        return "<arena>";
    }

    @Override
    public String getPermission() {
        return "fortunepillars.spectate";
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
        Player player = (Player)sender;
        if (this.plugin.getArenaManager().isInArena(player)) {
            player.sendMessage(FortunePillars.parseWithPrefix("<red>You must leave your current arena first! Use /tof leave"));
            return;
        }
        String arenaName = args[0];
        Arena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            player.sendMessage(FortunePillars.parseWithPrefix("<red>Arena '<yellow>" + arenaName + "</yellow>' not found!"));
            return;
        }
        if (arena.getState() != GameState.IN_GAME) {
            player.sendMessage(FortunePillars.parseWithPrefix("<red>There is no active game in this arena to spectate!"));
            return;
        }
        this.plugin.getArenaManager().setPlayerArena(player, arena);
        PlayerData playerData = new PlayerData(player.getUniqueId(), arena);
        playerData.savePlayerState(player);
        arena.getPlayers().put(player.getUniqueId(), playerData);
        this.plugin.getSpectatorManager().makeSpectator(player, arena);
        player.setGameMode(GameMode.SPECTATOR);
        this.plugin.getScoreboardManager().addPlayer(player, arena);
        player.sendMessage(FortunePillars.parseWithPrefix("<green>You are now spectating <yellow>" + arena.getName() + "</yellow>!"));
        player.sendMessage(FortunePillars.parseWithPrefix("<gray>Use <yellow>/tof leave</yellow> to stop spectating."));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return this.plugin.getArenaManager().getAllArenas().stream().filter(arena -> arena.getState() == GameState.IN_GAME).map(Arena::getName).filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

