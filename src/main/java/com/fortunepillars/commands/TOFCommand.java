/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.event.ClickEvent
 *  net.kyori.adventure.text.event.HoverEvent
 *  net.kyori.adventure.text.event.HoverEventSource
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.EntityType
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.TNTPrimed
 *  org.bukkit.plugin.Plugin
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.fortunepillars.commands;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.commands.subcommands.ArenasCommand;
import com.fortunepillars.commands.subcommands.BlacklistWorldCommand;
import com.fortunepillars.commands.subcommands.CreateCommand;
import com.fortunepillars.commands.subcommands.DeleteCommand;
import com.fortunepillars.commands.subcommands.DisableArenaCommand;
import com.fortunepillars.commands.subcommands.EnableArenaCommand;
import com.fortunepillars.commands.subcommands.ForceEndCommand;
import com.fortunepillars.commands.subcommands.ForceSchematicCommand;
import com.fortunepillars.commands.subcommands.ForceStartCommand;
import com.fortunepillars.commands.subcommands.HelpCommand;
import com.fortunepillars.commands.subcommands.InfoCommand;
import com.fortunepillars.commands.subcommands.JoinCommand;
import com.fortunepillars.commands.subcommands.JoinGUICommand;
import com.fortunepillars.commands.subcommands.KickCommand;
import com.fortunepillars.commands.subcommands.LeaderboardCommand;
import com.fortunepillars.commands.subcommands.LeaveCommand;
import com.fortunepillars.commands.subcommands.ListCommand;
import com.fortunepillars.commands.subcommands.LootModeCommand;
import com.fortunepillars.commands.subcommands.ReloadCommand;
import com.fortunepillars.commands.subcommands.SaveSchematicCommand;
import com.fortunepillars.commands.subcommands.ScoreboardToggleCommand;
import com.fortunepillars.commands.subcommands.SetCageCommand;
import com.fortunepillars.commands.subcommands.SetGameModeCommand;
import com.fortunepillars.commands.subcommands.SetLobbyCommand;
import com.fortunepillars.commands.subcommands.SetMaxPlayersCommand;
import com.fortunepillars.commands.subcommands.SetMinPlayersCommand;
import com.fortunepillars.commands.subcommands.SetPosCommand;
import com.fortunepillars.commands.subcommands.SetSpectatorSpawnCommand;
import com.fortunepillars.commands.subcommands.SpectateCommand;
import com.fortunepillars.commands.subcommands.StatsCommand;
import com.fortunepillars.commands.subcommands.SubCommand;
import com.fortunepillars.commands.subcommands.TeleportToArenaCommand;
import com.fortunepillars.commands.subcommands.VoteCommand;
import com.fortunepillars.game.loot.LootMode;
import com.fortunepillars.game.modes.TNTRainMode;
import com.fortunepillars.gui.MainMenuGUI;
import com.fortunepillars.gui.loot.LootEditorGUI;
import com.fortunepillars.player.PlayerData;
import com.fortunepillars.player.PlayerManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TOFCommand
implements CommandExecutor,
TabCompleter {
    private final FortunePillars plugin;
    private final Map<String, SubCommand> subCommands;
    private final Map<String, String> aliases;

    public TOFCommand(FortunePillars plugin) {
        this.plugin = plugin;
        this.subCommands = new LinkedHashMap<String, SubCommand>();
        this.aliases = new HashMap<String, String>();
        this.registerSubCommands();
        this.registerAliases();
    }

    private void registerSubCommands() {
        this.subCommands.put("join", new JoinCommand(this.plugin));
        this.subCommands.put("joingui", new JoinGUICommand(this.plugin));
        this.subCommands.put("leave", new LeaveCommand(this.plugin));
        this.subCommands.put("stats", new StatsCommand(this.plugin));
        this.subCommands.put("vote", new VoteCommand(this.plugin));
        this.subCommands.put("scoreboard", new ScoreboardToggleCommand(this.plugin));
        this.subCommands.put("list", new ListCommand(this.plugin));
        this.subCommands.put("arenas", new ArenasCommand(this.plugin));
        this.subCommands.put("leaderboard", new LeaderboardCommand(this.plugin));
        this.subCommands.put("spectate", new SpectateCommand(this.plugin));
        this.subCommands.put("help", new HelpCommand(this.plugin));
        this.subCommands.put("create", new CreateCommand(this.plugin));
        this.subCommands.put("delete", new DeleteCommand(this.plugin));
        this.subCommands.put("setpos", new SetPosCommand(this.plugin));
        this.subCommands.put("setcage", new SetCageCommand(this.plugin));
        this.subCommands.put("setspectatorspawn", new SetSpectatorSpawnCommand(this.plugin));
        this.subCommands.put("setlobby", new SetLobbyCommand(this.plugin));
        this.subCommands.put("saveschematic", new SaveSchematicCommand(this.plugin));
        this.subCommands.put("forcestart", new ForceStartCommand(this.plugin));
        this.subCommands.put("forceend", new ForceEndCommand(this.plugin));
        this.subCommands.put("reload", new ReloadCommand(this.plugin));
        this.subCommands.put("info", new InfoCommand(this.plugin));
        this.subCommands.put("setminplayers", new SetMinPlayersCommand(this.plugin));
        this.subCommands.put("setmaxplayers", new SetMaxPlayersCommand(this.plugin));
        this.subCommands.put("setgamemode", new SetGameModeCommand(this.plugin));
        this.subCommands.put("lootmode", new LootModeCommand(this.plugin));
        this.subCommands.put("enable", new EnableArenaCommand(this.plugin));
        this.subCommands.put("disable", new DisableArenaCommand(this.plugin));
        this.subCommands.put("tp", new TeleportToArenaCommand(this.plugin));
        this.subCommands.put("kick", new KickCommand(this.plugin));
        this.subCommands.put("forcesaveschematic", new ForceSchematicCommand(this.plugin));
        this.subCommands.put("blacklistworld", new BlacklistWorldCommand(this.plugin));
    }

    private void registerAliases() {
        this.aliases.put("lb", "leaderboard");
        this.aliases.put("top", "leaderboard");
        this.aliases.put("j", "join");
        this.aliases.put("gui", "joingui");
        this.aliases.put("menu", "joingui");
        this.aliases.put("l", "leave");
        this.aliases.put("q", "leave");
        this.aliases.put("quit", "leave");
        this.aliases.put("exit", "leave");
        this.aliases.put("s", "stats");
        this.aliases.put("v", "vote");
        this.aliases.put("c", "cosmetics");
        this.aliases.put("shop", "cosmetics");
        this.aliases.put("sb", "scoreboard");
        this.aliases.put("spec", "spectate");
        this.aliases.put("watch", "spectate");
        this.aliases.put("fs", "forcestart");
        this.aliases.put("start", "forcestart");
        this.aliases.put("fe", "forceend");
        this.aliases.put("stop", "forceend");
        this.aliases.put("i", "info");
        this.aliases.put("arena", "arenas");
        this.aliases.put("teleport", "tp");
        this.aliases.put("goto", "tp");
        this.aliases.put("?", "help");
        this.aliases.put("save", "saveschematic");
        this.aliases.put("forcesave", "forcesaveschematic");
        this.aliases.put("lootgui", "lootedit");
        this.aliases.put("editloot", "lootedit");
        this.aliases.put("le", "lootedit");
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        SubCommand subCommand;
        Player player;
        if (sender instanceof Player) {
            player = (Player)sender;
            if (this.plugin.getConfigManager().isWorldBlacklisted(player.getWorld())) {
                player.sendMessage(this.plugin.getMessages().getWithPrefix("blacklist-disabled-world"));
                return true;
            }
        }
        if (args.length == 0) {
            if (sender instanceof Player) {
                player = (Player)sender;
                new MainMenuGUI(this.plugin, player).open();
            } else {
                this.sendHelp(sender, 1);
            }
            return true;
        }
        String subCommandName = args[0].toLowerCase();
        if (subCommandName.equals("lootedit") || subCommandName.equals("lootgui") || subCommandName.equals("editloot") || subCommandName.equals("le")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>This command is for players only!"));
                return true;
            }
            Player player2 = (Player)sender;
            if (!player2.hasPermission("fortunepillars.admin")) {
                player2.sendMessage(FortunePillars.parseWithPrefix("<red>You don't have permission to use this command!"));
                return true;
            }
            new LootEditorGUI(this.plugin).openMainMenu(player2);
            player2.sendMessage(FortunePillars.parseWithPrefix("<green>Opening Loot Editor GUI..."));
            return true;
        }
        if (subCommandName.equals("testrestore")) {
            if (!sender.hasPermission("fortunepillars.admin")) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>No permission!"));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>Usage: /tof testrestore <arena>"));
                return true;
            }
            String arenaName = args[1];
            Arena arena = this.plugin.getArenaManager().getArena(arenaName);
            if (arena == null) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>Arena not found!"));
                return true;
            }
            sender.sendMessage(FortunePillars.parseWithPrefix("<yellow>Testing arena restoration..."));
            this.plugin.getSchematicManager().restoreArena(arena, () -> sender.sendMessage(FortunePillars.parseWithPrefix("<green>\u2713 Restoration complete!")));
            return true;
        }
        if (subCommandName.equals("schematicinfo")) {
            if (!sender.hasPermission("fortunepillars.admin")) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>No permission!"));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>Usage: /tof schematicinfo <arena>"));
                return true;
            }
            String arenaName = args[1];
            String info = this.plugin.getSchematicManager().getSchematicInfo(arenaName);
            sender.sendMessage(FortunePillars.parseWithPrefix("<gold>Schematic Info for " + arenaName + ":"));
            sender.sendMessage(FortunePillars.parse("<gray>" + info));
            return true;
        }
        if (subCommandName.equals("checkschematic")) {
            if (!sender.hasPermission("fortunepillars.admin")) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>No permission!"));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>Usage: /tof checkschematic <arena>"));
                return true;
            }
            String arenaName = args[1];
            Arena arena = this.plugin.getArenaManager().getArena(arenaName);
            if (arena == null) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>Arena not found: " + arenaName));
                return true;
            }
            boolean hasSchematic = this.plugin.getSchematicManager().hasSchematic(arenaName);
            sender.sendMessage(FortunePillars.parse("<gold><bold>\u2550\u2550\u2550 Schematic Check \u2550\u2550\u2550</bold></gold>"));
            sender.sendMessage(FortunePillars.parse("<gray>Arena: <yellow>" + arenaName));
            sender.sendMessage(FortunePillars.parse("<gray>Setup Complete: " + (arena.isSetupComplete() ? "<green>Yes \u2713" : "<red>No \u2717")));
            sender.sendMessage(FortunePillars.parse("<gray>Has Schematic: " + (hasSchematic ? "<green>Yes \u2713" : "<red>No \u2717")));
            if (arena.isSetupComplete()) {
                sender.sendMessage(FortunePillars.parse(""));
                sender.sendMessage(FortunePillars.parse("<gray>Position 1: " + (arena.getPos1() != null ? "<green>Set" : "<red>Not set")));
                sender.sendMessage(FortunePillars.parse("<gray>Position 2: " + (arena.getPos2() != null ? "<green>Set" : "<red>Not set")));
                sender.sendMessage(FortunePillars.parse("<gray>Spectator Spawn: " + (arena.getSpectatorSpawn() != null ? "<green>Set" : "<red>Not set")));
                sender.sendMessage(FortunePillars.parse("<gray>Cages: <yellow>" + arena.getCageLocations().size() + "</yellow>/<yellow>" + arena.getMinPlayers() + "</yellow>"));
            }
            if (arena.isSetupComplete() && !hasSchematic) {
                sender.sendMessage(FortunePillars.parse(""));
                sender.sendMessage(FortunePillars.parse("<yellow>\u26a0 Arena is ready but has no schematic!"));
                sender.sendMessage(FortunePillars.parse("<yellow>Run: <white><click:suggest_command:'/tof saveschematic " + arenaName + "'>/tof saveschematic " + arenaName + "</click>"));
            }
            sender.sendMessage(FortunePillars.parse("<gold><bold>\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550</bold></gold>"));
            return true;
        }
        if (subCommandName.equals("spawntnt")) {
            if (!sender.hasPermission("fortunepillars.admin")) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>No permission!"));
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>Players only!"));
                return true;
            }
            Player player3 = (Player)sender;
            Location loc = player3.getLocation().add(0.0, 20.0, 0.0);
            sender.sendMessage(FortunePillars.parseWithPrefix("<yellow>Spawning test TNT..."));
            try {
                TNTPrimed tnt = (TNTPrimed)loc.getWorld().spawnEntity(loc, EntityType.TNT);
                tnt.setFuseTicks(100);
                tnt.setYield(3.5f);
                sender.sendMessage(FortunePillars.parseWithPrefix("<green>\u2713 TNT spawned successfully!"));
                sender.sendMessage(FortunePillars.parseWithPrefix("<gray>UUID: " + String.valueOf(tnt.getUniqueId())));
                sender.sendMessage(FortunePillars.parseWithPrefix("<gray>Valid: " + tnt.isValid()));
            }
            catch (Exception e) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>\u2717 Failed: " + e.getMessage()));
                e.printStackTrace();
            }
            return true;
        }
        if (subCommandName.equals("debug")) {
            if (!sender.hasPermission("fortunepillars.admin")) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>No permission!"));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>Usage: /tof debug <player>"));
                return true;
            }
            Player target = Bukkit.getPlayer((String)args[1]);
            if (target == null) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>Player not found!"));
                return true;
            }
            Arena arena = this.plugin.getArenaManager().getPlayerArena(target);
            sender.sendMessage(FortunePillars.parse("<gold><bold>=== Debug: " + target.getName() + " ===</bold></gold>"));
            sender.sendMessage(FortunePillars.parse("<gray>In arena: " + (String)(arena != null ? "<green>" + arena.getName() : "<red>None")));
            if (arena != null) {
                sender.sendMessage(FortunePillars.parse("<gray>Arena state: <yellow>" + arena.getState().name()));
                sender.sendMessage(FortunePillars.parse("<gray>Is alive: " + (arena.isPlayerAlive(target.getUniqueId()) ? "<green>Yes" : "<red>No")));
                sender.sendMessage(FortunePillars.parse("<gray>Is spectator: " + (arena.isSpectator(target.getUniqueId()) ? "<green>Yes" : "<red>No")));
            }
            sender.sendMessage(FortunePillars.parse("<gray>Persistent data:"));
            sender.sendMessage(FortunePillars.parse("<gray>- In game: " + (PlayerData.isInGame(target) ? "<green>Yes" : "<red>No")));
            sender.sendMessage(FortunePillars.parse("<gray>- Is spectator: " + (PlayerData.isSpectator(target) ? "<green>Yes" : "<red>No")));
            sender.sendMessage(FortunePillars.parse("<gray>- Arena name: <yellow>" + PlayerData.getArenaName(target)));
            return true;
        }
        if (subCommandName.equals("forceremove")) {
            if (!sender.hasPermission("fortunepillars.admin")) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>No permission!"));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>Usage: /tof forceremove <player>"));
                return true;
            }
            Player target = Bukkit.getPlayer((String)args[1]);
            if (target == null) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>Player not found!"));
                return true;
            }
            sender.sendMessage(FortunePillars.parseWithPrefix("<yellow>Force removing " + target.getName() + "..."));
            this.plugin.getArenaManager().forceRemovePlayer(target);
            sender.sendMessage(FortunePillars.parseWithPrefix("<green>Player removed!"));
            return true;
        }
        if (subCommandName.equals("testtnt")) {
            if (!sender.hasPermission("fortunepillars.admin")) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>No permission!"));
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>Players only!"));
                return true;
            }
            Player player4 = (Player)sender;
            Arena arena = this.plugin.getArenaManager().getPlayerArena(player4);
            if (arena == null) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>You must be in an arena!"));
                return true;
            }
            if (!arena.isActive()) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>Arena must be active!"));
                return true;
            }
            sender.sendMessage(FortunePillars.parseWithPrefix("<yellow>Starting TNT Rain mode..."));
            this.plugin.getGameModeManager().stopGameModeTasks(arena);
            TNTRainMode tntMode = new TNTRainMode(this.plugin, arena);
            tntMode.start();
            sender.sendMessage(FortunePillars.parseWithPrefix("<green>TNT Rain started!"));
            return true;
        }
        if (subCommandName.equals("testlb")) {
            if (!sender.hasPermission("fortunepillars.admin")) {
                sender.sendMessage(FortunePillars.parseWithPrefix("<red>No permission!"));
                return true;
            }
            sender.sendMessage(FortunePillars.parseWithPrefix("<yellow>Updating leaderboard..."));
            this.plugin.getLeaderboardManager().forceUpdateSync();
            if (sender instanceof Player) {
                this.plugin.getLeaderboardManager().showLeaderboard((Player)sender, "wins");
            }
            sender.sendMessage(FortunePillars.parseWithPrefix("<green>Updated!"));
            return true;
        }
        if (subCommandName.equals("debugloot")) {
            if (!sender.hasPermission("fortunepillars.admin")) {
                sender.sendMessage("\u00a7cNo permission!");
                return true;
            }
            sender.sendMessage("\u00a76========== LOOT DEBUG ==========");
            sender.sendMessage("\u00a77Normal Multi: \u00a7f" + this.plugin.getLootManager().getNormalLootMulti().size());
            sender.sendMessage("\u00a77Balanced Multi: \u00a7f" + this.plugin.getLootManager().getBalancedLootMulti().size());
            sender.sendMessage("\u00a77OP Multi: \u00a7f" + this.plugin.getLootManager().getOpLootMulti().size());
            sender.sendMessage("\u00a77Normal Single: \u00a7f" + this.plugin.getLootManager().getNormalLootSingle().size());
            sender.sendMessage("\u00a77Balanced Single: \u00a7f" + this.plugin.getLootManager().getBalancedLootSingle().size());
            sender.sendMessage("\u00a77OP Single: \u00a7f" + this.plugin.getLootManager().getOpLootSingle().size());
            if (!(sender instanceof Player)) {
                return true;
            }
            Player player5 = (Player)sender;
            sender.sendMessage("\u00a7eGiving test loot...");
            this.plugin.getLootManager().giveLootByTier(player5, LootMode.LootTier.NORMAL, LootMode.DropStyle.SINGLE, 3);
            return true;
        }
        if (subCommandName.equals("debugdb")) {
            if (!sender.hasPermission("fortunepillars.admin")) {
                sender.sendMessage("\u00a7cNo permission!");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("\u00a7cUsage: /tof debugdb <player>");
                return true;
            }
            Player target = Bukkit.getPlayer((String)args[1]);
            if (target == null) {
                sender.sendMessage("\u00a7cPlayer not found!");
                return true;
            }
            sender.sendMessage("\u00a7eChecking database for " + target.getName() + "...");
            Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
                PlayerManager.PlayerStats stats = this.plugin.getDatabase().getPlayerStats(target.getUniqueId());
                Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
                    if (stats == null) {
                        sender.sendMessage("\u00a7c\u2717 No stats found!");
                        sender.sendMessage("\u00a7eCreating entry...");
                        this.plugin.getDatabase().createPlayerData(target.getUniqueId(), target.getName());
                    } else {
                        sender.sendMessage("\u00a7a\u2713 Stats found:");
                        sender.sendMessage("  \u00a77Wins: \u00a7f" + stats.wins());
                        sender.sendMessage("  \u00a77Kills: \u00a7f" + stats.kills());
                        sender.sendMessage("  \u00a77Deaths: \u00a7f" + stats.deaths());
                        sender.sendMessage("  \u00a77Games: \u00a7f" + stats.gamesPlayed());
                    }
                });
            });
            return true;
        }
        if (subCommandName.equals("testwin")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("\u00a7cOnly players!");
                return true;
            }
            Player player6 = (Player)sender;
            if (!sender.hasPermission("fortunepillars.admin")) {
                sender.sendMessage("\u00a7cNo permission!");
                return true;
            }
            sender.sendMessage("\u00a7eAdding test win...");
            this.plugin.getDatabase().createPlayerData(player6.getUniqueId(), player6.getName());
            this.plugin.getDatabase().addWin(player6.getUniqueId());
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
                PlayerManager.PlayerStats stats = this.plugin.getDatabase().getPlayerStats(player6.getUniqueId());
                Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
                    if (stats != null) {
                        sender.sendMessage("\u00a7a\u2713 Win added! Total: " + stats.wins());
                    } else {
                        sender.sendMessage("\u00a7c\u2717 Failed!");
                    }
                });
            }), 40L);
            return true;
        }
        if (subCommandName.equals("forcedrop")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("\u00a7cOnly players!");
                return true;
            }
            Player player7 = (Player)sender;
            if (!sender.hasPermission("fortunepillars.admin")) {
                sender.sendMessage("\u00a7cNo permission!");
                return true;
            }
            Arena arena = this.plugin.getArenaManager().getPlayerArena(player7);
            if (arena == null) {
                sender.sendMessage("\u00a7cYou're not in an arena!");
                return true;
            }
            if (!arena.isActive()) {
                sender.sendMessage("\u00a7cArena must be active!");
                return true;
            }
            LootMode mode = arena.getLootMode();
            int itemCount = mode.getRandomItemCount();
            sender.sendMessage("\u00a7a========================================");
            sender.sendMessage("\u00a7e\ud83c\udf81 FORCE DROPPING LOOT");
            sender.sendMessage("\u00a7a========================================");
            sender.sendMessage("\u00a77Mode: \u00a7f" + mode.getDisplayName());
            sender.sendMessage("\u00a77Tier: \u00a7f" + String.valueOf((Object)mode.getTier()));
            sender.sendMessage("\u00a77Style: \u00a7f" + String.valueOf((Object)mode.getStyle()));
            sender.sendMessage("\u00a77Items: \u00a7f" + itemCount);
            sender.sendMessage("\u00a7a========================================");
            for (Player p : arena.getAlivePlayers()) {
                this.plugin.getLootManager().giveLootByTier(p, mode.getTier(), mode.getStyle(), itemCount);
            }
            sender.sendMessage("\u00a7a\u2713 Loot dropped to " + arena.getAlivePlayers().size() + " players!");
            return true;
        }
        if (subCommandName.equals("checktask")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("\u00a7cOnly players!");
                return true;
            }
            Player player8 = (Player)sender;
            if (!sender.hasPermission("fortunepillars.admin")) {
                sender.sendMessage("\u00a7cNo permission!");
                return true;
            }
            Arena arena = this.plugin.getArenaManager().getPlayerArena(player8);
            if (arena == null) {
                sender.sendMessage("\u00a7cYou're not in an arena!");
                return true;
            }
            sender.sendMessage("\u00a76========== TASK DEBUG ==========");
            sender.sendMessage("\u00a77Arena: \u00a7f" + arena.getName());
            sender.sendMessage("\u00a77State: \u00a7f" + String.valueOf((Object)arena.getState()));
            sender.sendMessage("\u00a77Active: \u00a7f" + arena.isActive());
            sender.sendMessage("\u00a77Loot Mode: \u00a7f" + arena.getLootMode().getDisplayName());
            sender.sendMessage("\u00a77Alive Players: \u00a7f" + arena.getAlivePlayers().size());
            sender.sendMessage("\u00a76================================");
            return true;
        }
        if (subCommandName.equals("updateleaderboard")) {
            if (!sender.hasPermission("fortunepillars.admin")) {
                sender.sendMessage("\u00a7cNo permission!");
                return true;
            }
            sender.sendMessage("\u00a7eUpdating all leaderboards...");
            Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
                this.plugin.getLeaderboardManager().updateLeaderboards();
                Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> sender.sendMessage("\u00a7a\u2713 Leaderboards updated!"));
            });
            return true;
        }
        if (this.aliases.containsKey(subCommandName)) {
            subCommandName = this.aliases.get(subCommandName);
        }
        if ((subCommand = this.subCommands.get(subCommandName)) == null) {
            sender.sendMessage(this.plugin.getMessages().getWithPrefix("invalid-command", "command", args[0]));
            this.sendSuggestions(sender, args[0]);
            return true;
        }
        if (subCommand.isPlayerOnly() && !(sender instanceof Player)) {
            sender.sendMessage(this.plugin.getMessages().getWithPrefix("player-only"));
            return true;
        }
        if (!sender.hasPermission(subCommand.getPermission())) {
            sender.sendMessage(this.plugin.getMessages().getWithPrefix("no-permission"));
            return true;
        }
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        if (subArgs.length < subCommand.getMinArgs()) {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>Usage: <yellow>/tof " + subCommandName + " " + subCommand.getUsage()));
            return true;
        }
        try {
            subCommand.execute(sender, subArgs);
        }
        catch (Exception e) {
            sender.sendMessage(FortunePillars.parseWithPrefix("<red>Command error!"));
            this.plugin.getLogger().severe("Error executing '" + subCommandName + "': " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String subCommandName;
        if (args.length == 1) {
            ArrayList<String> completions = new ArrayList<String>();
            this.subCommands.entrySet().stream().filter(entry -> sender.hasPermission(((SubCommand)entry.getValue()).getPermission())).map(Map.Entry::getKey).filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).forEach(completions::add);
            this.aliases.entrySet().stream().filter(entry -> {
                SubCommand cmd = this.subCommands.get(entry.getValue());
                return cmd != null && sender.hasPermission(cmd.getPermission());
            }).map(Map.Entry::getKey).filter(alias -> alias.toLowerCase().startsWith(args[0].toLowerCase())).forEach(completions::add);
            if (sender.hasPermission("fortunepillars.admin")) {
                List<String> adminCommands = Arrays.asList("lootedit", "lootgui", "editloot", "le", "checkschematic", "schematicinfo", "testrestore", "debug", "forceremove", "testtnt", "testlb", "debugloot", "debugdb", "testwin", "forcedrop", "checktask", "updateleaderboard", "spawntnt");
                for (String cmd : adminCommands) {
                    if (!cmd.startsWith(args[0].toLowerCase())) continue;
                    completions.add(cmd);
                }
            }
            return completions.stream().distinct().sorted().collect(Collectors.toList());
        }
        if (args.length == 2) {
            subCommandName = args[0].toLowerCase();
            List<String> arenaCommands = Arrays.asList("checkschematic", "schematicinfo", "testrestore");
            if (arenaCommands.contains(subCommandName)) {
                return this.plugin.getArenaManager().getArenaNames().stream().filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
            }
            List<String> playerCommands = Arrays.asList("debug", "forceremove", "debugdb");
            if (playerCommands.contains(subCommandName)) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
            }
        }
        if (args.length > 1) {
            SubCommand subCommand;
            subCommandName = args[0].toLowerCase();
            if (this.aliases.containsKey(subCommandName)) {
                subCommandName = this.aliases.get(subCommandName);
            }
            if ((subCommand = this.subCommands.get(subCommandName)) != null && sender.hasPermission(subCommand.getPermission())) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                List<String> completions = subCommand.tabComplete(sender, subArgs);
                return completions != null ? completions : Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    private void sendHelp(CommandSender sender, int page) {
        ArrayList<Component> helpMessages = new ArrayList<Component>();
        int totalPages = sender.hasPermission("fortunepillars.admin") ? 3 : 1;
        page = Math.max(1, Math.min(page, totalPages));
        helpMessages.add(Component.empty());
        helpMessages.add(FortunePillars.parse("<gradient:#FFD700:#FFA500><bold>\u2550\u2550\u2550\u2550\u2550\u2550 FortunePillars Help \u2550\u2550\u2550\u2550\u2550\u2550</bold></gradient>"));
        helpMessages.add(FortunePillars.parse("<gray>Page <yellow>" + page + "</yellow>/<yellow>" + totalPages + "</yellow></gray>"));
        helpMessages.add(Component.empty());
        if (page == 1) {
            helpMessages.add(FortunePillars.parse("<yellow><bold>Player Commands:</bold></yellow>"));
            helpMessages.add(this.createClickableCommand("/tof", "Open main menu", "/tof"));
            helpMessages.add(this.createClickableCommand("/tof join [arena]", "Join an arena", "/tof join "));
            helpMessages.add(this.createClickableCommand("/tof leave", "Leave arena", "/tof leave"));
            helpMessages.add(this.createClickableCommand("/tof stats", "View stats", "/tof stats"));
            helpMessages.add(this.createClickableCommand("/tof vote", "Vote for mode", "/tof vote"));
            helpMessages.add(this.createClickableCommand("/tof leaderboard", "View leaderboard", "/tof leaderboard"));
        }
        if (page == 2 && sender.hasPermission("fortunepillars.admin")) {
            helpMessages.add(FortunePillars.parse("<red><bold>Admin Commands:</bold></red>"));
            helpMessages.add(this.createClickableCommand("/tof create <name>", "Create arena", "/tof create "));
            helpMessages.add(this.createClickableCommand("/tof setpos <arena> <1|2>", "Set bounds", "/tof setpos "));
            helpMessages.add(this.createClickableCommand("/tof setcage <arena> <#>", "Set cage", "/tof setcage "));
            helpMessages.add(this.createClickableCommand("/tof saveschematic <arena>", "Save schematic", "/tof saveschematic "));
            helpMessages.add(this.createClickableCommand("/tof checkschematic <arena>", "Check schematic", "/tof checkschematic "));
            helpMessages.add(this.createClickableCommand("/tof forcestart <arena>", "Force start", "/tof forcestart "));
            helpMessages.add(this.createClickableCommand("/tof debug <player>", "Debug player", "/tof debug "));
        }
        if (page == 3 && sender.hasPermission("fortunepillars.admin")) {
            helpMessages.add(FortunePillars.parse("<gold><bold>Loot Commands:</bold></gold>"));
            helpMessages.add(this.createClickableCommand("/tof lootedit", "Open loot editor GUI", "/tof lootedit"));
            helpMessages.add(this.createClickableCommand("/tof debugloot", "Debug loot tables", "/tof debugloot"));
            helpMessages.add(this.createClickableCommand("/tof forcedrop", "Force drop loot (in arena)", "/tof forcedrop"));
            helpMessages.add(Component.empty());
            helpMessages.add(FortunePillars.parse("<gray><italic>Use /tof lootedit for easy loot management!</italic></gray>"));
        }
        helpMessages.add(Component.empty());
        helpMessages.add(FortunePillars.parse("<gradient:#FFD700:#FFA500><bold>\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550</bold></gradient>"));
        helpMessages.forEach(arg_0 -> sender.sendMessage(arg_0));
    }

    private Component createClickableCommand(String command, String description, String suggestCommand) {
        return FortunePillars.parse("<gold>" + command + "</gold> <dark_gray>-</dark_gray> <gray>" + description).clickEvent(ClickEvent.suggestCommand((String)suggestCommand)).hoverEvent((HoverEventSource)HoverEvent.showText((Component)FortunePillars.parse("<yellow>Click to use")));
    }

    private void sendSuggestions(CommandSender sender, String input) {
        List suggestions = this.subCommands.keySet().stream().filter(cmd -> {
            SubCommand subCmd = this.subCommands.get(cmd);
            return sender.hasPermission(subCmd.getPermission());
        }).filter(cmd -> this.isSimilar((String)cmd, input.toLowerCase())).limit(3L).collect(Collectors.toList());
        if (!suggestions.isEmpty()) {
            sender.sendMessage(FortunePillars.parse("<gray>Did you mean: <yellow>" + String.join((CharSequence)", ", suggestions) + "</yellow>?"));
        }
    }

    private boolean isSimilar(String s1, String s2) {
        return s1.startsWith(s2) || s2.startsWith(s1) || s1.contains(s2) || s2.contains(s1) || this.levenshteinDistance(s1, s2) <= 2;
    }

    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); ++i) {
            for (int j = 0; j <= s2.length(); ++j) {
                dp[i][j] = i == 0 ? j : (j == 0 ? i : Math.min(dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1), Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1)));
            }
        }
        return dp[s1.length()][s2.length()];
    }

    public Map<String, SubCommand> getSubCommands() {
        return Collections.unmodifiableMap(this.subCommands);
    }

    public Map<String, String> getAliases() {
        return Collections.unmodifiableMap(this.aliases);
    }

    public SubCommand getSubCommand(String name) {
        String commandName = this.aliases.getOrDefault(name.toLowerCase(), name.toLowerCase());
        return this.subCommands.get(commandName);
    }
}

