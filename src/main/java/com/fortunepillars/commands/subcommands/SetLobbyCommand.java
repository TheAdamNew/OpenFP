/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package com.fortunepillars.commands.subcommands;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.commands.subcommands.SubCommand;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLobbyCommand
implements SubCommand {
    private final FortunePillars plugin;

    public SetLobbyCommand(FortunePillars plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "setlobby";
    }

    @Override
    public String getDescription() {
        return "Set the main lobby location";
    }

    @Override
    public String getUsage() {
        return "";
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
        return 0;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        this.plugin.getConfigManager().setLobbyLocation(player.getLocation());
        player.sendMessage(FortunePillars.parseWithPrefix("<green>Main lobby location has been set!"));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}

