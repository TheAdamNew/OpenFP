/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 */
package com.fortunepillars.config;

import com.fortunepillars.FortunePillars;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Messages {
    private final FortunePillars plugin;
    private FileConfiguration config;
    private File configFile;
    private final Map<String, String> messageCache = new HashMap<String, String>();
    public static final String PREFIX = "prefix";
    public static final String NO_PERMISSION = "no-permission";
    public static final String PLAYER_ONLY = "player-only";
    public static final String INVALID_COMMAND = "invalid-command";
    public static final String ARENA_CREATED = "arena-created";
    public static final String ARENA_DELETED = "arena-deleted";
    public static final String ARENA_NOT_FOUND = "arena-not-found";
    public static final String ARENA_EXISTS = "arena-exists";
    public static final String ARENA_ENABLED = "arena-enabled";
    public static final String ARENA_DISABLED = "arena-disabled";
    public static final String ARENA_NOT_CONFIGURED = "arena-not-configured";
    public static final String POS1_SET = "pos1-set";
    public static final String POS2_SET = "pos2-set";
    public static final String CAGE_SET = "cage-set";
    public static final String SPECTATOR_SPAWN_SET = "spectator-spawn-set";
    public static final String LOBBY_SET = "lobby-set";
    public static final String JOINED_ARENA = "joined-arena";
    public static final String LEFT_ARENA = "left-arena";
    public static final String ARENA_FULL = "arena-full";
    public static final String GAME_STARTING = "game-starting";
    public static final String GAME_STARTED = "game-started";
    public static final String GAME_ENDED = "game-ended";
    public static final String PLAYER_ELIMINATED = "player-eliminated";
    public static final String PLAYER_WON = "player-won";
    public static final String NOT_ENOUGH_PLAYERS = "not-enough-players";
    public static final String ALREADY_IN_GAME = "already-in-game";
    public static final String NOT_IN_GAME = "not-in-game";
    public static final String VOTE_CAST = "vote-cast";
    public static final String VOTE_MODE_SELECTED = "vote-mode-selected";
    public static final String COUNTDOWN = "countdown";
    public static final String GRACE_PERIOD_START = "grace-period-start";
    public static final String GRACE_PERIOD_END = "grace-period-end";
    public static final String SWAPPER_WARNING = "swapper-warning";
    public static final String SWAPPER_SWAP = "swapper-swap";
    public static final String SHUFFLE_WARNING = "shuffle-warning";
    public static final String SHUFFLE_SHUFFLE = "shuffle-shuffle";
    public static final String LOOT_RECEIVED = "loot-received";
    public static final String NOW_SPECTATING = "now-spectating";
    public static final String SCHEMATIC_SAVED = "schematic-saved";
    public static final String ARENA_RESET = "arena-reset";
    public static final String CONFIG_RELOADED = "config-reloaded";
    public static final String STATS_HEADER = "stats-header";
    public static final String STATS_LINE = "stats-line";
    public static final String NO_ARENAS_AVAILABLE = "no-arenas-available";
    public static final String QUICK_JOIN_FOUND = "quick-join-found";
    public static final String MIN_PLAYERS_SET = "min-players-set";
    public static final String MAX_PLAYERS_SET = "max-players-set";
    public static final String GAMEMODE_SET = "gamemode-set";
    public static final String PLAYER_KICKED = "player-kicked";
    public static final String YOU_WERE_KICKED = "you-were-kicked";
    public static final String PLAYER_NOT_IN_ARENA = "player-not-in-arena";
    public static final String TELEPORTED_TO_ARENA = "teleported-to-arena";
    public static final String FORCE_START = "force-start";
    public static final String FORCE_END = "force-end";
    public static final String CAGE_COLOR_CHANGED = "cage-color-changed";
    public static final String CAGE_COLOR_LOCKED = "cage-color-locked";
    public static final String CAGE_COLOR_UNLOCKED = "cage-color-unlocked";
    public static final String NOT_ENOUGH_COINS = "not-enough-coins";
    public static final String GAME_IN_PROGRESS = "game-in-progress";
    public static final String BLACKLIST_DISABLED_WORLD = "blacklist-disabled-world";
    public static final String BLACKLIST_WORLD_ADDED = "blacklist-world-added";
    public static final String BLACKLIST_WORLD_REMOVED = "blacklist-world-removed";
    public static final String BLACKLIST_WORLD_NOT_FOUND = "blacklist-world-not-found";
    public static final String BLACKLIST_EMPTY = "blacklist-empty";
    public static final String BLACKLIST_USAGE = "blacklist-usage";
    public static final String BLACKLIST_USAGE_ADD = "blacklist-usage-add";
    public static final String BLACKLIST_USAGE_REMOVE = "blacklist-usage-remove";

    public Messages(FortunePillars plugin) {
        this.plugin = plugin;
        this.loadMessages();
    }

    private void loadMessages() {
        this.configFile = new File(this.plugin.getDataFolder(), "messages.yml");
        if (!this.configFile.exists()) {
            this.plugin.saveResource("messages.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration((File)this.configFile);
        this.cacheMessages();
    }

    private void cacheMessages() {
        this.messageCache.clear();
        this.messageCache.put(PREFIX, "<gradient:#FFD700:#FFA500><bold>FortunePillars</bold></gradient> <dark_gray>\u00bb</dark_gray> ");
        this.messageCache.put(NO_PERMISSION, "<red>You don't have permission to do that!");
        this.messageCache.put(PLAYER_ONLY, "<red>This command can only be used by players!");
        this.messageCache.put(INVALID_COMMAND, "<red>Unknown command: <yellow>{command}</yellow>. Use /tof help for commands.");
        this.messageCache.put(ARENA_CREATED, "<green>Arena <yellow>{arena}</yellow> has been created!");
        this.messageCache.put(ARENA_DELETED, "<green>Arena <yellow>{arena}</yellow> has been deleted!");
        this.messageCache.put(ARENA_NOT_FOUND, "<red>Arena <yellow>{arena}</yellow> was not found!");
        this.messageCache.put(ARENA_EXISTS, "<red>Arena <yellow>{arena}</yellow> already exists!");
        this.messageCache.put(ARENA_ENABLED, "<green>Arena <yellow>{arena}</yellow> has been enabled!");
        this.messageCache.put(ARENA_DISABLED, "<yellow>Arena <yellow>{arena}</yellow> has been disabled!");
        this.messageCache.put(ARENA_NOT_CONFIGURED, "<red>Arena <yellow>{arena}</yellow> is not fully configured!");
        this.messageCache.put(POS1_SET, "<green>Position 1 set for arena <yellow>{arena}</yellow>!");
        this.messageCache.put(POS2_SET, "<green>Position 2 set for arena <yellow>{arena}</yellow>!");
        this.messageCache.put(CAGE_SET, "<green>Cage {number} set for arena <yellow>{arena}</yellow>!");
        this.messageCache.put(SPECTATOR_SPAWN_SET, "<green>Spectator spawn set for arena <yellow>{arena}</yellow>!");
        this.messageCache.put(LOBBY_SET, "<green>Main lobby location has been set!");
        this.messageCache.put(JOINED_ARENA, "<green>You joined arena <yellow>{arena}</yellow>! <gray>({current}/{max})");
        this.messageCache.put(LEFT_ARENA, "<red>You left the arena.");
        this.messageCache.put(ARENA_FULL, "<red>This arena is full!");
        this.messageCache.put(GAME_STARTING, "<yellow>Game starting in <gold>{seconds}</gold> seconds!");
        this.messageCache.put(GAME_STARTED, "<green><bold>GAME STARTED!</bold></green> <gray>Good luck!");
        this.messageCache.put(GAME_ENDED, "<gold><bold>GAME OVER!</bold></gold>");
        this.messageCache.put(PLAYER_ELIMINATED, "<red>{player} has been eliminated! <gray>({remaining} remaining)");
        this.messageCache.put(PLAYER_WON, "<gold><bold>\u2605</bold></gold> <yellow>{player}</yellow> <gold>won the game!</gold> <gold><bold>\u2605</bold></gold>");
        this.messageCache.put(NOT_ENOUGH_PLAYERS, "<red>Not enough players to start! Need at least {min} players.");
        this.messageCache.put(ALREADY_IN_GAME, "<red>You're already in a game!");
        this.messageCache.put(NOT_IN_GAME, "<red>You're not in a game!");
        this.messageCache.put(VOTE_CAST, "<green>You voted for <yellow>{mode}</yellow>!");
        this.messageCache.put(VOTE_MODE_SELECTED, "<gold>Game mode selected: <yellow>{mode}</yellow>!");
        this.messageCache.put(COUNTDOWN, "<yellow>{seconds}...");
        this.messageCache.put(GRACE_PERIOD_START, "<aqua>Grace period active! <gray>No PvP for {seconds} seconds.");
        this.messageCache.put(GRACE_PERIOD_END, "<red><bold>PVP ENABLED!</bold></red> <gray>Fight!");
        this.messageCache.put(SWAPPER_WARNING, "<yellow>Positions swapping in <red>5</red> seconds!");
        this.messageCache.put(SWAPPER_SWAP, "<light_purple><bold>SWAP!</bold></light_purple> <gray>Positions have been swapped!");
        this.messageCache.put(SHUFFLE_WARNING, "<yellow>Inventories shuffling in <red>5</red> seconds!");
        this.messageCache.put(SHUFFLE_SHUFFLE, "<light_purple><bold>SHUFFLE!</bold></light_purple> <gray>Inventories have been randomized!");
        this.messageCache.put(LOOT_RECEIVED, "<green>+{count} item(s) received!");
        this.messageCache.put(NOW_SPECTATING, "<gray>You are now spectating. <italic>Use /tof leave to return to lobby.</italic>");
        this.messageCache.put(SCHEMATIC_SAVED, "<green>Arena schematic saved!");
        this.messageCache.put(ARENA_RESET, "<green>Arena has been reset!");
        this.messageCache.put(CONFIG_RELOADED, "<green>Configuration reloaded!");
        this.messageCache.put(STATS_HEADER, "<gold>\u2550\u2550\u2550 <yellow>Your Stats</yellow> \u2550\u2550\u2550</gold>");
        this.messageCache.put(STATS_LINE, "<gray>{stat}: <white>{value}</white>");
        this.messageCache.put(NO_ARENAS_AVAILABLE, "<red>No arenas are currently available!");
        this.messageCache.put(QUICK_JOIN_FOUND, "<green>Found arena: <yellow>{arena}</yellow>");
        this.messageCache.put(MIN_PLAYERS_SET, "<green>Minimum players for <yellow>{arena}</yellow> set to <gold>{value}</gold>!");
        this.messageCache.put(MAX_PLAYERS_SET, "<green>Maximum players for <yellow>{arena}</yellow> set to <gold>{value}</gold>!");
        this.messageCache.put(GAMEMODE_SET, "<green>Default game mode for <yellow>{arena}</yellow> set to <gold>{mode}</gold>!");
        this.messageCache.put(PLAYER_KICKED, "<green>Player <yellow>{player}</yellow> has been kicked from the arena!");
        this.messageCache.put(YOU_WERE_KICKED, "<red>You have been kicked from the arena by an admin!");
        this.messageCache.put(PLAYER_NOT_IN_ARENA, "<red>Player <yellow>{player}</yellow> is not in any arena!");
        this.messageCache.put(TELEPORTED_TO_ARENA, "<green>Teleported to arena <yellow>{arena}</yellow>!");
        this.messageCache.put(FORCE_START, "<yellow>Game force started by admin!");
        this.messageCache.put(FORCE_END, "<yellow>Game force ended by admin!");
        this.messageCache.put(CAGE_COLOR_CHANGED, "<green>Your cage color has been changed to <yellow>{color}</yellow>!");
        this.messageCache.put(CAGE_COLOR_LOCKED, "<red>This cage color is locked! Unlock it first.");
        this.messageCache.put(CAGE_COLOR_UNLOCKED, "<green>You unlocked the <yellow>{color}</yellow> cage color!");
        this.messageCache.put(NOT_ENOUGH_COINS, "<red>You don't have enough coins! Need <yellow>{required}</yellow>, have <yellow>{balance}</yellow>.");
        this.messageCache.put(GAME_IN_PROGRESS, "<red>This game is already in progress!");
        this.messageCache.put(BLACKLIST_DISABLED_WORLD, "<red>\u26a0 FortunePillars is disabled in this world!");
        this.messageCache.put(BLACKLIST_WORLD_ADDED, "<green>World <yellow>{world}</yellow> added to blacklist!");
        this.messageCache.put(BLACKLIST_WORLD_REMOVED, "<green>World <yellow>{world}</yellow> removed from blacklist!");
        this.messageCache.put(BLACKLIST_WORLD_NOT_FOUND, "<red>World <yellow>{world}</yellow> not found!");
        this.messageCache.put(BLACKLIST_EMPTY, "<yellow>No worlds are blacklisted.");
        this.messageCache.put(BLACKLIST_USAGE, "<red>Usage: /tof blacklistworld <add|remove|list> [world]");
        this.messageCache.put(BLACKLIST_USAGE_ADD, "<red>Usage: /tof blacklistworld add <world>");
        this.messageCache.put(BLACKLIST_USAGE_REMOVE, "<red>Usage: /tof blacklistworld remove <world>");
        for (String key : this.config.getKeys(true)) {
            String value;
            if (!this.config.isString(key) || (value = this.config.getString(key)) == null) continue;
            this.messageCache.put(key, value);
        }
        this.plugin.getLogger().info("Loaded " + this.messageCache.size() + " messages.");
    }

    public void reload() {
        this.loadMessages();
    }

    public String getPrefix() {
        return this.messageCache.getOrDefault(PREFIX, "<gradient:#FFD700:#FFA500><bold>FortunePillars</bold></gradient> <dark_gray>\u00bb</dark_gray> ");
    }

    public String getRaw(String key) {
        return this.messageCache.getOrDefault(key, "<red>Missing message: " + key);
    }

    public String getRaw(String key, String ... placeholders) {
        String message = this.getRaw(key);
        return this.replacePlaceholders(message, placeholders);
    }

    public Component get(String key) {
        return FortunePillars.parse(this.getRaw(key));
    }

    public Component get(String key, String ... placeholders) {
        return FortunePillars.parse(this.getRaw(key, placeholders));
    }

    public Component getWithPrefix(String key) {
        return FortunePillars.parse(this.getPrefix() + this.getRaw(key));
    }

    public Component getWithPrefix(String key, String ... placeholders) {
        return FortunePillars.parse(this.getPrefix() + this.getRaw(key, placeholders));
    }

    private String replacePlaceholders(String message, String ... placeholders) {
        if (placeholders.length % 2 != 0) {
            this.plugin.getLogger().warning("Placeholders must be in key-value pairs!");
            return message;
        }
        for (int i = 0; i < placeholders.length; i += 2) {
            message = message.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
        }
        return message;
    }

    public void save() {
        try {
            this.config.save(this.configFile);
        }
        catch (IOException e) {
            this.plugin.getLogger().warning("Could not save messages.yml: " + e.getMessage());
        }
    }
}

