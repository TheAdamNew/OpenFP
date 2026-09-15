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
import com.fortunepillars.arena.CageLocation;
import com.fortunepillars.commands.subcommands.SubCommand;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetCageCommand
implements SubCommand {
    private final FortunePillars plugin;

    public SetCageCommand(FortunePillars plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "setcage";
    }

    @Override
    public String getDescription() {
        return "Set a cage spawn location";
    }

    @Override
    public String getUsage() {
        int maxCages = this.plugin.getConfigManager().getMaxCages();
        return "<arena> <1-" + maxCages + ">";
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
        return 2;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        int cageNumber;
        Player player = (Player)sender;
        String arenaName = args[0].toLowerCase();
        Arena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            player.sendMessage(FortunePillars.parseWithPrefix("<red>Arena '<yellow>" + arenaName + "</yellow>' not found!"));
            return;
        }
        int maxCages = this.plugin.getConfigManager().getMaxCages();
        try {
            cageNumber = Integer.parseInt(args[1]);
            if (cageNumber < 1 || cageNumber > maxCages) {
                throw new NumberFormatException();
            }
        }
        catch (NumberFormatException e) {
            player.sendMessage(FortunePillars.parseWithPrefix("<red>Cage number must be between 1 and " + maxCages + "!"));
            return;
        }
        CageLocation cage = new CageLocation(player.getLocation());
        arena.setCageLocation(cageNumber, cage);
        player.sendMessage(FortunePillars.parseWithPrefix("<green>Cage <yellow>#" + cageNumber + "</yellow> set for arena '<yellow>" + arenaName + "</yellow>'!"));
        int totalCages = arena.getCageLocations().size();
        player.sendMessage(FortunePillars.parseWithPrefix("<gray>Cages set: <yellow>" + totalCages + "/" + maxCages + "</yellow> <dark_gray>(minimum " + arena.getMinPlayers() + " required)</dark_gray>"));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return this.plugin.getArenaManager().getAllArenas().stream().map(Arena::getName).filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2) {
            int maxCages = this.plugin.getConfigManager().getMaxCages();
            return IntStream.rangeClosed(1, maxCages).mapToObj(String::valueOf).filter(num -> num.startsWith(args[1])).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

