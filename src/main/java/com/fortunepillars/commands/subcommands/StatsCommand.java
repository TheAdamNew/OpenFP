/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 */
package com.fortunepillars.commands.subcommands;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.commands.subcommands.BaseSubCommand;
import com.fortunepillars.player.PlayerManager;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class StatsCommand
extends BaseSubCommand {
    public StatsCommand(FortunePillars plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public String getDescription() {
        return "View player statistics";
    }

    @Override
    public String getUsage() {
        return "/tof stats [player]";
    }

    @Override
    public String getPermission() {
        return "fortunepillars.stats";
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public int getMinArgs() {
        return 0;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        OfflinePlayer target;
        if (args.length > 0) {
            target = Bukkit.getOfflinePlayer((String)args[0]);
            if (!target.hasPlayedBefore() && !target.isOnline()) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>Player not found!"));
                return;
            }
        } else if (sender instanceof Player) {
            target = (Player)sender;
        } else {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>Please specify a player!"));
            return;
        }
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            PlayerManager.PlayerStats stats = this.plugin.getDatabase().getPlayerStats(target.getUniqueId());
            this.plugin.getServer().getScheduler().runTask((Plugin)this.plugin, () -> this.sendStats(sender, target.getName(), stats));
        });
    }

    private void sendStats(CommandSender sender, String playerName, PlayerManager.PlayerStats stats) {
        sender.sendMessage(FortunePillars.parse(""));
        sender.sendMessage(FortunePillars.parse("<gradient:#FFD700:#FFA500><bold>\u2550\u2550 " + playerName + "'s Stats \u2550\u2550</bold></gradient>"));
        sender.sendMessage(FortunePillars.parse(""));
        sender.sendMessage(FortunePillars.parse("<yellow>\u2694 Wins:</yellow> <white>" + stats.getWins() + "</white>"));
        sender.sendMessage(FortunePillars.parse("<yellow>\u2620 Kills:</yellow> <white>" + stats.getKills() + "</white>"));
        sender.sendMessage(FortunePillars.parse("<yellow>\ud83d\udc80 Deaths:</yellow> <white>" + stats.getDeaths() + "</white>"));
        sender.sendMessage(FortunePillars.parse("<yellow>\ud83d\udcca K/D Ratio:</yellow> <white>" + String.format("%.2f", stats.getKDR()) + "</white>"));
        sender.sendMessage(FortunePillars.parse("<yellow>\ud83c\udfae Games Played:</yellow> <white>" + stats.getGamesPlayed() + "</white>"));
        sender.sendMessage(FortunePillars.parse("<yellow>\ud83d\udd25 Current Winstreak:</yellow> <white>" + stats.getWinstreak() + "</white>"));
        sender.sendMessage(FortunePillars.parse("<yellow> Best Winstreak:</yellow> <white>" + stats.getHighestWinstreak() + "</white>"));
        sender.sendMessage(FortunePillars.parse("<yellow>\ud83d\udcc8 Win Rate:</yellow> <white>" + String.format("%.1f", stats.getWinRate()) + "%</white>"));
        sender.sendMessage(FortunePillars.parse(""));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

