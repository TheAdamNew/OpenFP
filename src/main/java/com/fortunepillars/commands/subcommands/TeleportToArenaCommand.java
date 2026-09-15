/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package com.fortunepillars.commands.subcommands;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.arena.CageLocation;
import com.fortunepillars.commands.subcommands.SubCommand;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportToArenaCommand
implements SubCommand {
    private final FortunePillars plugin;

    public TeleportToArenaCommand(FortunePillars plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "tp";
    }

    @Override
    public String getDescription() {
        return "Teleport to an arena";
    }

    @Override
    public String getUsage() {
        return "<arena>";
    }

    @Override
    public String getPermission() {
        return "fortunepillars.admin.tp";
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
        String arenaName = args[0];
        Arena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            player.sendMessage(FortunePillars.parseWithPrefix("<red>Arena '<yellow>" + arenaName + "</yellow>' not found!"));
            return;
        }
        Location teleportLocation = null;
        if (arena.getSpectatorSpawn() != null) {
            teleportLocation = arena.getSpectatorSpawn();
        } else if (!arena.getCageLocations().isEmpty()) {
            Map<Integer, CageLocation> cages = arena.getCageLocations();
            CageLocation firstCage = cages.values().iterator().next();
            if (firstCage != null) {
                teleportLocation = firstCage.getLocation();
            }
        } else if (arena.getPos1() != null) {
            teleportLocation = arena.getPos1();
        }
        if (teleportLocation == null) {
            player.sendMessage(FortunePillars.parseWithPrefix("<red>No valid location found in arena! Set positions first."));
            return;
        }
        player.teleport(teleportLocation);
        player.sendMessage(FortunePillars.parseWithPrefix("<green>Teleported to arena '<yellow>" + arena.getName() + "</yellow>'!"));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return this.plugin.getArenaManager().getAllArenas().stream().map(Arena::getName).filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}

