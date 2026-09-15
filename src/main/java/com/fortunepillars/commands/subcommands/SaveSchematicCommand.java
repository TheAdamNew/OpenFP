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
import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;

public class SaveSchematicCommand
implements SubCommand {
    private final FortunePillars plugin;

    public SaveSchematicCommand(FortunePillars plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "saveschematic";
    }

    @Override
    public String getDescription() {
        return "Save arena schematic for regeneration";
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
        String arenaName = args[0];
        Arena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>Arena not found: <yellow>" + arenaName));
            return;
        }
        if (!arena.isSetupComplete()) {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red><bold>Arena is not fully setup!</bold></red>"));
            sender.sendMessage(FortunePillars.parseWithPrefix("<yellow>Required settings:"));
            sender.sendMessage(FortunePillars.parseWithPrefix("<gray>- Position 1: " + (arena.getPos1() != null ? "<green>\u2713" : "<red>\u2717 Missing")));
            sender.sendMessage(FortunePillars.parseWithPrefix("<gray>- Position 2: " + (arena.getPos2() != null ? "<green>\u2713" : "<red>\u2717 Missing")));
            sender.sendMessage(FortunePillars.parseWithPrefix("<gray>- Spectator Spawn: " + (arena.getSpectatorSpawn() != null ? "<green>\u2713" : "<red>\u2717 Missing")));
            sender.sendMessage(FortunePillars.parseWithPrefix("<gray>- Cages: " + arena.getCageLocations().size() + "/" + arena.getMinPlayers() + (arena.getCageLocations().size() >= arena.getMinPlayers() ? " <green>\u2713" : " <red>\u2717 Need more")));
            return;
        }
        int volume = Math.abs((arena.getPos1().getBlockX() - arena.getPos2().getBlockX()) * (arena.getPos1().getBlockY() - arena.getPos2().getBlockY()) * (arena.getPos1().getBlockZ() - arena.getPos2().getBlockZ()));
        sender.sendMessage(FortunePillars.parseWithPrefix("<yellow>Saving schematic for arena: <gold>" + arenaName + "</gold>..."));
        sender.sendMessage(FortunePillars.parseWithPrefix("<gray>Arena volume: <white>~" + volume + " blocks"));
        sender.sendMessage(FortunePillars.parseWithPrefix("<gray>This may take a moment for large arenas..."));
        long startTime = System.currentTimeMillis();
        this.plugin.getSchematicManager().saveArena(arena, success -> {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            if (success.booleanValue()) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<green><bold>\u2713 Schematic saved successfully!</bold></green>"));
                sender.sendMessage(FortunePillars.parseWithPrefix("<gray>Arena: <yellow>" + arenaName));
                sender.sendMessage(FortunePillars.parseWithPrefix("<gray>Time taken: <white>" + duration + "ms <gray>(" + (double)duration / 1000.0 + "s)"));
                sender.sendMessage(FortunePillars.parseWithPrefix("<gray>File: <white>plugins/FortunePillars/schematics/" + arenaName + ".fpschem"));
                sender.sendMessage(FortunePillars.parseWithPrefix("<green>Arena will now regenerate after each match!"));
                String info = this.plugin.getSchematicManager().getSchematicInfo(arenaName);
                sender.sendMessage(FortunePillars.parseWithPrefix("<gray>Info: <white>" + info));
                this.plugin.getLogger().info("Schematic saved for " + arenaName + " in " + duration + "ms");
            } else {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red><bold>\u2717 Failed to save schematic for: <yellow>" + arenaName + "</bold></red>"));
                sender.sendMessage(FortunePillars.parseWithPrefix("<gray>Check console for detailed errors."));
                sender.sendMessage(FortunePillars.parseWithPrefix("<yellow>Common issues:"));
                sender.sendMessage(FortunePillars.parseWithPrefix("<gray>- World not loaded"));
                sender.sendMessage(FortunePillars.parseWithPrefix("<gray>- Insufficient permissions"));
                sender.sendMessage(FortunePillars.parseWithPrefix("<gray>- Disk space full"));
            }
        });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return this.plugin.getArenaManager().getArenaNames();
        }
        return Collections.emptyList();
    }
}

