/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  me.clip.placeholderapi.expansion.PlaceholderExpansion
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.entity.Player
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.fortunepillars.hooks;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.api.FortunePillarsAPI;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.player.PlayerManager;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderAPIHook
extends PlaceholderExpansion {
    private final FortunePillars plugin;
    private static final String[] GAMEMODE_KEYS = new String[]{"NORMAL", "BALANCED", "SWAPPER", "SHUFFLE", "LAVA_RISING", "BORDER_SHRINK", "SPEED_UHC", "TNT_RAIN"};
    private static final Map<String, String> GAMEMODE_ALIAS_MAP = PlaceholderAPIHook.buildAlias();

    private static Map<String, String> buildAlias() {
        LinkedHashMap<String, String> m = new LinkedHashMap<String, String>();
        for (String key : GAMEMODE_KEYS) {
            m.put(key.toLowerCase(), key);
        }
        return Collections.unmodifiableMap(m);
    }

    public PlaceholderAPIHook(FortunePillars plugin) {
        this.plugin = plugin;
    }

    public static void register(FortunePillars plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(plugin).register();
            plugin.getLogger().info("PlaceholderAPI hook registered!");
        }
    }

    @NotNull
    public String getIdentifier() {
        return "fortunepillars";
    }

    @NotNull
    public String getAuthor() {
        return String.join((CharSequence)", ", this.plugin.getDescription().getAuthors());
    }

    @NotNull
    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    public boolean persist() {
        return true;
    }

    @Nullable
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        Player player;
        if (offlinePlayer == null) {
            return "";
        }
        if (params.startsWith("stats_")) {
            return this.handleOfflineStats(offlinePlayer, params.substring(6));
        }
        if (offlinePlayer.isOnline() && (player = offlinePlayer.getPlayer()) != null) {
            return this.handleOnlinePlaceholder(player, params);
        }
        String gameModeResult = this.tryGameModePlaceholderOffline(offlinePlayer, params);
        if (gameModeResult != null) {
            return gameModeResult;
        }
        return null;
    }

    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }
        return this.onRequest((OfflinePlayer)player, identifier);
    }

    private String handleOnlinePlaceholder(Player player, String params) {
        switch (params.toLowerCase()) {
            case "wins": {
                return String.valueOf(this.getAPI().getPlayerWins(player.getUniqueId()));
            }
            case "kills": {
                return String.valueOf(this.getAPI().getPlayerKills(player.getUniqueId()));
            }
            case "deaths": {
                return String.valueOf(this.getAPI().getPlayerDeaths(player.getUniqueId()));
            }
            case "kdr": {
                return String.format("%.2f", this.getAPI().getPlayerKDR(player.getUniqueId()));
            }
            case "games_played": {
                PlayerManager.PlayerStats s = this.getAPI().getPlayerStats(player.getUniqueId());
                return s != null ? String.valueOf(s.getGamesPlayed()) : "0";
            }
            case "winstreak": {
                PlayerManager.PlayerStats s = this.getAPI().getPlayerStats(player.getUniqueId());
                return s != null ? String.valueOf(s.getWinstreak()) : "0";
            }
            case "highest_winstreak": 
            case "best_winstreak": {
                PlayerManager.PlayerStats s = this.getAPI().getPlayerStats(player.getUniqueId());
                return s != null ? String.valueOf(s.getHighestWinstreak()) : "0";
            }
            case "winrate": {
                PlayerManager.PlayerStats s = this.getAPI().getPlayerStats(player.getUniqueId());
                return s != null ? String.format("%.1f%%", s.getWinRate()) : "0.0%";
            }
            case "in_arena": 
            case "in_game": {
                return this.getAPI().isPlayerInArena(player) ? "Yes" : "No";
            }
            case "is_alive": {
                return this.getAPI().isPlayerAlive(player.getUniqueId()) ? "Yes" : "No";
            }
            case "is_spectating": {
                return this.getAPI().isPlayerSpectating(player) ? "Yes" : "No";
            }
            case "arena": 
            case "arena_name": {
                Arena a = this.getAPI().getPlayerArena(player);
                return a != null ? a.getName() : "None";
            }
            case "arena_state": {
                Arena a = this.getAPI().getPlayerArena(player);
                return a != null ? a.getState().getDisplayName() : "None";
            }
            case "arena_players": {
                Arena a = this.getAPI().getPlayerArena(player);
                return a != null ? String.valueOf(a.getPlayerCount()) : "0";
            }
            case "arena_max": {
                Arena a = this.getAPI().getPlayerArena(player);
                return a != null ? String.valueOf(a.getMaxPlayers()) : "0";
            }
            case "arena_alive": {
                Arena a = this.getAPI().getPlayerArena(player);
                return a != null ? String.valueOf(a.getAliveCount()) : "0";
            }
            case "arena_gamemode": {
                Arena a = this.getAPI().getPlayerArena(player);
                return a != null ? a.getGameMode().getDisplayName() : "None";
            }
        }
        String gmResult = this.tryGameModePlaceholderOffline((OfflinePlayer)player, params);
        if (gmResult != null) {
            return gmResult;
        }
        return null;
    }

    private String handleOfflineStats(OfflinePlayer player, String stat) {
        String gmResult = this.tryParseGameModeStat(player, stat);
        if (gmResult != null) {
            return gmResult;
        }
        PlayerManager.PlayerStats stats = this.plugin.getDatabase().getPlayerStats(player.getUniqueId());
        if (stats == null) {
            return "0";
        }
        return switch (stat.toLowerCase()) {
            case "wins" -> String.valueOf(stats.getWins());
            case "kills" -> String.valueOf(stats.getKills());
            case "deaths" -> String.valueOf(stats.getDeaths());
            case "kdr" -> String.format("%.2f", stats.getKDR());
            case "winstreak" -> String.valueOf(stats.getWinstreak());
            case "best_winstreak", "highest_winstreak" -> String.valueOf(stats.getHighestWinstreak());
            case "games", "games_played" -> String.valueOf(stats.getGamesPlayed());
            case "winrate" -> String.format("%.1f%%", stats.getWinRate());
            default -> null;
        };
    }

    @Nullable
    private String tryGameModePlaceholderOffline(OfflinePlayer player, String params) {
        String lower = params.toLowerCase();
        for (Map.Entry<String, String> entry : GAMEMODE_ALIAS_MAP.entrySet()) {
            String alias = entry.getKey();
            String dbKey = entry.getValue();
            if (!lower.startsWith(alias + "_")) continue;
            String statPart = lower.substring(alias.length() + 1);
            return this.resolveGameModeStat(player.getUniqueId(), dbKey, statPart);
        }
        return null;
    }

    @Nullable
    private String tryParseGameModeStat(OfflinePlayer player, String stat) {
        String lower = stat.toLowerCase();
        for (Map.Entry<String, String> entry : GAMEMODE_ALIAS_MAP.entrySet()) {
            String alias = entry.getKey();
            String dbKey = entry.getValue();
            if (!lower.startsWith(alias + "_")) continue;
            String statPart = lower.substring(alias.length() + 1);
            return this.resolveGameModeStat(player.getUniqueId(), dbKey, statPart);
        }
        return null;
    }

    @Nullable
    private String resolveGameModeStat(UUID uuid, String gameModeKey, String statKey) {
        PlayerManager.GameModeStats gms = this.plugin.getDatabase().getGameModeStats(uuid, gameModeKey);
        return switch (statKey) {
            case "wins" -> String.valueOf(gms.getWins());
            case "losses" -> String.valueOf(gms.getLosses());
            case "games_played", "games" -> String.valueOf(gms.getGamesPlayed());
            case "winstreak" -> String.valueOf(gms.getWinstreak());
            case "best_winstreak", "highest_winstreak" -> String.valueOf(gms.getHighestWinstreak());
            case "winrate" -> String.format("%.1f%%", gms.getWinRate());
            default -> null;
        };
    }

    private FortunePillarsAPI getAPI() {
        return FortunePillarsAPI.getInstance();
    }
}

