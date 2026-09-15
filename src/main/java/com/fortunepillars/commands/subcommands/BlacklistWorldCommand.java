/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.World
 *  org.bukkit.command.CommandSender
 *  org.bukkit.generator.WorldInfo
 */
package com.fortunepillars.commands.subcommands;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.commands.subcommands.SubCommand;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.generator.WorldInfo;

public class BlacklistWorldCommand
implements SubCommand {
    private final FortunePillars plugin;

    public BlacklistWorldCommand(FortunePillars plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "blacklistworld";
    }

    @Override
    public String getDescription() {
        return "Manage world blacklist";
    }

    @Override
    public String getUsage() {
        return "<add|remove|list> [world]";
    }

    @Override
    public String getPermission() {
        return "fortunepillars.admin.blacklist";
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
        String action = args[0].toLowerCase();
        if (action.equals("add")) {
            if (args.length < 2) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>Usage: /tof blacklistworld add <world>"));
                return;
            }
            String worldNameAdd = args[1];
            World worldAdd = Bukkit.getWorld((String)worldNameAdd);
            if (worldAdd == null) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>World not found: <yellow>" + worldNameAdd));
                return;
            }
            this.plugin.getConfigManager().addBlacklistedWorld(worldAdd.getName());
            sender.sendMessage(FortunePillars.parseWithPrefix("<green>World <yellow>" + worldAdd.getName() + "</yellow> added to blacklist!"));
            sender.sendMessage(FortunePillars.parseWithPrefix("<gray>Plugin is now disabled in that world."));
        } else if (action.equals("remove")) {
            if (args.length < 2) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>Usage: /tof blacklistworld remove <world>"));
                return;
            }
            String worldNameRemove = args[1];
            this.plugin.getConfigManager().removeBlacklistedWorld(worldNameRemove);
            sender.sendMessage(FortunePillars.parseWithPrefix("<green>World <yellow>" + worldNameRemove + "</yellow> removed from blacklist!"));
            sender.sendMessage(FortunePillars.parseWithPrefix("<gray>Plugin is now enabled in that world."));
        } else if (action.equals("list")) {
            List<String> blacklist = this.plugin.getConfigManager().getBlacklistedWorlds();
            if (blacklist.isEmpty()) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<yellow>No worlds are blacklisted."));
                return;
            }
            sender.sendMessage(FortunePillars.parse("<gold><bold>\u2550\u2550\u2550 Blacklisted Worlds \u2550\u2550\u2550</bold></gold>"));
            for (String world : blacklist) {
                boolean exists = Bukkit.getWorld((String)world) != null;
                String status = exists ? "<green>\u2713" : "<red>\u2717 (not loaded)";
                sender.sendMessage(FortunePillars.parse("<gray>\u2022 <yellow>" + world + " " + status));
            }
            sender.sendMessage(FortunePillars.parse("<gold><bold>\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550</bold></gold>"));
        } else {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>Usage: /tof blacklistworld <add|remove|list> [world]"));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("add", "remove", "list").stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
            return Bukkit.getWorlds().stream().map(WorldInfo::getName).filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            return this.plugin.getConfigManager().getBlacklistedWorlds().stream().filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        return new ArrayList<String>();
    }
}

