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
import java.util.List;
import org.bukkit.command.CommandSender;

public class CreateCommand
implements SubCommand {
    private final FortunePillars plugin;

    public CreateCommand(FortunePillars plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getDescription() {
        return "Create a new arena";
    }

    @Override
    public String getUsage() {
        return "<name>";
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
        if (this.plugin.getArenaManager().getArena(arenaName) != null) {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>Arena '<yellow>" + arenaName + "</yellow>' already exists!"));
            return;
        }
        if (!arenaName.matches("^[a-zA-Z0-9_-]+$")) {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>Arena name can only contain letters, numbers, underscores and hyphens!"));
            return;
        }
        Arena arena = this.plugin.getArenaManager().createArena(arenaName);
        if (arena != null) {
            int maxCages = this.plugin.getConfigManager().getMaxCages();
            sender.sendMessage(FortunePillars.parseWithPrefix("<green>Arena '<yellow>" + arenaName + "</yellow>' has been created!"));
            sender.sendMessage(FortunePillars.parseWithPrefix("<gray>Next steps:"));
            sender.sendMessage(FortunePillars.parseWithPrefix("<gray>1. <yellow>/tof setpos " + arenaName + " 1</yellow> - Set corner 1"));
            sender.sendMessage(FortunePillars.parseWithPrefix("<gray>2. <yellow>/tof setpos " + arenaName + " 2</yellow> - Set corner 2"));
            sender.sendMessage(FortunePillars.parseWithPrefix("<gray>3. <yellow>/tof setcage " + arenaName + " 1-" + maxCages + "</yellow> - Set spawn cages"));
            sender.sendMessage(FortunePillars.parseWithPrefix("<gray>4. <yellow>/tof setspectatorspawn " + arenaName + "</yellow> - Set spectator spawn"));
            sender.sendMessage(FortunePillars.parseWithPrefix("<gray>5. <yellow>/tof save " + arenaName + "</yellow> - Save arena schematic"));
        } else {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>Failed to create arena!"));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("<arena_name>");
        }
        return List.of();
    }
}

