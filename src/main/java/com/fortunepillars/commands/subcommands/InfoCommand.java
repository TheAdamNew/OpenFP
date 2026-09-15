/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.command.CommandSender
 */
package com.fortunepillars.commands.subcommands;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.arena.CageLocation;
import com.fortunepillars.arena.GameState;
import com.fortunepillars.commands.subcommands.SubCommand;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

public class InfoCommand
implements SubCommand {
    private final FortunePillars plugin;

    public InfoCommand(FortunePillars plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getDescription() {
        return "View arena information";
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
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>Arena '<yellow>" + arenaName + "</yellow>' not found!"));
            return;
        }
        sender.sendMessage(FortunePillars.parse(""));
        sender.sendMessage(FortunePillars.parse("<gradient:#FFD700:#FFA500><bold>\u2550\u2550 Arena: " + arena.getName() + " \u2550\u2550</bold></gradient>"));
        sender.sendMessage(FortunePillars.parse(""));
        sender.sendMessage(FortunePillars.parse("<yellow>Status:</yellow> " + this.getStateColor(arena)));
        sender.sendMessage(FortunePillars.parse("<yellow>Setup Complete:</yellow> " + (arena.isSetupComplete() ? "<green>Yes</green>" : "<red>No</red>")));
        sender.sendMessage(FortunePillars.parse("<yellow>Players:</yellow> <white>" + arena.getPlayerCount() + "/" + arena.getMaxPlayers() + "</white>"));
        sender.sendMessage(FortunePillars.parse("<yellow>Min Players:</yellow> <white>" + arena.getMinPlayers() + "</white>"));
        sender.sendMessage(FortunePillars.parse("<yellow>Default Game Mode:</yellow> <white>" + arena.getDefaultGameMode().getDisplayName() + "</white>"));
        sender.sendMessage(FortunePillars.parse(""));
        sender.sendMessage(FortunePillars.parse("<yellow>Boundaries:</yellow>"));
        Location pos1 = arena.getPos1();
        Location pos2 = arena.getPos2();
        sender.sendMessage(FortunePillars.parse("  <gray>Pos1:</gray> " + (String)(pos1 != null ? "<white>" + this.formatLocation(pos1) + "</white>" : "<red>Not set</red>")));
        sender.sendMessage(FortunePillars.parse("  <gray>Pos2:</gray> " + (String)(pos2 != null ? "<white>" + this.formatLocation(pos2) + "</white>" : "<red>Not set</red>")));
        Location specSpawn = arena.getSpectatorSpawn();
        sender.sendMessage(FortunePillars.parse("  <gray>Spectator Spawn:</gray> " + (String)(specSpawn != null ? "<white>" + this.formatLocation(specSpawn) + "</white>" : "<red>Not set</red>")));
        sender.sendMessage(FortunePillars.parse(""));
        sender.sendMessage(FortunePillars.parse("<yellow>Cages:</yellow>"));
        Map<Integer, CageLocation> cages = arena.getCageLocations();
        for (int i = 1; i <= 8; ++i) {
            CageLocation cage = cages.get(i);
            if (cage != null) {
                Location loc = cage.getLocation();
                sender.sendMessage(FortunePillars.parse("  <gray>Cage " + i + ":</gray> <green>\u2713</green> <white>" + (loc != null ? this.formatLocation(loc) : "Unknown") + "</white>"));
                continue;
            }
            sender.sendMessage(FortunePillars.parse("  <gray>Cage " + i + ":</gray> <red>\u2717 Not set</red>"));
        }
        if (arena.getState().isActive()) {
            sender.sendMessage(FortunePillars.parse(""));
            sender.sendMessage(FortunePillars.parse("<yellow>Current Game Mode:</yellow> <white>" + arena.getGameMode().getDisplayName() + "</white>"));
            sender.sendMessage(FortunePillars.parse("<yellow>Alive Players:</yellow> <white>" + arena.getAliveCount() + "</white>"));
        }
        sender.sendMessage(FortunePillars.parse(""));
    }

    private String getStateColor(Arena arena) {
        return switch (arena.getState()) {
            default -> throw new MatchException(null, null);
            case GameState.DISABLED -> "<gray>Disabled</gray>";
            case GameState.WAITING -> "<green>Waiting</green>";
            case GameState.STARTING -> "<yellow>Starting</yellow>";
            case GameState.IN_GAME -> "<red>In Game</red>";
            case GameState.ENDING -> "<gold>Ending</gold>";
            case GameState.RESETTING -> "<blue>Resetting</blue>";
        };
    }

    private String formatLocation(Location loc) {
        return String.format("%s, %.1f, %.1f, %.1f", loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return this.plugin.getArenaManager().getAllArenas().stream().map(Arena::getName).filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

