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
import com.fortunepillars.gui.CosmeticsGUI;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CosmeticsCommand
implements SubCommand {
    private final FortunePillars plugin;

    public CosmeticsCommand(FortunePillars plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "cosmetics";
    }

    @Override
    public String getDescription() {
        return "Open the cosmetics menu";
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getPermission() {
        return "fortunepillars.cosmetics";
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
        if (!this.plugin.getConfigManager().areCosmeticsEnabled()) {
            player.sendMessage(FortunePillars.parseWithPrefix("<red>Cosmetics are currently disabled!"));
            return;
        }
        new CosmeticsGUI(this.plugin, player).open();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}

