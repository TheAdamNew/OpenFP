/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.plugin.Plugin
 */
package com.fortunepillars.database;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.database.Database;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.fortunepillars.player.PlayerManager;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
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

public class MySQLDatabase
implements Database {
    private final FortunePillars plugin = FortunePillars.getInstance();
    private HikariDataSource dataSource;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    public MySQLDatabase(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    @Override
    public void initialize() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?useSSL=false&autoReconnect=true&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8");
            config.setUsername(this.username);
            config.setPassword(this.password);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(300000L);
            config.setConnectionTimeout(10000L);
            config.setMaxLifetime(600000L);
            config.setLeakDetectionThreshold(60000L);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
            this.dataSource = new HikariDataSource(config);
            this.createTables();
            this.plugin.getLogger().info("MySQL database initialized with HikariCP connection pool.");
        }
        catch (Exception e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to initialize MySQL database!", e);
            throw new RuntimeException("Failed to initialize MySQL database", e);
        }
    }

    private void createTables() throws SQLException {
        String createPlayersTable = "CREATE TABLE IF NOT EXISTS players (\n    uuid VARCHAR(36) PRIMARY KEY,\n    player_name VARCHAR(16) NOT NULL DEFAULT 'Unknown',\n    wins INT DEFAULT 0,\n    kills INT DEFAULT 0,\n    deaths INT DEFAULT 0,\n    winstreak INT DEFAULT 0,\n    highest_winstreak INT DEFAULT 0,\n    games_played INT DEFAULT 0,\n    cage_color VARCHAR(32) DEFAULT 'WHITE',\n    win_effect VARCHAR(32) DEFAULT 'FIREWORK',\n    kill_effect VARCHAR(32) DEFAULT NULL,\n    trail VARCHAR(32) DEFAULT NULL,\n    death_cry VARCHAR(32) DEFAULT NULL,\n    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n    INDEX idx_wins (wins),\n    INDEX idx_kills (kills),\n    INDEX idx_winstreak (highest_winstreak),\n    INDEX idx_games (games_played)\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;\n";
        String createGameModeStatsTable = "CREATE TABLE IF NOT EXISTS player_gamemode_stats (\n    uuid        VARCHAR(36) NOT NULL,\n    gamemode    VARCHAR(32) NOT NULL,\n    wins        INT DEFAULT 0,\n    losses      INT DEFAULT 0,\n    games_played INT DEFAULT 0,\n    winstreak   INT DEFAULT 0,\n    highest_winstreak INT DEFAULT 0,\n    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n                 ON UPDATE CURRENT_TIMESTAMP,\n    PRIMARY KEY (uuid, gamemode),\n    INDEX idx_gm_uuid    (uuid),\n    INDEX idx_gm_mode    (gamemode),\n    INDEX idx_gm_wins    (wins),\n    INDEX idx_gm_streak  (highest_winstreak)\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;\n";
        try (Connection conn = this.dataSource.getConnection();
             Statement stmt = conn.createStatement();){
            stmt.execute(createPlayersTable);
            stmt.execute(createGameModeStatsTable);
            this.plugin.getLogger().info("Database tables created or already exist.");
        }
        this.addColumnIfNotExists("kill_effect", "VARCHAR(32) DEFAULT NULL");
        this.addColumnIfNotExists("trail", "VARCHAR(32) DEFAULT NULL");
        this.addColumnIfNotExists("death_cry", "VARCHAR(32) DEFAULT NULL");
    }

    private void addColumnIfNotExists(String columnName, String columnDef) {
        try (Connection conn = this.dataSource.getConnection();){
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, this.database, "players", columnName);
            if (!rs.next()) {
                try (Statement stmt = conn.createStatement();){
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
        if (this.dataSource != null && !this.dataSource.isClosed()) {
            this.dataSource.close();
            this.plugin.getLogger().info("MySQL connection pool closed.");
        }
    }

    private Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public PlayerManager.PlayerStats getPlayerStats(UUID uuid) {
        String query = "SELECT * FROM players WHERE uuid = ?";
        try (Connection conn = this.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);){
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                PlayerManager.PlayerStats playerStats = new PlayerManager.PlayerStats(rs.getInt("wins"), rs.getInt("kills"), rs.getInt("deaths"), rs.getInt("winstreak"), rs.getInt("highest_winstreak"), rs.getInt("games_played"));
                return playerStats;
            }
            this.plugin.getLogger().warning("No stats found for UUID: " + String.valueOf(uuid));
            return null;
        }
        catch (SQLException e) {
            this.plugin.getLogger().severe("Error getting player stats for " + String.valueOf(uuid) + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void createPlayerData(UUID uuid) {
        this.createPlayerData(uuid, "Unknown");
    }

    @Override
    public void createPlayerData(UUID uuid, String playerName) {
        String query = "INSERT IGNORE INTO players (uuid, player_name) VALUES (?, ?)";
        try (Connection conn = this.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);){
            stmt.setString(1, uuid.toString());
            stmt.setString(2, playerName);
            int inserted = stmt.executeUpdate();
            if (inserted > 0) {
                this.plugin.getLogger().info("Created database entry for: " + playerName + " (" + String.valueOf(uuid) + ")");
            }
        }
        catch (SQLException e) {
            this.plugin.getLogger().severe("Error creating player data for " + String.valueOf(uuid) + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void updatePlayerName(UUID uuid, String playerName) {
        String query = "UPDATE players SET player_name = ? WHERE uuid = ?";
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try (Connection conn = this.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query);){
                stmt.setString(1, playerName);
                stmt.setString(2, uuid.toString());
                stmt.executeUpdate();
            }
            catch (SQLException e) {
                this.plugin.getLogger().severe("Error updating player name for " + String.valueOf(uuid) + ": " + e.getMessage());
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
        try (Connection conn = this.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);){
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return "Unknown";
            String string = rs.getString("player_name");
            return string;
        }
        catch (SQLException e) {
            this.plugin.getLogger().severe("Error getting player name for " + String.valueOf(uuid) + ": " + e.getMessage());
        }
        return "Unknown";
    }

    @Override
    public Map<UUID, Integer> getTopPlayers(String statKey, int limit) {
        LinkedHashMap<UUID, Integer> results = new LinkedHashMap<UUID, Integer>();
        String validatedKey = this.validateStatKey(statKey);
        if (validatedKey == null) {
            this.plugin.getLogger().warning("Invalid stat key: " + statKey);
            return results;
        }
        String query = "SELECT uuid, " + validatedKey + " FROM players WHERE " + validatedKey + " > 0 ORDER BY " + validatedKey + " DESC LIMIT ?";
        try (Connection conn = this.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);){
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                results.put(UUID.fromString(rs.getString("uuid")), rs.getInt(validatedKey));
            }
        }
        catch (SQLException e) {
            this.plugin.getLogger().severe("Error getting top players for " + statKey + ": " + e.getMessage());
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
        String query = "SELECT * FROM players WHERE " + orderBy + " > 0 ORDER BY " + orderBy + " DESC LIMIT ?";
        try (Connection conn = this.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);){
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                stats.add(new PlayerManager.PlayerStats(rs.getInt("wins"), rs.getInt("kills"), rs.getInt("deaths"), rs.getInt("winstreak"), rs.getInt("highest_winstreak"), rs.getInt("games_played")));
            }
        }
        catch (SQLException e) {
            this.plugin.getLogger().severe("Error getting top stats for " + orderBy + ": " + e.getMessage());
        }
        return stats.toArray(new PlayerManager.PlayerStats[0]);
    }

    @Override
    public void addWin(UUID uuid) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try (Connection conn = this.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("UPDATE players SET wins = wins + 1 WHERE uuid = ?");){
                stmt.setString(1, uuid.toString());
                int updated = stmt.executeUpdate();
                if (updated == 0) {
                    this.createPlayerData(uuid);
                    this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> this.addWin(uuid), 20L);
                }
            }
            catch (SQLException e) {
                this.plugin.getLogger().severe("Failed to add win for " + String.valueOf(uuid) + ": " + e.getMessage());
            }
        });
    }

    @Override
    public void addKill(UUID uuid) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try (Connection conn = this.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("UPDATE players SET kills = kills + 1 WHERE uuid = ?");){
                stmt.setString(1, uuid.toString());
                int updated = stmt.executeUpdate();
                if (updated == 0) {
                    this.createPlayerData(uuid);
                    this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> this.addKill(uuid), 20L);
                }
            }
            catch (SQLException e) {
                this.plugin.getLogger().severe("Failed to add kill for " + String.valueOf(uuid) + ": " + e.getMessage());
            }
        });
    }

    @Override
    public void addDeath(UUID uuid) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try (Connection conn = this.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("UPDATE players SET deaths = deaths + 1 WHERE uuid = ?");){
                stmt.setString(1, uuid.toString());
                int updated = stmt.executeUpdate();
                if (updated == 0) {
                    this.createPlayerData(uuid);
                    this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> this.addDeath(uuid), 20L);
                }
            }
            catch (SQLException e) {
                this.plugin.getLogger().severe("Failed to add death for " + String.valueOf(uuid) + ": " + e.getMessage());
            }
        });
        this.resetWinstreak(uuid);
    }

    @Override
    public void addWinstreak(UUID uuid) {
        String query = "UPDATE players\nSET winstreak = winstreak + 1,\n    highest_winstreak = GREATEST(highest_winstreak, winstreak + 1)\nWHERE uuid = ?\n";
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try (Connection conn = this.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query);){
                stmt.setString(1, uuid.toString());
                int updated = stmt.executeUpdate();
                if (updated == 0) {
                    this.createPlayerData(uuid);
                    this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> this.addWinstreak(uuid), 20L);
                }
            }
            catch (SQLException e) {
                this.plugin.getLogger().severe("Failed to add winstreak for " + String.valueOf(uuid) + ": " + e.getMessage());
            }
        });
    }

    @Override
    public void resetWinstreak(UUID uuid) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try (Connection conn = this.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("UPDATE players SET winstreak = 0 WHERE uuid = ?");){
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
            }
            catch (SQLException e) {
                this.plugin.getLogger().severe("Failed to reset winstreak for " + String.valueOf(uuid) + ": " + e.getMessage());
            }
        });
    }

    @Override
    public void addGamePlayed(UUID uuid) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try (Connection conn = this.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("UPDATE players SET games_played = games_played + 1 WHERE uuid = ?");){
                stmt.setString(1, uuid.toString());
                int updated = stmt.executeUpdate();
                if (updated == 0) {
                    this.createPlayerData(uuid);
                    this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> this.addGamePlayed(uuid), 20L);
                }
            }
            catch (SQLException e) {
                this.plugin.getLogger().severe("Failed to add games_played for " + String.valueOf(uuid) + ": " + e.getMessage());
            }
        });
    }

    private void ensureGameModeRow(Connection conn, UUID uuid, String gameMode) throws SQLException {
        String sql = "INSERT IGNORE INTO player_gamemode_stats (uuid, gamemode) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql);){
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
        try (Connection conn = this.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);){
            stmt.setString(1, uuid.toString());
            stmt.setString(2, gameMode.toUpperCase());
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return PlayerManager.GameModeStats.empty(gameMode.toUpperCase());
            PlayerManager.GameModeStats gameModeStats = new PlayerManager.GameModeStats(gameMode.toUpperCase(), rs.getInt("wins"), rs.getInt("losses"), rs.getInt("games_played"), rs.getInt("winstreak"), rs.getInt("highest_winstreak"));
            return gameModeStats;
        }
        catch (SQLException e) {
            this.plugin.getLogger().severe("Error getting gamemode stats for " + String.valueOf(uuid) + " mode=" + gameMode + ": " + e.getMessage());
        }
        return PlayerManager.GameModeStats.empty(gameMode.toUpperCase());
    }

    @Override
    public void addGameModeWin(UUID uuid, String gameMode) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try (Connection conn = this.getConnection();){
                this.ensureGameModeRow(conn, uuid, gameMode);
                String sql = "UPDATE player_gamemode_stats\nSET wins = wins + 1\nWHERE uuid = ? AND gamemode = ?\n";
                try (PreparedStatement stmt = conn.prepareStatement(sql);){
                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, gameMode.toUpperCase());
                    stmt.executeUpdate();
                }
            }
            catch (SQLException e) {
                this.plugin.getLogger().severe("Failed to add gamemode win for " + String.valueOf(uuid) + " mode=" + gameMode + ": " + e.getMessage());
            }
        });
    }

    @Override
    public void addGameModeLoss(UUID uuid, String gameMode) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try (Connection conn = this.getConnection();){
                this.ensureGameModeRow(conn, uuid, gameMode);
                String sql = "UPDATE player_gamemode_stats\nSET losses = losses + 1\nWHERE uuid = ? AND gamemode = ?\n";
                try (PreparedStatement stmt = conn.prepareStatement(sql);){
                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, gameMode.toUpperCase());
                    stmt.executeUpdate();
                }
            }
            catch (SQLException e) {
                this.plugin.getLogger().severe("Failed to add gamemode loss for " + String.valueOf(uuid) + " mode=" + gameMode + ": " + e.getMessage());
            }
        });
    }

    @Override
    public void addGameModePlayed(UUID uuid, String gameMode) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try (Connection conn = this.getConnection();){
                this.ensureGameModeRow(conn, uuid, gameMode);
                String sql = "UPDATE player_gamemode_stats\nSET games_played = games_played + 1\nWHERE uuid = ? AND gamemode = ?\n";
                try (PreparedStatement stmt = conn.prepareStatement(sql);){
                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, gameMode.toUpperCase());
                    stmt.executeUpdate();
                }
            }
            catch (SQLException e) {
                this.plugin.getLogger().severe("Failed to add gamemode games_played for " + String.valueOf(uuid) + " mode=" + gameMode + ": " + e.getMessage());
            }
        });
    }

    @Override
    public void addGameModeWinstreak(UUID uuid, String gameMode) {
        String sql = "UPDATE player_gamemode_stats\nSET winstreak = winstreak + 1,\n    highest_winstreak = GREATEST(highest_winstreak, winstreak + 1)\nWHERE uuid = ? AND gamemode = ?\n";
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try (Connection conn = this.getConnection();){
                this.ensureGameModeRow(conn, uuid, gameMode);
                try (PreparedStatement stmt = conn.prepareStatement(sql);){
                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, gameMode.toUpperCase());
                    stmt.executeUpdate();
                }
            }
            catch (SQLException e) {
                this.plugin.getLogger().severe("Failed to add gamemode winstreak for " + String.valueOf(uuid) + " mode=" + gameMode + ": " + e.getMessage());
            }
        });
    }

    @Override
    public void resetGameModeWinstreak(UUID uuid, String gameMode) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try (Connection conn = this.getConnection();){
                this.ensureGameModeRow(conn, uuid, gameMode);
                String sql = "UPDATE player_gamemode_stats\nSET winstreak = 0\nWHERE uuid = ? AND gamemode = ?\n";
                try (PreparedStatement stmt = conn.prepareStatement(sql);){
                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, gameMode.toUpperCase());
                    stmt.executeUpdate();
                }
            }
            catch (SQLException e) {
                this.plugin.getLogger().severe("Failed to reset gamemode winstreak for " + String.valueOf(uuid) + " mode=" + gameMode + ": " + e.getMessage());
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
        try (Connection conn = this.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);){
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return "WHITE";
            String color = rs.getString("cage_color");
            String string = color != null ? color : "WHITE";
            return string;
        }
        catch (SQLException e) {
            this.plugin.getLogger().severe("Error getting cage color for " + String.valueOf(uuid) + ": " + e.getMessage());
        }
        return "WHITE";
    }

    @Override
    public void setPlayerCageColor(UUID uuid, String color) {
        this.createPlayerData(uuid);
        String query = "UPDATE players SET cage_color = ? WHERE uuid = ?";
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try (Connection conn = this.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query);){
                stmt.setString(1, color);
                stmt.setString(2, uuid.toString());
                stmt.executeUpdate();
            }
            catch (SQLException e) {
                this.plugin.getLogger().severe("Error setting cage color for " + String.valueOf(uuid) + ": " + e.getMessage());
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
        try (Connection conn = this.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);){
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return "FIREWORK";
            String effect = rs.getString("win_effect");
            String string = effect != null ? effect : "FIREWORK";
            return string;
        }
        catch (SQLException e) {
            this.plugin.getLogger().severe("Error getting win effect for " + String.valueOf(uuid) + ": " + e.getMessage());
        }
        return "FIREWORK";
    }

    @Override
    public void setPlayerWinEffect(UUID uuid, String effect) {
        this.createPlayerData(uuid);
        String query = "UPDATE players SET win_effect = ? WHERE uuid = ?";
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try (Connection conn = this.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query);){
                stmt.setString(1, effect);
                stmt.setString(2, uuid.toString());
                stmt.executeUpdate();
            }
            catch (SQLException e) {
                this.plugin.getLogger().severe("Error setting win effect for " + String.valueOf(uuid) + ": " + e.getMessage());
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
            this.plugin.getLogger().warning("Unknown cosmetic type: " + cosmeticType);
            return null;
        }
        String query = "SELECT " + column + " FROM players WHERE uuid = ?";
        try (Connection conn = this.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);){
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;
            String string = rs.getString(column);
            return string;
        }
        catch (SQLException e) {
            this.plugin.getLogger().severe("Error getting cosmetic " + cosmeticType + " for " + String.valueOf(uuid) + ": " + e.getMessage());
        }
        return null;
    }

    @Override
    public void setPlayerCosmetic(UUID uuid, String cosmeticType, String value) {
        String column = this.mapCosmeticTypeToColumn(cosmeticType);
        if (column == null) {
            this.plugin.getLogger().warning("Unknown cosmetic type: " + cosmeticType);
            return;
        }
        this.createPlayerData(uuid);
        String query = "UPDATE players SET " + column + " = ? WHERE uuid = ?";
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try (Connection conn = this.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query);){
                stmt.setString(1, value);
                stmt.setString(2, uuid.toString());
                stmt.executeUpdate();
            }
            catch (SQLException e) {
                this.plugin.getLogger().severe("Error setting cosmetic " + cosmeticType + " for " + String.valueOf(uuid) + ": " + e.getMessage());
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

