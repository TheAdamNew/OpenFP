/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 */
package com.fortunepillars.commands;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.game.LootManager;
import com.fortunepillars.game.loot.LootMode;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class LootDebugCommand
implements CommandExecutor,
TabCompleter {
    private final FortunePillars plugin;

    public LootDebugCommand(FortunePillars plugin) {
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
            case "tables": {
                this.debugLootTables(sender);
                break;
            }
            case "forceloot": {
                this.forceTestLoot(sender, args);
                break;
            }
            case "testdrop": {
                this.testSingleDrop(sender, args);
                break;
            }
            case "inspect": {
                this.inspectLootMode(sender, args);
                break;
            }
            case "reload": {
                this.reloadLootTables(sender);
                break;
            }
            default: {
                this.sendHelpMessage(sender);
            }
        }
        return true;
    }

    private void debugLootTables(CommandSender sender) {
        LootManager.LootItem sample;
        sender.sendMessage("\u00a76========================================");
        sender.sendMessage("\u00a7e\u00a7l\ud83d\udce6 LOOT TABLE DEBUG INFO");
        sender.sendMessage("\u00a76========================================");
        sender.sendMessage("");
        sender.sendMessage("\u00a76\u00a7l\ud83d\udce6 MULTI-ITEM TABLES (Stacks):");
        sender.sendMessage("  \u00a77Normal Multi: \u00a7f" + this.plugin.getLootManager().getNormalLootMulti().size() + " items");
        sender.sendMessage("  \u00a77Balanced Multi: \u00a7f" + this.plugin.getLootManager().getBalancedLootMulti().size() + " items");
        sender.sendMessage("  \u00a77OP Multi: \u00a7f" + this.plugin.getLootManager().getOpLootMulti().size() + " items");
        sender.sendMessage("");
        sender.sendMessage("\u00a7b\u00a7l1\ufe0f\u20e3 SINGLE-ITEM TABLES (1x Only):");
        sender.sendMessage("  \u00a77Normal Single: \u00a7f" + this.plugin.getLootManager().getNormalLootSingle().size() + " items");
        sender.sendMessage("  \u00a77Balanced Single: \u00a7f" + this.plugin.getLootManager().getBalancedLootSingle().size() + " items");
        sender.sendMessage("  \u00a77OP Single: \u00a7f" + this.plugin.getLootManager().getOpLootSingle().size() + " items");
        sender.sendMessage("");
        sender.sendMessage("\u00a7e\u00a7l\u2699 LEGACY TABLES:");
        sender.sendMessage("  \u00a77Normal: \u00a7f" + this.plugin.getLootManager().getNormalLoot().size() + " items");
        sender.sendMessage("  \u00a77Balanced: \u00a7f" + this.plugin.getLootManager().getBalancedLoot().size() + " items");
        sender.sendMessage("  \u00a77OP: \u00a7f" + this.plugin.getLootManager().getOpLoot().size() + " items");
        sender.sendMessage("");
        sender.sendMessage("\u00a7a\u00a7l\u2713 SAMPLE ITEMS:");
        if (!this.plugin.getLootManager().getNormalLootMulti().isEmpty()) {
            sample = this.plugin.getLootManager().getNormalLootMulti().get(0);
            sender.sendMessage("  \u00a77Normal Multi Example: \u00a7f" + String.valueOf(sample.material()) + " \u00a77(" + sample.minAmount() + "-" + sample.maxAmount() + ")");
        }
        if (!this.plugin.getLootManager().getNormalLootSingle().isEmpty()) {
            sample = this.plugin.getLootManager().getNormalLootSingle().get(0);
            sender.sendMessage("  \u00a77Normal Single Example: \u00a7f" + String.valueOf(sample.material()) + " \u00a77(Always 1x)");
        }
        sender.sendMessage("\u00a76========================================");
    }

    private void forceTestLoot(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00a7c\u2717 Only players can test loot drops!");
            return;
        }
        Player player = (Player)sender;
        LootMode.LootTier tier = LootMode.LootTier.NORMAL;
        LootMode.DropStyle style = LootMode.DropStyle.MULTI;
        int count = 3;
        if (args.length >= 2) {
            try {
                tier = LootMode.LootTier.valueOf(args[1].toUpperCase());
            }
            catch (IllegalArgumentException e) {
                sender.sendMessage("\u00a7c\u2717 Invalid tier! Use: NORMAL, BALANCED, or OP");
                return;
            }
        }
        if (args.length >= 3) {
            try {
                style = LootMode.DropStyle.valueOf(args[2].toUpperCase());
            }
            catch (IllegalArgumentException e) {
                sender.sendMessage("\u00a7c\u2717 Invalid style! Use: MULTI or SINGLE");
                return;
            }
        }
        if (args.length >= 4) {
            try {
                count = Integer.parseInt(args[3]);
                if (count < 1 || count > 64) {
                    sender.sendMessage("\u00a7c\u2717 Count must be between 1 and 64!");
                    return;
                }
            }
            catch (NumberFormatException e) {
                sender.sendMessage("\u00a7c\u2717 Invalid count!");
                return;
            }
        }
        sender.sendMessage("\u00a76========================================");
        sender.sendMessage("\u00a7e\u00a7l\ud83e\uddea FORCE LOOT TEST");
        sender.sendMessage("\u00a76========================================");
        sender.sendMessage("\u00a77Tier: \u00a7f" + String.valueOf((Object)tier));
        sender.sendMessage("\u00a77Style: \u00a7f" + String.valueOf((Object)style));
        sender.sendMessage("\u00a77Count: \u00a7f" + count);
        sender.sendMessage("");
        this.plugin.getLogger().info("========================================");
        this.plugin.getLogger().info("FORCE LOOT TEST by " + player.getName());
        this.plugin.getLogger().info("Tier: " + String.valueOf((Object)tier) + ", Style: " + String.valueOf((Object)style) + ", Count: " + count);
        this.plugin.getLogger().info("========================================");
        this.plugin.getLootManager().giveLootByTier(player, tier, style, count);
        sender.sendMessage("\u00a7a\u2713 Loot test complete! Check console for details.");
        sender.sendMessage("\u00a76========================================");
    }

    private void testSingleDrop(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00a7c\u2717 Only players can test drops!");
            return;
        }
        Player player = (Player)sender;
        Arena arena = this.plugin.getArenaManager().getPlayerArena(player);
        if (arena == null) {
            sender.sendMessage("\u00a7c\u2717 You must be in an arena to test drops!");
            sender.sendMessage("\u00a77Use: /tof forceloot [tier] [style] [count] to test without an arena");
            return;
        }
        if (!arena.isActive()) {
            sender.sendMessage("\u00a7c\u2717 Arena must be active!");
            return;
        }
        LootMode mode = arena.getLootMode();
        int itemCount = mode.getRandomItemCount();
        sender.sendMessage("\u00a76========================================");
        sender.sendMessage("\u00a7e\u00a7l\ud83c\udfaf ARENA LOOT DROP TEST");
        sender.sendMessage("\u00a76========================================");
        sender.sendMessage("\u00a77Arena: \u00a7f" + arena.getName());
        sender.sendMessage("\u00a77Mode: \u00a7f" + mode.getDisplayName());
        sender.sendMessage("\u00a77Tier: \u00a7f" + String.valueOf((Object)mode.getTier()));
        sender.sendMessage("\u00a77Style: \u00a7f" + String.valueOf((Object)mode.getStyle()));
        sender.sendMessage("\u00a77Items: \u00a7f" + itemCount);
        sender.sendMessage("");
        this.plugin.getLogger().info("========================================");
        this.plugin.getLogger().info("ARENA DROP TEST by " + player.getName());
        this.plugin.getLogger().info("Arena: " + arena.getName());
        this.plugin.getLogger().info("Mode: " + mode.name());
        this.plugin.getLogger().info("========================================");
        this.plugin.getLootManager().giveLootByTier(player, mode.getTier(), mode.getStyle(), itemCount);
        sender.sendMessage("\u00a7a\u2713 Drop test complete! Check console for details.");
        sender.sendMessage("\u00a76========================================");
    }

    private void inspectLootMode(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("\u00a7c\u2717 Usage: /lootdebug inspect <mode>");
            sender.sendMessage("\u00a77Example: /lootdebug inspect MULTI_NORMAL_FAST");
            return;
        }
        try {
            LootMode mode = LootMode.valueOf(args[1].toUpperCase());
            sender.sendMessage("\u00a76========================================");
            sender.sendMessage("\u00a7e\u00a7l\ud83d\udd0d LOOT MODE INSPECTION");
            sender.sendMessage("\u00a76========================================");
            sender.sendMessage("");
            sender.sendMessage("\u00a77Mode: " + mode.getColoredDisplayName());
            sender.sendMessage("\u00a77Description: \u00a7f" + mode.getDescription());
            sender.sendMessage("");
            sender.sendMessage("\u00a7e\u00a7lProperties:");
            sender.sendMessage("  \u00a77Tier: \u00a7f" + String.valueOf((Object)mode.getTier()));
            sender.sendMessage("  \u00a77Style: \u00a7f" + String.valueOf((Object)mode.getStyle()));
            sender.sendMessage("  \u00a77Min Interval: \u00a7f" + mode.getMinIntervalSeconds() + "s");
            sender.sendMessage("  \u00a77Max Interval: \u00a7f" + mode.getMaxIntervalSeconds() + "s");
            sender.sendMessage("  \u00a77Min Items: \u00a7f" + mode.getMinItems());
            sender.sendMessage("  \u00a77Max Items: \u00a7f" + mode.getMaxItems());
            sender.sendMessage("");
            sender.sendMessage("\u00a7e\u00a7lEmojis:");
            sender.sendMessage("  \u00a77Style Emoji: " + mode.getStyleEmoji());
            sender.sendMessage("  \u00a77Tier Color: " + mode.getTierColor() + "Example Text");
            sender.sendMessage("\u00a76========================================");
        }
        catch (IllegalArgumentException e) {
            sender.sendMessage("\u00a7c\u2717 Invalid loot mode!");
            sender.sendMessage("\u00a77Available modes: MULTI_NORMAL_FAST, MULTI_OP_MEDIUM, SINGLE_BALANCED_SLOW, etc.");
        }
    }

    private void reloadLootTables(CommandSender sender) {
        sender.sendMessage("\u00a7e\u23f3 Reloading loot tables...");
        this.plugin.getLogger().info("========================================");
        this.plugin.getLogger().info("RELOADING LOOT TABLES");
        this.plugin.getLogger().info("Triggered by: " + sender.getName());
        this.plugin.getLogger().info("========================================");
        this.plugin.getLootManager().reload();
        sender.sendMessage("\u00a7a\u2713 Loot tables reloaded!");
        sender.sendMessage("\u00a77Use /lootdebug tables to verify");
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("\u00a76========================================");
        sender.sendMessage("\u00a7e\u00a7l\ud83d\udce6 LOOT DEBUG COMMANDS");
        sender.sendMessage("\u00a76========================================");
        sender.sendMessage("");
        sender.sendMessage("\u00a7e/lootdebug tables");
        sender.sendMessage("  \u00a77- Show all loot table sizes and samples");
        sender.sendMessage("");
        sender.sendMessage("\u00a7e/lootdebug forceloot [tier] [style] [count]");
        sender.sendMessage("  \u00a77- Force give loot to yourself");
        sender.sendMessage("  \u00a77- Example: /lootdebug forceloot OP MULTI 5");
        sender.sendMessage("");
        sender.sendMessage("\u00a7e/lootdebug testdrop");
        sender.sendMessage("  \u00a77- Test loot drop using arena's current mode");
        sender.sendMessage("  \u00a77- Must be in an active arena");
        sender.sendMessage("");
        sender.sendMessage("\u00a7e/lootdebug inspect <mode>");
        sender.sendMessage("  \u00a77- Inspect a specific loot mode");
        sender.sendMessage("  \u00a77- Example: /lootdebug inspect MULTI_OP_FAST");
        sender.sendMessage("");
        sender.sendMessage("\u00a7e/lootdebug reload");
        sender.sendMessage("  \u00a77- Reload all loot tables");
        sender.sendMessage("\u00a76========================================");
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        ArrayList<String> completions = new ArrayList<String>();
        if (args.length == 1) {
            completions.add("tables");
            completions.add("forceloot");
            completions.add("testdrop");
            completions.add("inspect");
            completions.add("reload");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("forceloot")) {
                completions.add("NORMAL");
                completions.add("BALANCED");
                completions.add("OP");
            } else if (args[0].equalsIgnoreCase("inspect")) {
                for (LootMode mode : LootMode.values()) {
                    completions.add(mode.name());
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("forceloot")) {
            completions.add("MULTI");
            completions.add("SINGLE");
        } else if (args.length == 4 && args[0].equalsIgnoreCase("forceloot")) {
            completions.add("1");
            completions.add("3");
            completions.add("5");
            completions.add("10");
        }
        return completions;
    }
}

