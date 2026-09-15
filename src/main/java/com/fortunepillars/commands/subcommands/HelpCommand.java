/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TextComponent
 *  net.kyori.adventure.text.event.ClickEvent
 *  net.kyori.adventure.text.event.HoverEvent
 *  net.kyori.adventure.text.event.HoverEventSource
 *  org.bukkit.command.CommandSender
 */
package com.fortunepillars.commands.subcommands;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.commands.subcommands.SubCommand;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import org.bukkit.command.CommandSender;

public class HelpCommand
implements SubCommand {
    private final FortunePillars plugin;

    public HelpCommand(FortunePillars plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Show help information";
    }

    @Override
    public String getUsage() {
        return "[page]";
    }

    @Override
    public String getPermission() {
        return "fortunepillars.help";
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
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException e) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>Invalid page number!"));
                return;
            }
        }
        this.sendHelp(sender, page);
    }

    private void sendHelp(CommandSender sender, int page) {
        ArrayList<Component> helpMessages = new ArrayList<Component>();
        int totalPages = sender.hasPermission("fortunepillars.admin") ? 2 : 1;
        page = Math.max(1, Math.min(page, totalPages));
        helpMessages.add(Component.empty());
        helpMessages.add(FortunePillars.parse("<gradient:#FFD700:#FFA500><bold>\u2550\u2550\u2550\u2550\u2550\u2550 FortunePillars Help \u2550\u2550\u2550\u2550\u2550\u2550</bold></gradient>"));
        helpMessages.add(FortunePillars.parse("<gray>Page <yellow>" + page + "</yellow>/<yellow>" + totalPages + "</yellow></gray>"));
        helpMessages.add(Component.empty());
        if (page == 1) {
            helpMessages.add(FortunePillars.parse("<yellow><bold>Player Commands:</bold></yellow>"));
            helpMessages.add(this.createClickableCommand("/tof join [arena]", "Join an arena", "/tof join "));
            helpMessages.add(this.createClickableCommand("/tof leave", "Leave current arena", "/tof leave"));
            helpMessages.add(this.createClickableCommand("/tof stats [player]", "View statistics", "/tof stats "));
            helpMessages.add(this.createClickableCommand("/tof vote <mode>", "Vote for game mode", "/tof vote "));
            helpMessages.add(this.createClickableCommand("/tof cosmetics", "Open cosmetics menu", "/tof cosmetics"));
            helpMessages.add(this.createClickableCommand("/tof arenas", "List all arenas", "/tof arenas"));
            helpMessages.add(this.createClickableCommand("/tof leaderboard [type]", "View leaderboard", "/tof leaderboard "));
            helpMessages.add(this.createClickableCommand("/tof spectate <arena>", "Spectate a game", "/tof spectate "));
            helpMessages.add(this.createClickableCommand("/tof help [page]", "Show this help", "/tof help "));
        }
        if (page == 2 && sender.hasPermission("fortunepillars.admin")) {
            int maxCages = this.plugin.getConfigManager().getMaxCages();
            helpMessages.add(FortunePillars.parse("<red><bold>Admin Commands:</bold></red>"));
            helpMessages.add(this.createClickableCommand("/tof create <name>", "Create new arena", "/tof create "));
            helpMessages.add(this.createClickableCommand("/tof delete <arena>", "Delete an arena", "/tof delete "));
            helpMessages.add(this.createClickableCommand("/tof setpos <arena> <1|2>", "Set arena bounds", "/tof setpos "));
            helpMessages.add(this.createClickableCommand("/tof setcage <arena> <1-" + maxCages + ">", "Set cage location", "/tof setcage "));
            helpMessages.add(this.createClickableCommand("/tof setspectatorspawn <arena>", "Set spectator spawn", "/tof setspectatorspawn "));
            helpMessages.add(this.createClickableCommand("/tof setlobby", "Set main lobby", "/tof setlobby"));
            helpMessages.add(this.createClickableCommand("/tof forcestart <arena>", "Force start game", "/tof forcestart "));
            helpMessages.add(this.createClickableCommand("/tof forceend <arena>", "Force end game", "/tof forceend "));
            helpMessages.add(this.createClickableCommand("/tof reload", "Reload configuration", "/tof reload"));
        }
        helpMessages.add(Component.empty());
        if (totalPages > 1) {
            TextComponent navigation = Component.empty();
            if (page > 1) {
                navigation = navigation.append(FortunePillars.parse("<yellow>[\u2190 Previous]</yellow>").clickEvent(ClickEvent.runCommand((String)("/tof help " + (page - 1)))).hoverEvent((HoverEventSource)HoverEvent.showText((Component)FortunePillars.parse("<gray>Go to page " + (page - 1)))));
            }
            navigation = navigation.append(FortunePillars.parse(" "));
            if (page < totalPages) {
                navigation = navigation.append(FortunePillars.parse("<yellow>[Next \u2192]</yellow>").clickEvent(ClickEvent.runCommand((String)("/tof help " + (page + 1)))).hoverEvent((HoverEventSource)HoverEvent.showText((Component)FortunePillars.parse("<gray>Go to page " + (page + 1)))));
            }
            helpMessages.add(navigation);
        }
        helpMessages.add(FortunePillars.parse("<gradient:#FFD700:#FFA500><bold>\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550</bold></gradient>"));
        helpMessages.forEach(arg_0 -> sender.sendMessage(arg_0));
    }

    private Component createClickableCommand(String command, String description, String suggestCommand) {
        return FortunePillars.parse("<gold>" + command + "</gold> <dark_gray>-</dark_gray> <gray>" + description + "</gray>").clickEvent(ClickEvent.suggestCommand((String)suggestCommand)).hoverEvent((HoverEventSource)HoverEvent.showText((Component)FortunePillars.parse("<yellow>Click to use this command")));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            int totalPages = sender.hasPermission("fortunepillars.admin") ? 2 : 1;
            ArrayList<String> pages = new ArrayList<String>();
            for (int i = 1; i <= totalPages; ++i) {
                pages.add(String.valueOf(i));
            }
            return pages;
        }
        return List.of();
    }
}

