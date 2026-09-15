/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.plugin.Plugin
 */
package com.fortunepillars.database;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.database.Database;
import com.fortunepillars.player.PlayerManager;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.plugin.Plugin;

public class SQLiteDatabase
implements Database {
    private final FortunePillars plugin;
    private Connection connection;
    private final File databaseFile;

    public SQLiteDatabase(FortunePillars plugin) {
        this.plugin = plugin;
        this.databaseFile = new File(plugin.getDataFolder(), "data.db");
    }

    @Override
    public void initialize() {
        try {
            if (!this.databaseFile.exists()) {
                this.databaseFile.getParentFile().mkdirs();
                this.databaseFile.createNewFile();
            }
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.databaseFile.getAbsolutePath());
            this.createTables();
            this.plugin.getLogger().info("SQLite database initialized successfully.");
        }
        catch (ClassNotFoundException e) {
            this.plugin.getLogger().log(Level.SEVERE, "SQLite JDBC driver not found!", e);
        }
        catch (SQLException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to initialize SQLite database!", e);
        }
        catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to create database file!", e);
        }
    }

    private void createTables() throws SQLException {
        String createPlayersTable = "CREATE TABLE IF NOT EXISTS players (\n    uuid TEXT PRIMARY KEY,\n    player_name TEXT NOT NULL DEFAULT 'Unknown',\n    wins INTEGER DEFAULT 0,\n    kills INTEGER DEFAULT 0,\n    deaths INTEGER DEFAULT 0,\n    winstreak INTEGER DEFAULT 0,\n    highest_winstreak INTEGER DEFAULT 0,\n    games_played INTEGER DEFAULT 0,\n    cage_color TEXT DEFAULT 'WHITE',\n    win_effect TEXT DEFAULT 'FIREWORK',\n    kill_effect TEXT DEFAULT NULL,\n    trail TEXT DEFAULT NULL,\n    death_cry TEXT DEFAULT NULL,\n    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n);\n";
        String createGameModeStatsTable = "CREATE TABLE IF NOT EXISTS player_gamemode_stats (\n    uuid             TEXT NOT NULL,\n    gamemode         TEXT NOT NULL,\n    wins             INTEGER DEFAULT 0,\n    losses           INTEGER DEFAULT 0,\n    games_played     INTEGER DEFAULT 0,\n    winstreak        INTEGER DEFAULT 0,\n    highest_winstreak INTEGER DEFAULT 0,\n    last_updated     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n    PRIMARY KEY (uuid, gamemode)\n);\n";
        try (Statement stmt = this.connection.createStatement();){
            stmt.execute(createPlayersTable);
            stmt.execute(createGameModeStatsTable);
        }
        this.addColumnIfNotExists("kill_effect", "TEXT DEFAULT NULL");
        this.addColumnIfNotExists("trail", "TEXT DEFAULT NULL");
        this.addColumnIfNotExists("death_cry", "TEXT DEFAULT NULL");
    }

    private void addColumnIfNotExists(String columnName, String columnDef) {
        try {
            DatabaseMetaData meta = this.connection.getMetaData();
            ResultSet rs = meta.getColumns(null, null, "players", columnName);
            if (!rs.next()) {
                try (Statement stmt = this.connection.createStatement();){
                    stmt.execute("ALTER TABLE players ADD COLUMN " + columnName + " " + columnDef);
                    this.plugin.getLogger().info("Added column " + columnName + " to players table");
                }
            }
            rs.close();
        }
        catch (SQLException sQLException) {
            // empty catch block
        }
    }

    @Override
    public void close() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
                this.plugin.getLogger().info("SQLite database connection closed.");
            }
        }
        catch (SQLException e) {
            this.plugin.getLogger().warning("Error closing database: " + e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        if (this.connection == null || this.connection.isClosed()) {
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.databaseFile.getAbsolutePath());
        }
        return this.connection;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public PlayerManager.PlayerStats getPlayerStats(UUID uuid) {
        String query = "SELECT * FROM players WHERE uuid = ?";
        try (PreparedStatement stmt = this.getConnection().prepareStatement(query);){
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;
            PlayerManager.PlayerStats playerStats = new PlayerManager.PlayerStats(rs.getInt("wins"), rs.getInt("kills"), rs.getInt("deaths"), rs.getInt("winstreak"), rs.getInt("highest_winstreak"), rs.getInt("games_played"));
            return playerStats;
        }
        catch (SQLException e) {
            this.plugin.getLogger().warning("Error getting player stats: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void createPlayerData(UUID uuid) {
        this.createPlayerData(uuid, "Unknown");
    }

    @Override
    public void createPlayerData(UUID uuid, String playerName) {
        String query = "INSERT OR IGNORE INTO players (uuid, player_name) VALUES (?, ?)";
        try (PreparedStatement stmt = this.getConnection().prepareStatement(query);){
            stmt.setString(1, uuid.toString());
            stmt.setString(2, playerName);
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            this.plugin.getLogger().warning("Error creating player data: " + e.getMessage());
        }
    }

    @Override
    public void updatePlayerName(UUID uuid, String playerName) {
        String query = "UPDATE players SET player_name = ?, last_updated = CURRENT_TIMESTAMP WHERE uuid = ?";
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try (PreparedStatement stmt = this.getConnection().prepareStatement(query);){
                stmt.setString(1, playerName);
                stmt.setString(2, uuid.toString());
                stmt.executeUpdate();
            }
            catch (SQLException e) {
                this.plugin.getLogger().warning("Error updating player name: " + e.getMessage());
            }
        });
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public String getPlayerName(UUID uuid) {
        String query = "SELECT player_name FROM players WHERE uuid = ?";
        try (PreparedStatement stmt = this.getConnection().prepareStatement(query);){
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return "Unknown";
            String string = rs.getString("player_name");
            return string;
        }
        catch (SQLException e) {
            this.plugin.getLogger().warning("Error getting player name: " + e.getMessage());
        }
        return "Unknown";
    }

    @Override
    public Map<UUID, Integer> getTopPlayers(String statKey, int limit) {
        LinkedHashMap<UUID, Integer> results = new LinkedHashMap<UUID, Integer>();
        String validatedKey = this.validateStatKey(statKey);
        if (validatedKey == null) {
            return results;
        }
        String query = "SELECT uuid, " + validatedKey + " FROM players ORDER BY " + validatedKey + " DESC LIMIT ?";
        try (PreparedStatement stmt = this.getConnection().prepareStatement(query);){
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                results.put(UUID.fromString(rs.getString("uuid")), rs.getInt(validatedKey));
            }
        }
        catch (SQLException e) {
            this.plugin.getLogger().warning("Error getting top players: " + e.getMessage());
        }
        return results;
    }

    private String validateStatKey(String statKey) {
        return switch (statKey.toLowerCase()) {
            case "wins" -> "wins";
            case "kills" -> "kills";
            case "deaths" -> "deaths";
            case "winstreak" -> "winstreak";
            case "highest_winstreak" -> "highest_winstreak";
            case "games_played" -> "games_played";
            default -> null;
        };
    }

    @Override
    public PlayerManager.PlayerStats[] getTopWins(int limit) {
        return this.getTopStats("wins", limit);
    }

    @Override
    public PlayerManager.PlayerStats[] getTopKills(int limit) {
        return this.getTopStats("kills", limit);
    }

    @Override
    public PlayerManager.PlayerStats[] getTopWinstreak(int limit) {
        return this.getTopStats("highest_winstreak", limit);
    }

    private PlayerManager.PlayerStats[] getTopStats(String orderBy, int limit) {
        ArrayList<PlayerManager.PlayerStats> stats = new ArrayList<PlayerManager.PlayerStats>();
        String query = "SELECT * FROM players ORDER BY " + orderBy + " DESC LIMIT ?";
        try (PreparedStatement stmt = this.getConnection().prepareStatement(query);){
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                stats.add(new PlayerManager.PlayerStats(rs.getInt("wins"), rs.getInt("kills"), rs.getInt("deaths"), rs.getInt("winstreak"), rs.getInt("highest_winstreak"), rs.getInt("games_played")));
            }
        }
        catch (SQLException e) {
            this.plugin.getLogger().warning("Error getting top stats: " + e.getMessage());
        }
        return stats.toArray(new PlayerManager.PlayerStats[0]);
    }

    @Override
    public void addWin(UUID uuid) {
        this.incrementStatAsync(uuid, "wins", 1);
    }

    @Override
    public void addKill(UUID uuid) {
        this.incrementStatAsync(uuid, "kills", 1);
    }

    @Override
    public void addDeath(UUID uuid) {
        this.incrementStatAsync(uuid, "deaths", 1);
        this.resetWinstreak(uuid);
    }

    @Override
    public void addWinstreak(UUID uuid) {
        String query = "UPDATE players\nSET winstreak = winstreak + 1,\n    highest_winstreak = MAX(highest_winstreak, winstreak + 1),\n    last_updated = CURRENT_TIMESTAMP\nWHERE uuid = ?\n";
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try (PreparedStatement stmt = this.getConnection().prepareStatement(query);){
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
            }
            catch (SQLException e) {
                this.plugin.getLogger().warning("Error adding winstreak: " + e.getMessage());
            }
        });
    }

    @Override
    public void resetWinstreak(UUID uuid) {
        String query = "UPDATE players SET winstreak = 0, last_updated = CURRENT_TIMESTAMP WHERE uuid = ?";
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try (PreparedStatement stmt = this.getConnection().prepareStatement(query);){
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
            }
            catch (SQLException e) {
                this.plugin.getLogger().warning("Error resetting winstreak: " + e.getMessage());
            }
        });
    }

    @Override
    public void addGamePlayed(UUID uuid) {
        this.incrementStatAsync(uuid, "games_played", 1);
    }

    private void incrementStatAsync(UUID uuid, String column, int amount) {
        String query = "UPDATE players SET " + column + " = " + column + " + ?, last_updated = CURRENT_TIMESTAMP WHERE uuid = ?";
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try (PreparedStatement stmt = this.getConnection().prepareStatement(query);){
                stmt.setInt(1, amount);
                stmt.setString(2, uuid.toString());
                stmt.executeUpdate();
            }
            catch (SQLException e) {
                this.plugin.getLogger().warning("Error incrementing stat " + column + ": " + e.getMessage());
            }
        });
    }

    private void ensureGameModeRow(UUID uuid, String gameMode) throws SQLException {
        String sql = "INSERT OR IGNORE INTO player_gamemode_stats (uuid, gamemode) VALUES (?, ?)";
        try (PreparedStatement stmt = this.getConnection().prepareStatement(sql);){
            stmt.setString(1, uuid.toString());
            stmt.setString(2, gameMode.toUpperCase());
            stmt.executeUpdate();
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public PlayerManager.GameModeStats getGameModeStats(UUID uuid, String gameMode) {
        String sql = "SELECT wins, losses, games_played, winstreak, highest_winstreak\nFROM player_gamemode_stats\nWHERE uuid = ? AND gamemode = ?\n";
        try (PreparedStatement stmt = this.getConnection().prepareStatement(sql);){
            stmt.setString(1, uuid.toString());
            stmt.setString(2, gameMode.toUpperCase());
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return PlayerManager.GameModeStats.empty(gameMode.toUpperCase());
            PlayerManager.GameModeStats gameModeStats = new PlayerManager.GameModeStats(gameMode.toUpperCase(), rs.getInt("wins"), rs.getInt("losses"), rs.getInt("games_played"), rs.getInt("winstreak"), rs.getInt("highest_winstreak"));
            return gameModeStats;
        }
        catch (SQLException e) {
            this.plugin.getLogger().warning("Error getting gamemode stats for " + String.valueOf(uuid) + " mode=" + gameMode + ": " + e.getMessage());
        }
        return PlayerManager.GameModeStats.empty(gameMode.toUpperCase());
    }

    @Override
    public void addGameModeWin(UUID uuid, String gameMode) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try {
                this.ensureGameModeRow(uuid, gameMode);
                String sql = "UPDATE player_gamemode_stats\nSET wins = wins + 1, last_updated = CURRENT_TIMESTAMP\nWHERE uuid = ? AND gamemode = ?\n";
                try (PreparedStatement stmt = this.getConnection().prepareStatement(sql);){
                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, gameMode.toUpperCase());
                    stmt.executeUpdate();
                }
            }
            catch (SQLException e) {
                this.plugin.getLogger().warning("Error adding gamemode win: " + e.getMessage());
            }
        });
    }

    @Override
    public void addGameModeLoss(UUID uuid, String gameMode) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try {
                this.ensureGameModeRow(uuid, gameMode);
                String sql = "UPDATE player_gamemode_stats\nSET losses = losses + 1, last_updated = CURRENT_TIMESTAMP\nWHERE uuid = ? AND gamemode = ?\n";
                try (PreparedStatement stmt = this.getConnection().prepareStatement(sql);){
                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, gameMode.toUpperCase());
                    stmt.executeUpdate();
                }
            }
            catch (SQLException e) {
                this.plugin.getLogger().warning("Error adding gamemode loss: " + e.getMessage());
            }
        });
    }

    @Override
    public void addGameModePlayed(UUID uuid, String gameMode) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try {
                this.ensureGameModeRow(uuid, gameMode);
                String sql = "UPDATE player_gamemode_stats\nSET games_played = games_played + 1, last_updated = CURRENT_TIMESTAMP\nWHERE uuid = ? AND gamemode = ?\n";
                try (PreparedStatement stmt = this.getConnection().prepareStatement(sql);){
                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, gameMode.toUpperCase());
                    stmt.executeUpdate();
                }
            }
            catch (SQLException e) {
                this.plugin.getLogger().warning("Error adding gamemode games_played: " + e.getMessage());
            }
        });
    }

    @Override
    public void addGameModeWinstreak(UUID uuid, String gameMode) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try {
                this.ensureGameModeRow(uuid, gameMode);
                String sql = "UPDATE player_gamemode_stats\nSET winstreak = winstreak + 1,\n    highest_winstreak = MAX(highest_winstreak, winstreak + 1),\n    last_updated = CURRENT_TIMESTAMP\nWHERE uuid = ? AND gamemode = ?\n";
                try (PreparedStatement stmt = this.getConnection().prepareStatement(sql);){
                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, gameMode.toUpperCase());
                    stmt.executeUpdate();
                }
            }
            catch (SQLException e) {
                this.plugin.getLogger().warning("Error adding gamemode winstreak: " + e.getMessage());
            }
        });
    }

    @Override
    public void resetGameModeWinstreak(UUID uuid, String gameMode) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try {
                this.ensureGameModeRow(uuid, gameMode);
                String sql = "UPDATE player_gamemode_stats\nSET winstreak = 0, last_updated = CURRENT_TIMESTAMP\nWHERE uuid = ? AND gamemode = ?\n";
                try (PreparedStatement stmt = this.getConnection().prepareStatement(sql);){
                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, gameMode.toUpperCase());
                    stmt.executeUpdate();
                }
            }
            catch (SQLException e) {
                this.plugin.getLogger().warning("Error resetting gamemode winstreak: " + e.getMessage());
            }
        });
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public String getPlayerCageColor(UUID uuid) {
        String query = "SELECT cage_color FROM players WHERE uuid = ?";
        try (PreparedStatement stmt = this.getConnection().prepareStatement(query);){
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return "WHITE";
            String color = rs.getString("cage_color");
            String string = color != null ? color : "WHITE";
            return string;
        }
        catch (SQLException e) {
            this.plugin.getLogger().warning("Error getting cage color: " + e.getMessage());
        }
        return "WHITE";
    }

    @Override
    public void setPlayerCageColor(UUID uuid, String color) {
        this.createPlayerData(uuid);
        String query = "UPDATE players SET cage_color = ?, last_updated = CURRENT_TIMESTAMP WHERE uuid = ?";
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try (PreparedStatement stmt = this.getConnection().prepareStatement(query);){
                stmt.setString(1, color);
                stmt.setString(2, uuid.toString());
                stmt.executeUpdate();
            }
            catch (SQLException e) {
                this.plugin.getLogger().warning("Error setting cage color: " + e.getMessage());
            }
        });
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public String getPlayerWinEffect(UUID uuid) {
        String query = "SELECT win_effect FROM players WHERE uuid = ?";
        try (PreparedStatement stmt = this.getConnection().prepareStatement(query);){
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return "FIREWORK";
            String effect = rs.getString("win_effect");
            String string = effect != null ? effect : "FIREWORK";
            return string;
        }
        catch (SQLException e) {
            this.plugin.getLogger().warning("Error getting win effect: " + e.getMessage());
        }
        return "FIREWORK";
    }

    @Override
    public void setPlayerWinEffect(UUID uuid, String effect) {
        this.createPlayerData(uuid);
        String query = "UPDATE players SET win_effect = ?, last_updated = CURRENT_TIMESTAMP WHERE uuid = ?";
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try (PreparedStatement stmt = this.getConnection().prepareStatement(query);){
                stmt.setString(1, effect);
                stmt.setString(2, uuid.toString());
                stmt.executeUpdate();
            }
            catch (SQLException e) {
                this.plugin.getLogger().warning("Error setting win effect: " + e.getMessage());
            }
        });
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public String getPlayerCosmetic(UUID uuid, String cosmeticType) {
        String column = this.mapCosmeticTypeToColumn(cosmeticType);
        if (column == null) {
            return null;
        }
        String query = "SELECT " + column + " FROM players WHERE uuid = ?";
        try (PreparedStatement stmt = this.getConnection().prepareStatement(query);){
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;
            String string = rs.getString(column);
            return string;
        }
        catch (SQLException e) {
            this.plugin.getLogger().warning("Error getting cosmetic " + cosmeticType + ": " + e.getMessage());
        }
        return null;
    }

    @Override
    public void setPlayerCosmetic(UUID uuid, String cosmeticType, String value) {
        String column = this.mapCosmeticTypeToColumn(cosmeticType);
        if (column == null) {
            return;
        }
        this.createPlayerData(uuid);
        String query = "UPDATE players SET " + column + " = ?, last_updated = CURRENT_TIMESTAMP WHERE uuid = ?";
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try (PreparedStatement stmt = this.getConnection().prepareStatement(query);){
                stmt.setString(1, value);
                stmt.setString(2, uuid.toString());
                stmt.executeUpdate();
            }
            catch (SQLException e) {
                this.plugin.getLogger().warning("Error setting cosmetic " + cosmeticType + ": " + e.getMessage());
            }
        });
    }

    private String mapCosmeticTypeToColumn(String cosmeticType) {
        if (cosmeticType == null) {
            return null;
        }
        return switch (cosmeticType.toLowerCase()) {
            case "cage", "cage_color" -> "cage_color";
            case "win_effect", "win" -> "win_effect";
            case "kill_effect", "kill" -> "kill_effect";
            case "trail" -> "trail";
            case "death_cry", "deathcry" -> "death_cry";
            default -> null;
        };
    }
}

