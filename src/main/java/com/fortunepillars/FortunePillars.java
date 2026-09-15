/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.minimessage.MiniMessage
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.PluginCommand
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.event.Listener
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.PluginManager
 *  org.bukkit.plugin.java.JavaPlugin
 */
package com.fortunepillars;

import com.fortunepillars.api.FortunePillarsAPI;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.arena.ArenaManager;
import com.fortunepillars.arena.CageManager;
import com.fortunepillars.commands.DatabaseDebugCommand;
import com.fortunepillars.commands.LootDebugCommand;
import com.fortunepillars.commands.TOFCommand;
import com.fortunepillars.config.ConfigManager;
import com.fortunepillars.config.Messages;
import com.fortunepillars.cosmetics.CosmeticsManager;
import com.fortunepillars.database.Database;
import com.fortunepillars.database.MySQLDatabase;
import com.fortunepillars.database.SQLiteDatabase;
import com.fortunepillars.game.GameManager;
import com.fortunepillars.game.GameModeManager;
import com.fortunepillars.game.LootManager;
import com.fortunepillars.gui.GUIManager;
import com.fortunepillars.hooks.PlaceholderAPIHook;
import com.fortunepillars.leaderboard.LeaderboardManager;
import com.fortunepillars.listeners.BlockListener;
import com.fortunepillars.listeners.ChatListener;
import com.fortunepillars.listeners.CommandListener;
import com.fortunepillars.listeners.DatabaseListener;
import com.fortunepillars.listeners.EntityListener;
import com.fortunepillars.listeners.InventoryListener;
import com.fortunepillars.listeners.LootEditorListener;
import com.fortunepillars.listeners.PlayerConnectionListener;
import com.fortunepillars.listeners.PlayerDamageListener;
import com.fortunepillars.listeners.PlayerDeathListener;
import com.fortunepillars.listeners.PlayerInteractListener;
import com.fortunepillars.listeners.PlayerMoveListener;
import com.fortunepillars.listeners.WorldLoadListener;
import com.fortunepillars.player.PlayerManager;
import com.fortunepillars.player.SpectatorManager;
import com.fortunepillars.schematic.SchematicManager;
import com.fortunepillars.scoreboard.ScoreboardManager;
import com.fortunepillars.signs.SignManager;
import com.fortunepillars.voting.LootModeVoteManager;
import com.fortunepillars.voting.VoteManager;
import java.io.File;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class FortunePillars
extends JavaPlugin {
    private static FortunePillars instance;
    private static final MiniMessage MINI_MESSAGE;
    private ConfigManager configManager;
    private Messages messages;
    private Database database;
    private ArenaManager arenaManager;
    private GameManager gameManager;
    private GameModeManager gameModeManager;
    private PlayerManager playerManager;
    private SpectatorManager spectatorManager;
    private LootManager lootManager;
    private VoteManager voteManager;
    private CosmeticsManager cosmeticsManager;
    private SchematicManager schematicManager;
    private SignManager signManager;
    private LeaderboardManager leaderboardManager;
    private GUIManager guiManager;
    private CageManager cageManager;
    private ScoreboardManager scoreboardManager;
    private LootModeVoteManager lootModeVoteManager;

    public void onEnable() {
        instance = this;
        this.displayBanner();
        this.createDataFolders();
        this.getLogger().info("Loading configuration...");
        this.configManager = new ConfigManager(this);
        this.messages = new Messages(this);
        this.getLogger().info("Connecting to database...");
        if (!this.initializeDatabase()) {
            this.getLogger().severe("Failed to initialize database! Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin((Plugin)this);
            return;
        }
        this.getLogger().info("Initializing managers...");
        this.initializeManagers();
        this.getLogger().info("Registering commands...");
        this.registerCommands();
        this.getLogger().info("Registering listeners...");
        this.registerListeners();
        this.getLogger().info("Hooking external plugins...");
        this.hookExternalPlugins();
        FortunePillarsAPI.init(this);
        Bukkit.getScheduler().runTaskLater((Plugin)this, () -> {
            for (Arena arena : this.arenaManager.getAllArenas()) {
                if (!arena.isSetupComplete() || this.schematicManager.hasSchematic(arena.getName())) continue;
                this.schematicManager.saveArena(arena, success -> {
                    if (!success.booleanValue()) {
                        this.getLogger().severe("Failed to auto-save schematic for " + arena.getName());
                    }
                });
            }
        }, 100L);
        this.leaderboardManager.updateLeaderboards();
        this.getLogger().info("========================================");
        this.getLogger().info("FortunePillars v" + this.getDescription().getVersion() + " enabled!");
        this.getLogger().info("Compatible with Paper 1.21.1 - 1.21.8");
        this.getLogger().info("Arenas loaded: " + this.arenaManager.getAllArenas().size());
        this.getLogger().info("Database: " + (this.database instanceof MySQLDatabase ? "MySQL" : "SQLite"));
        this.getLogger().info("========================================");
    }

    public void onDisable() {
        if (this.arenaManager != null) {
            this.arenaManager.saveAllArenas();
            this.getLogger().info("Arenas saved.");
        }
        if (this.lootModeVoteManager != null) {
            this.lootModeVoteManager.shutdown();
        }
        if (this.voteManager != null && this.arenaManager != null) {
            for (Arena arena : this.arenaManager.getAllArenas()) {
                this.voteManager.clearVotes(arena);
            }
        }
        if (this.gameManager != null) {
            this.gameManager.endAllGames();
        }
        if (this.scoreboardManager != null) {
            this.scoreboardManager.shutdown();
        }
        if (this.signManager != null) {
            this.signManager.shutdown();
        }
        if (this.leaderboardManager != null) {
            this.leaderboardManager.shutdown();
        }
        if (this.database != null) {
            this.database.close();
        }
        this.getLogger().info("FortunePillars has been disabled!");
    }

    private void displayBanner() {
        this.getLogger().info("");
        this.getLogger().info("  _____ ___  ____ _____ _   _ _   _ _____ ");
        this.getLogger().info(" |  ___/ _ \\|  _ \\_   _| | | | \\ | | ____|");
        this.getLogger().info(" | |_ | | | | |_) || | | | | |  \\| |  _|  ");
        this.getLogger().info(" |  _|| |_| |  _ < | | | |_| | |\\  | |___ ");
        this.getLogger().info(" |_|   \\___/|_| \\_\\|_|  \\___/|_| \\_|_____|");
        this.getLogger().info("  ____  ___ _     _        _    ____  ____  ");
        this.getLogger().info(" |  _ \\|_ _| |   | |      / \\  |  _ \\/ ___| ");
        this.getLogger().info(" | |_) || || |   | |     / _ \\ | |_) \\___ \\ ");
        this.getLogger().info(" |  __/ | || |___| |___ / ___ \\|  _ < ___) |");
        this.getLogger().info(" |_|   |___|_____|_____/_/   \\_\\_| \\_\\____/ ");
        this.getLogger().info("");
        this.getLogger().info("         Advanced Mini-Game Plugin          ");
        this.getLogger().info("              Version " + this.getDescription().getVersion() + "                 ");
        this.getLogger().info("");
    }

    private void createDataFolders() {
        File schematicsFolder;
        File arenasFolder;
        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdirs();
        }
        if (!(arenasFolder = new File(this.getDataFolder(), "arenas")).exists()) {
            arenasFolder.mkdirs();
        }
        if (!(schematicsFolder = new File(this.getDataFolder(), "schematics")).exists()) {
            schematicsFolder.mkdirs();
        }
    }

    private boolean initializeDatabase() {
        String dbType = this.configManager.getConfig().getString("database.type", "sqlite");
        try {
            if (dbType.equalsIgnoreCase("mysql")) {
                String host = this.configManager.getConfig().getString("database.mysql.host");
                int port = this.configManager.getConfig().getInt("database.mysql.port");
                String dbName = this.configManager.getConfig().getString("database.mysql.database");
                String username = this.configManager.getConfig().getString("database.mysql.username");
                String password = this.configManager.getConfig().getString("database.mysql.password");
                this.database = new MySQLDatabase(host, port, dbName, username, password);
            } else {
                this.database = new SQLiteDatabase(this);
            }
            this.database.initialize();
            return true;
        }
        catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "Failed to initialize database!", e);
            return false;
        }
    }

    private void initializeManagers() {
        this.schematicManager = new SchematicManager(this);
        this.schematicManager.preloadSchematics();
        this.arenaManager = new ArenaManager(this);
        this.playerManager = new PlayerManager(this);
        this.spectatorManager = new SpectatorManager(this);
        this.lootManager = new LootManager(this);
        this.voteManager = new VoteManager(this);
        this.lootModeVoteManager = new LootModeVoteManager(this);
        this.cosmeticsManager = new CosmeticsManager(this);
        this.cageManager = new CageManager(this);
        this.gameModeManager = new GameModeManager(this);
        this.gameManager = new GameManager(this);
        this.leaderboardManager = new LeaderboardManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.guiManager = new GUIManager(this);
        this.signManager = new SignManager(this);
        this.arenaManager.loadAllArenas();
    }

    private void registerCommands() {
        TOFCommand tofCommand = new TOFCommand(this);
        PluginCommand tofCmd = this.getCommand("tof");
        if (tofCmd != null) {
            tofCmd.setExecutor((CommandExecutor)tofCommand);
            tofCmd.setTabCompleter((TabCompleter)tofCommand);
        } else {
            this.getLogger().severe("Failed to register /tof command!");
        }
        LootDebugCommand lootDebugCommand = new LootDebugCommand(this);
        PluginCommand lootDebugCmd = this.getCommand("lootdebug");
        if (lootDebugCmd != null) {
            lootDebugCmd.setExecutor((CommandExecutor)lootDebugCommand);
            lootDebugCmd.setTabCompleter((TabCompleter)lootDebugCommand);
        }
        DatabaseDebugCommand dbDebugCommand = new DatabaseDebugCommand(this);
        PluginCommand dbDebugCmd = this.getCommand("dbdebug");
        if (dbDebugCmd != null) {
            dbDebugCmd.setExecutor((CommandExecutor)dbDebugCommand);
            dbDebugCmd.setTabCompleter((TabCompleter)dbDebugCommand);
        }
    }

    private void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents((Listener)new PlayerConnectionListener(this), (Plugin)this);
        pm.registerEvents((Listener)new PlayerDeathListener(this), (Plugin)this);
        pm.registerEvents((Listener)new PlayerDamageListener(this), (Plugin)this);
        pm.registerEvents((Listener)new PlayerInteractListener(this), (Plugin)this);
        pm.registerEvents((Listener)new InventoryListener(this), (Plugin)this);
        pm.registerEvents((Listener)new PlayerMoveListener(this), (Plugin)this);
        pm.registerEvents((Listener)new BlockListener(this), (Plugin)this);
        pm.registerEvents((Listener)new EntityListener(this), (Plugin)this);
        pm.registerEvents((Listener)new ChatListener(this), (Plugin)this);
        pm.registerEvents((Listener)new DatabaseListener(this), (Plugin)this);
        pm.registerEvents((Listener)new LootEditorListener(this), (Plugin)this);
        pm.registerEvents((Listener)new WorldLoadListener(this), (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)new CommandListener(this), (Plugin)this);
        pm.registerEvents((Listener)this.signManager, (Plugin)this);
    }

    private void hookExternalPlugins() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            PlaceholderAPIHook.register(this);
            this.getLogger().info("Hooked into PlaceholderAPI");
        }
    }

    public static Component parse(String message) {
        return MINI_MESSAGE.deserialize(message);
    }

    public static Component parseWithPrefix(String message) {
        String prefix = FortunePillars.instance.messages.getPrefix();
        return MINI_MESSAGE.deserialize(prefix + message);
    }

    public static MiniMessage getMiniMessage() {
        return MINI_MESSAGE;
    }

    public static FortunePillars getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public Messages getMessages() {
        return this.messages;
    }

    public Database getDatabase() {
        return this.database;
    }

    public ArenaManager getArenaManager() {
        return this.arenaManager;
    }

    public GameManager getGameManager() {
        return this.gameManager;
    }

    public GameModeManager getGameModeManager() {
        return this.gameModeManager;
    }

    public PlayerManager getPlayerManager() {
        return this.playerManager;
    }

    public SpectatorManager getSpectatorManager() {
        return this.spectatorManager;
    }

    public LootManager getLootManager() {
        return this.lootManager;
    }

    public VoteManager getVoteManager() {
        return this.voteManager;
    }

    public CosmeticsManager getCosmeticsManager() {
        return this.cosmeticsManager;
    }

    public SchematicManager getSchematicManager() {
        return this.schematicManager;
    }

    public SignManager getSignManager() {
        return this.signManager;
    }

    public LeaderboardManager getLeaderboardManager() {
        return this.leaderboardManager;
    }

    public GUIManager getGuiManager() {
        return this.guiManager;
    }

    public CageManager getCageManager() {
        return this.cageManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return this.scoreboardManager;
    }

    public LootModeVoteManager getLootModeVoteManager() {
        return this.lootModeVoteManager;
    }

    static {
        MINI_MESSAGE = MiniMessage.miniMessage();
    }
}

