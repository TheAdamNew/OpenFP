/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 */
package com.fortunepillars.commands;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.player.PlayerManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class DatabaseDebugCommand
implements CommandExecutor,
TabCompleter {
    private final FortunePillars plugin;

    public DatabaseDebugCommand(FortunePillars plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("fortunepillars.admin")) {
            sender.sendMessage("\u00a7c\u2717 You don't have permission to use this command!");
            return true;
        }
        if (args.length == 0) {
            this.sendHelpMessage(sender);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "check": {
                this.checkPlayerStats(sender, args);
                break;
            }
            case "top": {
                this.showTopPlayers(sender, args);
                break;
            }
            case "testwin": {
                this.testAddWin(sender);
                break;
            }
            case "testkill": {
                this.testAddKill(sender);
                break;
            }
            case "testgame": {
                this.testAddGame(sender);
                break;
            }
            case "create": {
                this.createPlayerEntry(sender, args);
                break;
            }
            case "reset": {
                this.resetPlayerStats(sender, args);
                break;
            }
            case "info": {
                this.showDatabaseInfo(sender);
                break;
            }
            default: {
                this.sendHelpMessage(sender);
            }
        }
        return true;
    }

    private void checkPlayerStats(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("\u00a7c\u2717 Usage: /dbdebug check <player>");
            return;
        }
        Player target = Bukkit.getPlayer((String)args[1]);
        if (target == null) {
            sender.sendMessage("\u00a7c\u2717 Player not found!");
            return;
        }
        sender.sendMessage("\u00a7e\u23f3 Checking stats for " + target.getName() + "...");
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            PlayerManager.PlayerStats stats = this.plugin.getDatabase().getPlayerStats(target.getUniqueId());
            String playerName = this.plugin.getDatabase().getPlayerName(target.getUniqueId());
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
                sender.sendMessage("\u00a76========================================");
                sender.sendMessage("\u00a7e\u00a7l\ud83d\udcca PLAYER STATS DEBUG");
                sender.sendMessage("\u00a76========================================");
                sender.sendMessage("");
                sender.sendMessage("\u00a77Player: \u00a7f" + target.getName());
                sender.sendMessage("\u00a77UUID: \u00a7f" + String.valueOf(target.getUniqueId()));
                sender.sendMessage("\u00a77DB Name: \u00a7f" + playerName);
                sender.sendMessage("");
                if (stats == null) {
                    sender.sendMessage("\u00a7c\u2717 NO STATS FOUND IN DATABASE!");
                    sender.sendMessage("");
                    sender.sendMessage("\u00a77This means:");
                    sender.sendMessage("  \u00a7c\u2022 Player entry doesn't exist");
                    sender.sendMessage("  \u00a7c\u2022 Database query failed");
                    sender.sendMessage("  \u00a7c\u2022 Connection issue");
                    sender.sendMessage("");
                    sender.sendMessage("\u00a7eTry: /dbdebug create " + target.getName());
                } else {
                    sender.sendMessage("\u00a7a\u2713 STATS FOUND:");
                    sender.sendMessage("");
                    sender.sendMessage("  \u00a77Wins: \u00a7f" + stats.wins());
                    sender.sendMessage("  \u00a77Kills: \u00a7f" + stats.kills());
                    sender.sendMessage("  \u00a77Deaths: \u00a7f" + stats.deaths());
                    sender.sendMessage("  \u00a77Games Played: \u00a7f" + stats.gamesPlayed());
                    sender.sendMessage("  \u00a77Current Winstreak: \u00a7f" + stats.winstreak());
                    sender.sendMessage("  \u00a77Highest Winstreak: \u00a7f" + stats.highestWinstreak());
                    sender.sendMessage("");
                    sender.sendMessage("\u00a7e\u00a7lCalculated Stats:");
                    sender.sendMessage("  \u00a77K/D Ratio: \u00a7f" + String.format("%.2f", stats.getKDR()));
                    sender.sendMessage("  \u00a77Win Rate: \u00a7f" + String.format("%.1f%%", stats.getWinRate()));
                }
                sender.sendMessage("\u00a76========================================");
            });
        });
    }

    private void showTopPlayers(CommandSender sender, String[] args) {
        String statType = "wins";
        int limit = 10;
        if (args.length >= 2) {
            statType = args[1].toLowerCase();
        }
        if (args.length >= 3) {
            try {
                limit = Integer.parseInt(args[2]);
                if (limit < 1 || limit > 100) {
                    limit = 10;
                }
            }
            catch (NumberFormatException e) {
                sender.sendMessage("\u00a7c\u2717 Invalid limit! Using 10.");
            }
        }
        String finalStatType = statType;
        int finalLimit = limit;
        sender.sendMessage("\u00a7e\u23f3 Loading top " + finalLimit + " players by " + finalStatType + "...");
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            Map<UUID, Integer> topPlayers = this.plugin.getDatabase().getTopPlayers(finalStatType, finalLimit);
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
                sender.sendMessage("\u00a76========================================");
                sender.sendMessage("\u00a7e\u00a7l\ud83c\udfc6 TOP " + finalLimit + " PLAYERS - " + finalStatType.toUpperCase());
                sender.sendMessage("\u00a76========================================");
                sender.sendMessage("");
                if (topPlayers.isEmpty()) {
                    sender.sendMessage("\u00a7c\u2717 NO PLAYERS FOUND!");
                    sender.sendMessage("");
                    sender.sendMessage("\u00a77Possible reasons:");
                    sender.sendMessage("  \u00a7c\u2022 No one has played yet");
                    sender.sendMessage("  \u00a7c\u2022 All stats are 0");
                    sender.sendMessage("  \u00a7c\u2022 Database is empty");
                    sender.sendMessage("  \u00a7c\u2022 Invalid stat type: " + finalStatType);
                } else {
                    int rank = 1;
                    for (Map.Entry entry : topPlayers.entrySet()) {
                        String playerName = this.plugin.getDatabase().getPlayerName((UUID)entry.getKey());
                        int value = (Integer)entry.getValue();
                        Object medal = switch (rank) {
                            case 1 -> "\ud83e\udd47";
                            case 2 -> "\ud83e\udd48";
                            case 3 -> "\ud83e\udd49";
                            default -> "\u00a77" + rank + ".";
                        };
                        sender.sendMessage((String)medal + " \u00a7f" + playerName + " \u00a77- \u00a7e" + value);
                        ++rank;
                    }
                }
                sender.sendMessage("\u00a76========================================");
            });
        });
    }

    private void testAddWin(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00a7c\u2717 Only players can use this!");
            return;
        }
        Player player = (Player)sender;
        sender.sendMessage("\u00a7e\u23f3 Adding test win...");
        this.plugin.getLogger().info("========================================");
        this.plugin.getLogger().info("TEST WIN for " + player.getName());
        this.plugin.getLogger().info("UUID: " + String.valueOf(player.getUniqueId()));
        this.plugin.getLogger().info("========================================");
        this.plugin.getDatabase().createPlayerData(player.getUniqueId(), player.getName());
        this.plugin.getDatabase().addWin(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            PlayerManager.PlayerStats stats = this.plugin.getDatabase().getPlayerStats(player.getUniqueId());
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
                if (stats != null) {
                    sender.sendMessage("\u00a7a\u2713 Win added successfully!");
                    sender.sendMessage("\u00a77Total wins: \u00a7f" + stats.wins());
                } else {
                    sender.sendMessage("\u00a7c\u2717 Failed to verify win!");
                }
            });
        }), 40L);
    }

    private void testAddKill(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00a7c\u2717 Only players can use this!");
            return;
        }
        Player player = (Player)sender;
        sender.sendMessage("\u00a7e\u23f3 Adding test kill...");
        this.plugin.getDatabase().createPlayerData(player.getUniqueId(), player.getName());
        this.plugin.getDatabase().addKill(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            PlayerManager.PlayerStats stats = this.plugin.getDatabase().getPlayerStats(player.getUniqueId());
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
                if (stats != null) {
                    sender.sendMessage("\u00a7a\u2713 Kill added successfully!");
                    sender.sendMessage("\u00a77Total kills: \u00a7f" + stats.kills());
                } else {
                    sender.sendMessage("\u00a7c\u2717 Failed to verify kill!");
                }
            });
        }), 40L);
    }

    private void testAddGame(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00a7c\u2717 Only players can use this!");
            return;
        }
        Player player = (Player)sender;
        sender.sendMessage("\u00a7e\u23f3 Adding test game...");
        this.plugin.getDatabase().createPlayerData(player.getUniqueId(), player.getName());
        this.plugin.getDatabase().addGamePlayed(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            PlayerManager.PlayerStats stats = this.plugin.getDatabase().getPlayerStats(player.getUniqueId());
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
                if (stats != null) {
                    sender.sendMessage("\u00a7a\u2713 Game added successfully!");
                    sender.sendMessage("\u00a77Total games: \u00a7f" + stats.gamesPlayed());
                } else {
                    sender.sendMessage("\u00a7c\u2717 Failed to verify game!");
                }
            });
        }), 40L);
    }

    private void createPlayerEntry(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("\u00a7c\u2717 Usage: /dbdebug create <player>");
            return;
        }
        Player target = Bukkit.getPlayer((String)args[1]);
        if (target == null) {
            sender.sendMessage("\u00a7c\u2717 Player not found!");
            return;
        }
        sender.sendMessage("\u00a7e\u23f3 Creating database entry for " + target.getName() + "...");
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            this.plugin.getDatabase().createPlayerData(target.getUniqueId(), target.getName());
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
                sender.sendMessage("\u00a7a\u2713 Database entry created!");
                sender.sendMessage("\u00a77Use: /dbdebug check " + target.getName() + " to verify");
            });
        });
    }

    private void resetPlayerStats(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("\u00a7c\u2717 Usage: /dbdebug reset <player>");
            return;
        }
        Player target = Bukkit.getPlayer((String)args[1]);
        if (target == null) {
            sender.sendMessage("\u00a7c\u2717 Player not found!");
            return;
        }
        sender.sendMessage("\u00a7c\u26a0 This will reset ALL stats for " + target.getName() + "!");
        sender.sendMessage("\u00a77Type '/dbdebug reset " + target.getName() + " confirm' to proceed");
        if (args.length >= 3 && args[2].equalsIgnoreCase("confirm")) {
            Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
                sender.sendMessage("\u00a7c\u2717 Reset function not yet implemented!");
                sender.sendMessage("\u00a77You'll need to manually delete from database");
            });
        }
    }

    private void showDatabaseInfo(CommandSender sender) {
        sender.sendMessage("\u00a76========================================");
        sender.sendMessage("\u00a7e\u00a7l\ud83d\udcbe DATABASE INFO");
        sender.sendMessage("\u00a76========================================");
        sender.sendMessage("");
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            Map<UUID, Integer> topWins = this.plugin.getDatabase().getTopPlayers("wins", 1);
            Map<UUID, Integer> topKills = this.plugin.getDatabase().getTopPlayers("kills", 1);
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
                sender.sendMessage("\u00a77Database Type: \u00a7fMySQL/SQLite");
                sender.sendMessage("\u00a77Connection: \u00a7a\u2713 Active");
                sender.sendMessage("");
                sender.sendMessage("\u00a77Players with wins: \u00a7f" + topWins.size());
                sender.sendMessage("\u00a77Players with kills: \u00a7f" + topKills.size());
                sender.sendMessage("");
                sender.sendMessage("\u00a77Available stat types:");
                sender.sendMessage("  \u00a7f\u2022 wins");
                sender.sendMessage("  \u00a7f\u2022 kills");
                sender.sendMessage("  \u00a7f\u2022 deaths");
                sender.sendMessage("  \u00a7f\u2022 winstreak");
                sender.sendMessage("  \u00a7f\u2022 highest_winstreak");
                sender.sendMessage("  \u00a7f\u2022 games_played");
                sender.sendMessage("\u00a76========================================");
            });
        });
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("\u00a76========================================");
        sender.sendMessage("\u00a7e\u00a7l\ud83d\udcbe DATABASE DEBUG COMMANDS");
        sender.sendMessage("\u00a76========================================");
        sender.sendMessage("");
        sender.sendMessage("\u00a7e/dbdebug check <player>");
        sender.sendMessage("  \u00a77- Check a player's stats in database");
        sender.sendMessage("");
        sender.sendMessage("\u00a7e/dbdebug top [stat] [limit]");
        sender.sendMessage("  \u00a77- Show top players");
        sender.sendMessage("  \u00a77- Example: /dbdebug top wins 10");
        sender.sendMessage("");
        sender.sendMessage("\u00a7e/dbdebug testwin");
        sender.sendMessage("  \u00a77- Add a test win to yourself");
        sender.sendMessage("");
        sender.sendMessage("\u00a7e/dbdebug testkill");
        sender.sendMessage("  \u00a77- Add a test kill to yourself");
        sender.sendMessage("");
        sender.sendMessage("\u00a7e/dbdebug testgame");
        sender.sendMessage("  \u00a77- Add a test game to yourself");
        sender.sendMessage("");
        sender.sendMessage("\u00a7e/dbdebug create <player>");
        sender.sendMessage("  \u00a77- Create database entry for player");
        sender.sendMessage("");
        sender.sendMessage("\u00a7e/dbdebug info");
        sender.sendMessage("  \u00a77- Show database information");
        sender.sendMessage("\u00a76========================================");
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        ArrayList<String> completions = new ArrayList<String>();
        if (args.length == 1) {
            completions.add("check");
            completions.add("top");
            completions.add("testwin");
            completions.add("testkill");
            completions.add("testgame");
            completions.add("create");
            completions.add("reset");
            completions.add("info");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("check") || args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("reset")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    completions.add(p.getName());
                }
            } else if (args[0].equalsIgnoreCase("top")) {
                completions.add("wins");
                completions.add("kills");
                completions.add("deaths");
                completions.add("winstreak");
                completions.add("games_played");
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("top")) {
            completions.add("5");
            completions.add("10");
            completions.add("25");
            completions.add("50");
        }
        return completions;
    }
}

