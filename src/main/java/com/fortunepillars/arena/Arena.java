/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.bossbar.BossBar
 *  net.kyori.adventure.bossbar.BossBar$Color
 *  net.kyori.adventure.bossbar.BossBar$Overlay
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.title.Title
 *  net.kyori.adventure.title.Title$Times
 *  org.bukkit.Bukkit
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.World
 *  org.bukkit.block.Block
 *  org.bukkit.block.data.BlockData
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package com.fortunepillars.arena;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.CageLocation;
import com.fortunepillars.arena.GameState;
import com.fortunepillars.game.GameModeType;
import com.fortunepillars.game.loot.LootDropTask;
import com.fortunepillars.game.loot.LootMode;
import com.fortunepillars.player.PlayerData;
import com.fortunepillars.schematic.ArenaSchematic;
import com.fortunepillars.utils.SoundUtil;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class Arena {
    private final FortunePillars plugin;
    private final String name;
    private final File arenaFile;
    private Location pos1;
    private Location pos2;
    private final Map<Integer, CageLocation> cageLocations;
    private Location spectatorSpawn;
    private GameState state;
    private GameModeType gameMode;
    private int countdown;
    private boolean gracePeriodActive;
    private boolean schematicSaved = false;
    private boolean gameActuallyStarted = false;
    private boolean manuallyEnabled = true;
    private String pendingPos1World;
    private int pendingPos1X;
    private int pendingPos1Y;
    private int pendingPos1Z;
    private String pendingPos2World;
    private int pendingPos2X;
    private int pendingPos2Y;
    private int pendingPos2Z;
    private String pendingSpectatorWorld;
    private double pendingSpectatorX;
    private double pendingSpectatorY;
    private double pendingSpectatorZ;
    private float pendingSpectatorYaw;
    private float pendingSpectatorPitch;
    private final Map<Integer, Map<String, Object>> pendingCages = new HashMap<Integer, Map<String, Object>>();
    private BukkitTask pendingRetryTask;
    private final Map<UUID, PlayerData> players;
    private final Set<UUID> alivePlayers;
    private final Set<UUID> spectators;
    private BukkitTask countdownTask;
    private BukkitTask gameTask;
    private BukkitTask gameModeTask;
    private LootDropTask lootDropTask;
    private LootMode lootMode = LootMode.MULTI_NORMAL_MEDIUM;
    private final Map<UUID, GameModeType> votes;
    private int maxPlayers;
    private int minPlayers;
    private GameModeType defaultGameMode;
    private BossBar countdownBossBar = null;
    private final Set<UUID> countdownBossBarViewers = new HashSet<UUID>();

    public Arena(FortunePillars plugin, String name) {
        this.plugin = plugin;
        this.name = name;
        this.arenaFile = new File(String.valueOf(plugin.getDataFolder()) + "/arenas", name + ".yml");
        this.cageLocations = new ConcurrentHashMap<Integer, CageLocation>();
        this.players = new ConcurrentHashMap<UUID, PlayerData>();
        this.alivePlayers = ConcurrentHashMap.newKeySet();
        this.spectators = ConcurrentHashMap.newKeySet();
        this.votes = new ConcurrentHashMap<UUID, GameModeType>();
        this.state = GameState.DISABLED;
        this.gameMode = GameModeType.NORMAL;
        this.defaultGameMode = GameModeType.NORMAL;
        this.gracePeriodActive = false;
        this.maxPlayers = plugin.getConfigManager().getMaxPlayers();
        this.minPlayers = plugin.getConfigManager().getMinPlayers();
    }

    public boolean addPlayer(Player player) {
        Arena currentArena = this.plugin.getArenaManager().getPlayerArena(player);
        if (currentArena != null) {
            this.plugin.getLogger().warning(player.getName() + " is already in arena " + currentArena.getName() + ", removing first...");
            currentArena.removePlayer(player, false);
        }
        if (!this.state.isJoinable()) {
            player.sendMessage(FortunePillars.parseWithPrefix("<red>This game is already in progress!"));
            SoundUtil.playError(player);
            return false;
        }
        if (this.players.size() >= this.maxPlayers) {
            player.sendMessage(this.plugin.getMessages().getWithPrefix("arena-full"));
            SoundUtil.playError(player);
            return false;
        }
        if (this.players.containsKey(player.getUniqueId())) {
            this.plugin.getLogger().warning(player.getName() + " already in this arena!");
            return false;
        }
        int cageNumber = this.findAvailableCage();
        if (cageNumber == -1) {
            player.sendMessage(this.plugin.getMessages().getWithPrefix("arena-full"));
            SoundUtil.playError(player);
            return false;
        }
        PlayerData playerData = new PlayerData(player.getUniqueId(), this);
        playerData.savePlayerState(player);
        playerData.setCageNumber(cageNumber);
        this.players.put(player.getUniqueId(), playerData);
        this.alivePlayers.add(player.getUniqueId());
        CageLocation cage = this.cageLocations.get(cageNumber);
        if (cage != null) {
            cage.teleportPlayer(player);
            SoundUtil.playJoinArena(player);
            if (this.state == GameState.WAITING || this.state == GameState.STARTING) {
                Material cageColor = this.plugin.getCageManager().getPlayerCageColor(player);
                cage.buildCage(this.plugin.getConfigManager().getCageSize(), cageColor);
            }
        }
        this.preparePlayer(player);
        if (this.plugin.getConfigManager().isChatNotificationEnabled("player-join")) {
            this.broadcastMessage(this.plugin.getMessages().getWithPrefix("joined-arena", "arena", this.name, "current", String.valueOf(this.players.size()), "max", String.valueOf(this.maxPlayers)));
        }
        if (this.state == GameState.WAITING && this.players.size() >= this.minPlayers) {
            this.startCountdown();
        } else if (this.state == GameState.DISABLED && this.players.size() >= 1) {
            this.setState(GameState.WAITING);
        }
        return true;
    }

    public void removePlayer(Player player, boolean teleportToLobby) {
        Location lobby;
        UUID uuid = player.getUniqueId();
        if (!this.players.containsKey(uuid)) {
            this.plugin.getLogger().warning("Attempted to remove player " + player.getName() + " who is not in arena " + this.name);
            return;
        }
        this.plugin.getLogger().info("Removing player " + player.getName() + " from arena " + this.name);
        PlayerData playerData = this.players.remove(uuid);
        this.alivePlayers.remove(uuid);
        this.spectators.remove(uuid);
        this.votes.remove(uuid);
        this.plugin.getLootModeVoteManager().removeVote(player, this);
        this.plugin.getArenaManager().setPlayerArena(player, null);
        this.plugin.getScoreboardManager().removePlayer(player);
        if (this.plugin.getSpectatorManager().isSpectator(player)) {
            this.plugin.getSpectatorManager().removeSpectator(player);
        }
        if (playerData != null) {
            CageLocation cage;
            playerData.restorePlayerState(player);
            this.plugin.getLogger().info("Restored state for " + player.getName());
            if ((this.state == GameState.WAITING || this.state == GameState.STARTING) && (cage = this.cageLocations.get(playerData.getCageNumber())) != null) {
                cage.removeCage();
            }
        }
        if (teleportToLobby && (lobby = this.plugin.getConfigManager().getLobbyLocation()) != null) {
            player.teleport(lobby);
            SoundUtil.playLeaveArena(player);
            this.plugin.getLogger().info("Teleported " + player.getName() + " to lobby");
        }
        player.sendMessage(this.plugin.getMessages().getWithPrefix("left-arena"));
        if (this.state == GameState.STARTING && this.players.size() < this.minPlayers) {
            this.cancelCountdown();
            this.setState(GameState.WAITING);
            if (this.plugin.getConfigManager().isChatNotificationEnabled("not-enough-players")) {
                this.broadcastMessage(this.plugin.getMessages().getWithPrefix("not-enough-players", "min", String.valueOf(this.minPlayers)));
            }
        } else if (this.state == GameState.IN_GAME) {
            this.checkWinCondition();
        } else if (this.players.isEmpty()) {
            if (this.gameActuallyStarted) {
                this.plugin.getLogger().info("Arena " + this.name + " emptied after game started - regenerating");
                this.gameActuallyStarted = false;
                this.setState(GameState.RESETTING);
                this.resetArena();
            } else {
                this.plugin.getLogger().info("Arena " + this.name + " emptied before game started - no regeneration needed");
                this.setState(GameState.WAITING);
            }
        }
        this.plugin.getLogger().info("Player " + player.getName() + " successfully removed from " + this.name);
    }

    public void removeSpectator(Player player) {
        UUID uuid = player.getUniqueId();
        if (!this.spectators.contains(uuid)) {
            return;
        }
        this.spectators.remove(uuid);
        PlayerData playerData = this.players.remove(uuid);
        this.plugin.getArenaManager().setPlayerArena(player, null);
        this.plugin.getScoreboardManager().removePlayer(player);
        if (playerData != null) {
            playerData.restorePlayerState(player);
        }
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(false);
        player.setFlying(false);
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showPlayer((Plugin)this.plugin, player);
            player.showPlayer((Plugin)this.plugin, online);
        }
        Location lobby = this.plugin.getConfigManager().getLobbyLocation();
        if (lobby != null) {
            player.teleport(lobby);
        }
        player.sendMessage(this.plugin.getMessages().getWithPrefix("left-arena"));
    }

    public void forceSaveSchematicSync() {
        if (this.pos1 == null || this.pos2 == null) {
            this.plugin.getLogger().severe("Cannot save schematic - positions null!");
            return;
        }
        this.plugin.getLogger().warning("FORCE SAVING SCHEMATIC (SYNC): " + this.name);
        try {
            ArenaSchematic schematic = this.captureBlocksSync();
            if (schematic == null) {
                this.plugin.getLogger().severe("Failed to capture blocks!");
                return;
            }
            this.plugin.getSchematicManager().saveSchematicSync(this.name, schematic);
            this.schematicSaved = true;
            this.save();
            this.plugin.getLogger().info("\u2713 FORCE SAVE COMPLETE: " + this.name);
        }
        catch (Exception e) {
            this.plugin.getLogger().severe("Force save failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private ArenaSchematic captureBlocksSync() {
        World world = this.pos1.getWorld();
        int minX = Math.min(this.pos1.getBlockX(), this.pos2.getBlockX());
        int minY = Math.min(this.pos1.getBlockY(), this.pos2.getBlockY());
        int minZ = Math.min(this.pos1.getBlockZ(), this.pos2.getBlockZ());
        int maxX = Math.max(this.pos1.getBlockX(), this.pos2.getBlockX());
        int maxY = Math.max(this.pos1.getBlockY(), this.pos2.getBlockY());
        int maxZ = Math.max(this.pos1.getBlockZ(), this.pos2.getBlockZ());
        int sizeX = maxX - minX + 1;
        int sizeY = maxY - minY + 1;
        int sizeZ = maxZ - minZ + 1;
        ArenaSchematic schematic = new ArenaSchematic(world.getName(), minX, minY, minZ, sizeX, sizeY, sizeZ);
        int totalBlocks = 0;
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    Block block = world.getBlockAt(x, y, z);
                    int relX = x - minX;
                    int relY = y - minY;
                    int relZ = z - minZ;
                    BlockData blockData = block.getBlockData();
                    schematic.setBlock(relX, relY, relZ, blockData.getAsString());
                    ++totalBlocks;
                }
            }
        }
        this.plugin.getLogger().info("Captured " + totalBlocks + " blocks (" + schematic.getTotalBlocks() + " non-air)");
        return schematic;
    }

    private int findAvailableCage() {
        Set usedCages = this.players.values().stream().map(PlayerData::getCageNumber).collect(Collectors.toSet());
        int maxCages = this.plugin.getConfigManager().getMaxCages();
        for (int i = 1; i <= maxCages; ++i) {
            if (!this.cageLocations.containsKey(i) || usedCages.contains(i)) continue;
            return i;
        }
        return -1;
    }

    private void preparePlayer(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        player.setExp(0.0f);
        player.setLevel(0);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.setFireTicks(0);
    }

    private void createCountdownBossBar() {
        this.countdownBossBar = BossBar.bossBar((Component)FortunePillars.parse("<yellow><bold>Game starting...</bold></yellow>"), (float)1.0f, (BossBar.Color)this.plugin.getConfigManager().getCountdownBossBarColor(), (BossBar.Overlay)this.plugin.getConfigManager().getCountdownBossBarStyle());
    }

    private void addPlayerToCountdownBossBar(Player player) {
        if (this.countdownBossBar == null) {
            return;
        }
        if (this.countdownBossBarViewers.add(player.getUniqueId())) {
            player.showBossBar(this.countdownBossBar);
        }
    }

    private void removePlayerFromCountdownBossBar(Player player) {
        if (this.countdownBossBar == null) {
            return;
        }
        if (this.countdownBossBarViewers.remove(player.getUniqueId())) {
            player.hideBossBar(this.countdownBossBar);
        }
    }

    private void destroyCountdownBossBar() {
        if (this.countdownBossBar == null) {
            return;
        }
        for (UUID uuid : new HashSet<UUID>(this.countdownBossBarViewers)) {
            Player p = Bukkit.getPlayer((UUID)uuid);
            if (p == null) continue;
            p.hideBossBar(this.countdownBossBar);
        }
        this.countdownBossBarViewers.clear();
        this.countdownBossBar = null;
    }

    private void updateCountdownBossBar(int secondsLeft, int totalSeconds) {
        if (this.countdownBossBar == null) {
            return;
        }
        float progress = totalSeconds > 0 ? Math.max(0.0f, Math.min(1.0f, (float)secondsLeft / (float)totalSeconds)) : 1.0f;
        this.countdownBossBar.name(FortunePillars.parse("<yellow><bold>Starting in " + secondsLeft + "s</bold></yellow>"));
        this.countdownBossBar.progress(progress);
        if (secondsLeft <= 5) {
            this.countdownBossBar.color(BossBar.Color.RED);
        } else if (secondsLeft <= 10) {
            this.countdownBossBar.color(BossBar.Color.YELLOW);
        } else {
            this.countdownBossBar.color(this.plugin.getConfigManager().getCountdownBossBarColor());
        }
    }

    public void startCountdown() {
        if (this.state != GameState.WAITING) {
            return;
        }
        this.setState(GameState.STARTING);
        int totalSeconds = this.plugin.getConfigManager().getCountdownSeconds();
        this.countdown = this.plugin.getConfigManager().getCountdownSeconds();
        if (this.plugin.getConfigManager().isCountdownBossBarEnabled()) {
            this.createCountdownBossBar();
            this.getOnlinePlayers().forEach(this::addPlayerToCountdownBossBar);
        }
        for (PlayerData pd : this.players.values()) {
            Player player;
            CageLocation cage = this.cageLocations.get(pd.getCageNumber());
            if (cage == null || (player = Bukkit.getPlayer((UUID)pd.getUuid())) == null) continue;
            Material cageColor = this.plugin.getCageManager().getPlayerCageColor(player);
            cage.buildCage(this.plugin.getConfigManager().getCageSize(), cageColor);
        }
        this.countdownTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            if (this.countdown <= 0) {
                this.startGame();
                return;
            }
            if (this.plugin.getConfigManager().isCountdownBossBarEnabled()) {
                this.updateCountdownBossBar(this.countdown, totalSeconds);
            }
            if (this.plugin.getConfigManager().isCountdownActionBarEnabled()) {
                Component ab = FortunePillars.parse("<gold><bold>Starting in " + this.countdown + "s</bold></gold>");
                this.getOnlinePlayers().forEach(p -> p.sendActionBar(ab));
            }
            if (this.countdown <= 10 || this.countdown % 10 == 0) {
                if (this.plugin.getConfigManager().isChatNotificationEnabled("countdown")) {
                    this.broadcastMessage(this.plugin.getMessages().getWithPrefix("game-starting", "seconds", String.valueOf(this.countdown)));
                }
                if (this.countdown <= 5) {
                    Title title = Title.title((Component)FortunePillars.parse("<yellow><bold>" + this.countdown + "</bold></yellow>"), (Component)Component.empty(), (Title.Times)Title.Times.times((Duration)Duration.ZERO, (Duration)Duration.ofMillis(1000L), (Duration)Duration.ZERO));
                    this.getOnlinePlayers().forEach(p -> {
                        p.showTitle(title);
                        if (this.countdown <= 5) {
                            SoundUtil.playCountdownFinal(p);
                        } else {
                            SoundUtil.playCountdown(p);
                        }
                    });
                } else {
                    this.getOnlinePlayers().forEach(SoundUtil::playCountdown);
                }
            }
            --this.countdown;
        }, 0L, 20L);
    }

    public void cancelCountdown() {
        if (this.countdownTask != null) {
            this.countdownTask.cancel();
            this.countdownTask = null;
        }
        this.destroyCountdownBossBar();
        this.getOnlinePlayers().forEach(SoundUtil::playCountdownStop);
    }

    public void startGame() {
        this.cancelCountdown();
        this.setState(GameState.IN_GAME);
        this.gameActuallyStarted = true;
        this.plugin.getLogger().info("STARTING GAME IN ARENA: " + this.name);
        if (this.plugin.getConfigManager().isVotingEnabled()) {
            GameModeType votedGameMode = this.plugin.getVoteManager().calculateWinningMode(this);
            LootMode votedLootMode = this.plugin.getLootModeVoteManager().calculateWinningMode(this);
            this.gameMode = votedGameMode != null ? votedGameMode : this.defaultGameMode;
            this.lootMode = votedLootMode != null ? votedLootMode : LootMode.MULTI_NORMAL_MEDIUM;
        } else {
            this.gameMode = this.defaultGameMode;
            this.lootMode = this.plugin.getLootModeVoteManager().getDefaultLootMode();
        }
        this.plugin.getLogger().info("Game Mode: " + this.gameMode.name() + ", Loot Mode: " + this.lootMode.name() + " (voting " + (this.plugin.getConfigManager().isVotingEnabled() ? "enabled" : "DISABLED") + ")");
        if (!this.schematicSaved || !this.plugin.getSchematicManager().hasSchematic(this.name)) {
            this.plugin.getLogger().warning("Arena " + this.name + " has no schematic! Auto-saving...");
            this.forceSaveSchematicSync();
        }
        if (this.plugin.getConfigManager().isChatNotificationEnabled("game-settings")) {
            this.broadcastMessage((Component)Component.empty());
            this.broadcastMessage(FortunePillars.parse("<gold><bold>\u2550\u2550\u2550 Game Settings \u2550\u2550\u2550</bold></gold>"));
            this.broadcastMessage(FortunePillars.parseWithPrefix("<yellow>Game Mode: <gold>" + this.gameMode.getDisplayName()));
            this.broadcastMessage(FortunePillars.parse("<gray>" + this.gameMode.getDescription()));
            this.broadcastMessage((Component)Component.empty());
            this.broadcastMessage(FortunePillars.parseWithPrefix("<aqua>Loot Mode: " + this.lootMode.getColoredDisplayName()));
            this.broadcastMessage(FortunePillars.parse("<gray>Drops every " + this.lootMode.getMinIntervalSeconds() + "-" + this.lootMode.getMaxIntervalSeconds() + " seconds"));
            this.broadcastMessage(FortunePillars.parse("<gold><bold>\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550</bold></gold>"));
            this.broadcastMessage((Component)Component.empty());
        }
        for (CageLocation cage : this.cageLocations.values()) {
            cage.removeCage();
        }
        this.getOnlinePlayers().forEach(SoundUtil::playGameStart);
        Title startTitle = Title.title((Component)FortunePillars.parse("<green><bold>GO!</bold></green>"), (Component)FortunePillars.parse("<gray>" + this.gameMode.getDisplayName()), (Title.Times)Title.Times.times((Duration)Duration.ZERO, (Duration)Duration.ofSeconds(2L), (Duration)Duration.ofMillis(500L)));
        this.getOnlinePlayers().forEach(p -> p.showTitle(startTitle));
        if (this.plugin.getConfigManager().isChatNotificationEnabled("game-started")) {
            this.broadcastMessage(this.plugin.getMessages().getWithPrefix("game-started"));
        }
        this.plugin.getScoreboardManager().startScoreboard(this);
        this.startGracePeriod();
        this.startLootTask();
        this.plugin.getGameModeManager().startGameModeTasks(this);
        this.plugin.getLogger().info("Game started successfully in " + this.name);
    }

    private void startGracePeriod() {
        int gracePeriod = this.plugin.getConfigManager().getGracePeriodSeconds();
        if (gracePeriod <= 0) {
            return;
        }
        this.gracePeriodActive = true;
        this.broadcastMessage(this.plugin.getMessages().getWithPrefix("grace-period-start", "seconds", String.valueOf(gracePeriod)));
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            this.gracePeriodActive = false;
            this.broadcastMessage(this.plugin.getMessages().getWithPrefix("grace-period-end"));
            this.getOnlinePlayers().forEach(SoundUtil::playGracePeriodEnd);
        }, (long)gracePeriod * 20L);
    }

    private void startLootTask() {
        if (this.lootDropTask != null) {
            this.lootDropTask.stop();
            this.lootDropTask = null;
        }
        this.lootDropTask = new LootDropTask(this.plugin, this, this.lootMode);
        this.lootDropTask.runTaskTimer((Plugin)this.plugin, 0L, 1L);
    }

    public void endGame(Player winner) {
        this.setState(GameState.ENDING);
        this.plugin.getScoreboardManager().stopScoreboard(this);
        this.cancelAllTasks();
        String gameModeKey = this.gameMode.name();
        if (winner != null) {
            this.plugin.getLogger().info("GAME WINNER: " + winner.getName());
            this.broadcastMessage(this.plugin.getMessages().getWithPrefix("player-won", "player", winner.getName()));
            SoundUtil.playVictory(winner);
            UUID winnerUUID = winner.getUniqueId();
            String winnerName = winner.getName();
            this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
                this.plugin.getDatabase().createPlayerData(winnerUUID, winnerName);
                try {
                    Thread.sleep(100L);
                }
                catch (InterruptedException interruptedException) {
                    // empty catch block
                }
                this.plugin.getDatabase().addWin(winnerUUID);
                this.plugin.getDatabase().addWinstreak(winnerUUID);
                this.plugin.getDatabase().addGameModeWin(winnerUUID, gameModeKey);
                this.plugin.getDatabase().addGameModePlayed(winnerUUID, gameModeKey);
                this.plugin.getDatabase().addGameModeWinstreak(winnerUUID, gameModeKey);
            });
            this.plugin.getCosmeticsManager().playWinCelebration(winner, this);
            Title winTitle = Title.title((Component)FortunePillars.parse("<gold><bold>\u2605 VICTORY! \u2605</bold></gold>"), (Component)FortunePillars.parse("<yellow>You are the champion!"), (Title.Times)Title.Times.times((Duration)Duration.ZERO, (Duration)Duration.ofSeconds(5L), (Duration)Duration.ofSeconds(1L)));
            winner.showTitle(winTitle);
        }
        this.getOnlinePlayers().stream().filter(p -> winner == null || !p.equals((Object)winner)).forEach(SoundUtil::playGameEnd);
        HashSet<UUID> allParticipants = new HashSet<UUID>(this.players.keySet());
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            for (UUID uuid : allParticipants) {
                if (winner != null && uuid.equals(winner.getUniqueId())) continue;
                Player p = Bukkit.getPlayer((UUID)uuid);
                if (p != null) {
                    this.plugin.getDatabase().createPlayerData(uuid, p.getName());
                } else {
                    this.plugin.getDatabase().createPlayerData(uuid);
                }
                try {
                    Thread.sleep(50L);
                }
                catch (InterruptedException interruptedException) {
                    // empty catch block
                }
                this.plugin.getDatabase().addGamePlayed(uuid);
                this.plugin.getDatabase().addGameModeLoss(uuid, gameModeKey);
                this.plugin.getDatabase().addGameModePlayed(uuid, gameModeKey);
                this.plugin.getDatabase().resetGameModeWinstreak(uuid, gameModeKey);
            }
            if (winner != null) {
                this.plugin.getDatabase().addGamePlayed(winner.getUniqueId());
            }
            this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> this.plugin.getLeaderboardManager().updateLeaderboards(), 60L);
        });
        this.broadcastMessage(this.plugin.getMessages().getWithPrefix("game-ended"));
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            this.teleportAllToLobby();
            this.resetArena();
        }, 100L);
    }

    public void forceEnd() {
        this.gameActuallyStarted = false;
        this.plugin.getScoreboardManager().stopScoreboard(this);
        this.cancelAllTasks();
        this.teleportAllToLobby();
        this.resetArena();
    }

    private void clearAllCageBlocks() {
        if (this.pos1 == null || this.pos2 == null) {
            return;
        }
        World world = this.pos1.getWorld();
        if (world == null) {
            return;
        }
        int minX = Math.min(this.pos1.getBlockX(), this.pos2.getBlockX());
        int maxX = Math.max(this.pos1.getBlockX(), this.pos2.getBlockX());
        int minY = Math.min(this.pos1.getBlockY(), this.pos2.getBlockY());
        int maxY = Math.max(this.pos1.getBlockY(), this.pos2.getBlockY());
        int minZ = Math.min(this.pos1.getBlockZ(), this.pos2.getBlockZ());
        int maxZ = Math.max(this.pos1.getBlockZ(), this.pos2.getBlockZ());
        int removed = 0;
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    Block block = world.getBlockAt(x, y, z);
                    Material type = block.getType();
                    if (!type.name().contains("GLASS") || type.name().contains("GLASS_PANE")) continue;
                    block.setType(Material.AIR);
                    ++removed;
                }
            }
        }
        if (removed > 0) {
            this.plugin.getLogger().info("Cleared " + removed + " potential cage blocks from arena");
        }
    }

    private void cancelAllTasks() {
        if (this.countdownTask != null) {
            this.countdownTask.cancel();
            this.countdownTask = null;
        }
        if (this.gameTask != null) {
            this.gameTask.cancel();
            this.gameTask = null;
        }
        if (this.lootDropTask != null) {
            this.lootDropTask.stop();
            this.lootDropTask = null;
        }
        if (this.gameModeTask != null) {
            this.gameModeTask.cancel();
            this.gameModeTask = null;
        }
        this.plugin.getGameModeManager().stopGameModeTasks(this);
    }

    private void teleportAllToLobby() {
        Player p;
        Player player;
        Location lobby = this.plugin.getConfigManager().getLobbyLocation();
        this.plugin.getLogger().info("Teleporting all players to lobby from arena: " + this.name);
        HashSet<UUID> playersCopy = new HashSet<UUID>(this.players.keySet());
        HashSet<UUID> spectatorsCopy = new HashSet<UUID>(this.spectators);
        for (UUID uuid : spectatorsCopy) {
            player = Bukkit.getPlayer((UUID)uuid);
            if (player == null || !player.isOnline()) continue;
            this.plugin.getSpectatorManager().removeSpectator(player);
        }
        for (UUID uuid : playersCopy) {
            p = Bukkit.getPlayer((UUID)uuid);
            if (p == null) continue;
            this.plugin.getArenaManager().setPlayerArena(p, null);
            this.plugin.getScoreboardManager().removePlayer(p);
        }
        for (UUID uuid : spectatorsCopy) {
            p = Bukkit.getPlayer((UUID)uuid);
            if (p == null) continue;
            this.plugin.getArenaManager().setPlayerArena(p, null);
            this.plugin.getScoreboardManager().removePlayer(p);
        }
        for (UUID uuid : playersCopy) {
            player = Bukkit.getPlayer((UUID)uuid);
            if (player == null || !player.isOnline()) continue;
            PlayerData pd = this.players.get(uuid);
            if (pd != null) {
                pd.restorePlayerState(player);
            }
            if (lobby != null) {
                player.teleport(lobby);
            }
            player.sendMessage(this.plugin.getMessages().getWithPrefix("left-arena"));
        }
        this.players.clear();
        this.alivePlayers.clear();
        this.spectators.clear();
        this.votes.clear();
        this.plugin.getLootModeVoteManager().clearVotes(this);
    }

    public void saveSchematic() {
        if (this.pos1 == null || this.pos2 == null) {
            this.plugin.getLogger().warning("Cannot save schematic for " + this.name + " - positions not set!");
            return;
        }
        this.plugin.getSchematicManager().saveArena(this, success -> {
            if (success.booleanValue()) {
                this.schematicSaved = true;
                this.plugin.getLogger().info("\u2713 Successfully saved schematic for " + this.name);
            } else {
                this.plugin.getLogger().warning("\u2717 Failed to save schematic for " + this.name);
            }
        });
    }

    private void resetArena() {
        this.setState(GameState.RESETTING);
        this.gameActuallyStarted = false;
        this.plugin.getLogger().info("RESETTING ARENA: " + this.name);
        this.votes.clear();
        this.plugin.getLootModeVoteManager().clearVotes(this);
        for (CageLocation cage : this.cageLocations.values()) {
            cage.removeCage();
        }
        this.clearAllEntities();
        boolean hasSchematic = this.plugin.getSchematicManager().hasSchematic(this.name);
        if (!hasSchematic) {
            this.plugin.getLogger().severe("NO SCHEMATIC FOUND FOR ARENA: " + this.name);
            Bukkit.broadcast((Component)FortunePillars.parseWithPrefix("<red><bold>\u26a0 Arena " + this.name + " cannot regenerate - no schematic!"));
            this.setState(GameState.WAITING);
            return;
        }
        this.plugin.getSchematicManager().restoreArena(this, () -> {
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.clearAllCageBlocks(), 10L);
            this.setState(GameState.WAITING);
            this.plugin.getLogger().info("ARENA " + this.name + " RESTORED SUCCESSFULLY!");
            if (this.plugin.getConfigManager().isChatNotificationEnabled("arena-restored")) {
                Bukkit.broadcast((Component)FortunePillars.parseWithPrefix("<green>\u2713 Arena <yellow>" + this.name + "</yellow> is ready for the next game!"));
            }
        });
    }

    private void clearAllEntities() {
        if (this.pos1 == null || this.pos2 == null) {
            return;
        }
        World world = this.pos1.getWorld();
        if (world == null) {
            return;
        }
        int minX = Math.min(this.pos1.getBlockX(), this.pos2.getBlockX());
        int maxX = Math.max(this.pos1.getBlockX(), this.pos2.getBlockX());
        int minY = Math.min(this.pos1.getBlockY(), this.pos2.getBlockY());
        int maxY = Math.max(this.pos1.getBlockY(), this.pos2.getBlockY());
        int minZ = Math.min(this.pos1.getBlockZ(), this.pos2.getBlockZ());
        int maxZ = Math.max(this.pos1.getBlockZ(), this.pos2.getBlockZ());
        int entityCount = 0;
        for (Entity entity : world.getEntities()) {
            Location loc;
            if (entity instanceof Player || !((loc = entity.getLocation()).getX() >= (double)minX) || !(loc.getX() <= (double)maxX) || !(loc.getY() >= (double)minY) || !(loc.getY() <= (double)maxY) || !(loc.getZ() >= (double)minZ) || !(loc.getZ() <= (double)maxZ)) continue;
            entity.remove();
            ++entityCount;
        }
        if (entityCount > 0) {
            this.plugin.getLogger().info("Cleared " + entityCount + " entities from arena " + this.name);
        }
    }

    public void eliminatePlayer(Player player, Player killer) {
        UUID uuid = player.getUniqueId();
        if (!this.alivePlayers.contains(uuid)) {
            return;
        }
        this.alivePlayers.remove(uuid);
        this.spectators.add(uuid);
        this.plugin.getDatabase().addDeath(uuid);
        if (killer != null && !killer.equals((Object)player)) {
            this.plugin.getDatabase().addKill(killer.getUniqueId());
        }
        this.plugin.getSpectatorManager().makeSpectator(player, this);
        SoundUtil.playSpectate(player);
        this.getAlivePlayers().forEach(SoundUtil::playElimination);
        if (this.plugin.getConfigManager().isChatNotificationEnabled("player-eliminated")) {
            this.broadcastMessage(this.plugin.getMessages().getWithPrefix("player-eliminated", "player", player.getName(), "remaining", String.valueOf(this.alivePlayers.size())));
        }
        this.checkWinCondition();
    }

    private void checkWinCondition() {
        if (this.state != GameState.IN_GAME) {
            return;
        }
        if (this.alivePlayers.size() <= 1) {
            Player winner = null;
            if (this.alivePlayers.size() == 1) {
                UUID winnerId = this.alivePlayers.iterator().next();
                winner = Bukkit.getPlayer((UUID)winnerId);
            }
            this.endGame(winner);
        }
    }

    public boolean isActive() {
        return this.state == GameState.IN_GAME;
    }

    public boolean isEnabled() {
        return this.state == GameState.WAITING || this.state == GameState.STARTING;
    }

    public void setEnabled(boolean enabled) {
        this.manuallyEnabled = enabled;
        if (enabled) {
            if (this.isSetupComplete()) {
                this.setState(GameState.WAITING);
                this.save();
            }
        } else if (this.state == GameState.WAITING || this.state == GameState.DISABLED) {
            this.setState(GameState.DISABLED);
        }
        this.save();
    }

    public boolean isFullyConfigured() {
        return this.isSetupComplete();
    }

    public boolean hasPlayer(UUID uuid) {
        return this.players.containsKey(uuid);
    }

    public GameModeType getDefaultGameMode() {
        return this.defaultGameMode;
    }

    public void setDefaultGameMode(GameModeType defaultGameMode) {
        this.defaultGameMode = defaultGameMode;
        this.save();
    }

    public void setLootMode(LootMode mode) {
        this.lootMode = mode;
        if (this.lootDropTask != null && this.state == GameState.IN_GAME) {
            this.lootDropTask.stop();
            this.startLootTask();
        }
    }

    public LootMode getLootMode() {
        return this.lootMode;
    }

    public void broadcastMessage(Component message) {
        this.getOnlinePlayers().forEach(p -> p.sendMessage(message));
    }

    public void broadcastTitle(Title title) {
        this.getOnlinePlayers().forEach(p -> p.showTitle(title));
    }

    public List<Player> getOnlinePlayers() {
        return this.players.keySet().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).filter(OfflinePlayer::isOnline).collect(Collectors.toList());
    }

    public List<Player> getAlivePlayers() {
        return this.alivePlayers.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).filter(OfflinePlayer::isOnline).collect(Collectors.toList());
    }

    public List<Player> getSpectators() {
        return this.spectators.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).filter(OfflinePlayer::isOnline).collect(Collectors.toList());
    }

    public boolean isPlayerAlive(UUID uuid) {
        return this.alivePlayers.contains(uuid);
    }

    public boolean isSpectator(UUID uuid) {
        return this.spectators.contains(uuid);
    }

    public boolean containsPlayer(UUID uuid) {
        return this.players.containsKey(uuid);
    }

    public Location getCenter() {
        if (this.pos1 == null || this.pos2 == null) {
            return null;
        }
        return new Location(this.pos1.getWorld(), (this.pos1.getX() + this.pos2.getX()) / 2.0, (this.pos1.getY() + this.pos2.getY()) / 2.0, (this.pos1.getZ() + this.pos2.getZ()) / 2.0);
    }

    public Location getCorner1() {
        return this.pos1;
    }

    public Location getCorner2() {
        return this.pos2;
    }

    public boolean isInArena(Location location) {
        if (this.pos1 == null || this.pos2 == null) {
            return false;
        }
        if (location == null || location.getWorld() == null) {
            return false;
        }
        if (!location.getWorld().equals((Object)this.pos1.getWorld())) {
            return false;
        }
        double minX = Math.min(this.pos1.getX(), this.pos2.getX());
        double maxX = Math.max(this.pos1.getX(), this.pos2.getX());
        double minY = Math.min(this.pos1.getY(), this.pos2.getY());
        double maxY = Math.max(this.pos1.getY(), this.pos2.getY());
        double minZ = Math.min(this.pos1.getZ(), this.pos2.getZ());
        double maxZ = Math.max(this.pos1.getZ(), this.pos2.getZ());
        double x = location.getBlockX();
        double y = location.getBlockY();
        double z = location.getBlockZ();
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public boolean isAboveBuildLimit(Location location) {
        if (this.pos1 == null || this.pos2 == null) {
            return false;
        }
        if (location == null || location.getWorld() == null) {
            return false;
        }
        if (!location.getWorld().equals((Object)this.pos1.getWorld())) {
            return false;
        }
        double maxY = Math.max(this.pos1.getY(), this.pos2.getY());
        return (double)location.getBlockY() > maxY;
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("name", (Object)this.name);
        config.set("max-players", (Object)this.maxPlayers);
        config.set("min-players", (Object)this.minPlayers);
        config.set("default-game-mode", (Object)this.defaultGameMode.name());
        config.set("loot-mode", (Object)this.lootMode.name());
        config.set("schematic-saved", (Object)this.schematicSaved);
        config.set("enabled", (Object)this.manuallyEnabled);
        if (this.pos1 != null) {
            config.set("pos1.world", (Object)this.pos1.getWorld().getName());
            config.set("pos1.x", (Object)this.pos1.getBlockX());
            config.set("pos1.y", (Object)this.pos1.getBlockY());
            config.set("pos1.z", (Object)this.pos1.getBlockZ());
        }
        if (this.pos2 != null) {
            config.set("pos2.world", (Object)this.pos2.getWorld().getName());
            config.set("pos2.x", (Object)this.pos2.getBlockX());
            config.set("pos2.y", (Object)this.pos2.getBlockY());
            config.set("pos2.z", (Object)this.pos2.getBlockZ());
        }
        if (this.spectatorSpawn != null) {
            config.set("spectator-spawn.world", (Object)this.spectatorSpawn.getWorld().getName());
            config.set("spectator-spawn.x", (Object)this.spectatorSpawn.getX());
            config.set("spectator-spawn.y", (Object)this.spectatorSpawn.getY());
            config.set("spectator-spawn.z", (Object)this.spectatorSpawn.getZ());
            config.set("spectator-spawn.yaw", (Object)Float.valueOf(this.spectatorSpawn.getYaw()));
            config.set("spectator-spawn.pitch", (Object)Float.valueOf(this.spectatorSpawn.getPitch()));
        }
        for (Map.Entry<Integer, CageLocation> entry : this.cageLocations.entrySet()) {
            config.set("cages." + String.valueOf(entry.getKey()), entry.getValue().serialize());
        }
        try {
            config.save(this.arenaFile);
        }
        catch (IOException e) {
            this.plugin.getLogger().severe("Failed to save arena " + this.name + ": " + e.getMessage());
        }
    }

    public void load() {
        World world;
        String worldName;
        if (!this.arenaFile.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration((File)this.arenaFile);
        this.maxPlayers = config.getInt("max-players", this.plugin.getConfigManager().getMaxPlayers());
        this.minPlayers = config.getInt("min-players", this.plugin.getConfigManager().getMinPlayers());
        this.schematicSaved = config.getBoolean("schematic-saved", false);
        this.manuallyEnabled = config.getBoolean("enabled", true);
        String gameModeName = config.getString("default-game-mode", "NORMAL");
        try {
            this.defaultGameMode = GameModeType.valueOf(gameModeName);
        }
        catch (IllegalArgumentException e) {
            this.defaultGameMode = GameModeType.NORMAL;
        }
        String lootModeName = config.getString("loot-mode", "MULTI_NORMAL_MEDIUM");
        try {
            this.lootMode = LootMode.valueOf(lootModeName);
        }
        catch (IllegalArgumentException e) {
            switch (lootModeName) {
                case "FAST_OP": {
                    LootMode lootMode = LootMode.MULTI_OP_FAST;
                    break;
                }
                case "NORMAL": {
                    LootMode lootMode = LootMode.MULTI_NORMAL_MEDIUM;
                    break;
                }
                case "SLOW": {
                    LootMode lootMode = LootMode.MULTI_NORMAL_SLOW;
                    break;
                }
                default: {
                    LootMode lootMode = this.lootMode = LootMode.MULTI_NORMAL_MEDIUM;
                }
            }
        }
        if (config.contains("pos1")) {
            worldName = config.getString("pos1.world");
            world = Bukkit.getWorld((String)worldName);
            if (world != null) {
                this.pos1 = new Location(world, (double)config.getInt("pos1.x"), (double)config.getInt("pos1.y"), (double)config.getInt("pos1.z"));
            } else {
                this.plugin.getLogger().warning("Arena " + this.name + ": world '" + worldName + "' for pos1 is not loaded! Will retry when world loads.");
                this.pendingPos1World = worldName;
                this.pendingPos1X = config.getInt("pos1.x");
                this.pendingPos1Y = config.getInt("pos1.y");
                this.pendingPos1Z = config.getInt("pos1.z");
            }
        }
        if (config.contains("pos2")) {
            worldName = config.getString("pos2.world");
            world = Bukkit.getWorld((String)worldName);
            if (world != null) {
                this.pos2 = new Location(world, (double)config.getInt("pos2.x"), (double)config.getInt("pos2.y"), (double)config.getInt("pos2.z"));
            } else {
                this.plugin.getLogger().warning("Arena " + this.name + ": world '" + worldName + "' for pos2 is not loaded! Will retry when world loads.");
                this.pendingPos2World = worldName;
                this.pendingPos2X = config.getInt("pos2.x");
                this.pendingPos2Y = config.getInt("pos2.y");
                this.pendingPos2Z = config.getInt("pos2.z");
            }
        }
        if (config.contains("spectator-spawn")) {
            worldName = config.getString("spectator-spawn.world");
            world = Bukkit.getWorld((String)worldName);
            if (world != null) {
                this.spectatorSpawn = new Location(world, config.getDouble("spectator-spawn.x"), config.getDouble("spectator-spawn.y"), config.getDouble("spectator-spawn.z"), (float)config.getDouble("spectator-spawn.yaw"), (float)config.getDouble("spectator-spawn.pitch"));
            } else {
                this.plugin.getLogger().warning("Arena " + this.name + ": world '" + worldName + "' for spectator-spawn is not loaded! Will retry when world loads.");
                this.pendingSpectatorWorld = worldName;
                this.pendingSpectatorX = config.getDouble("spectator-spawn.x");
                this.pendingSpectatorY = config.getDouble("spectator-spawn.y");
                this.pendingSpectatorZ = config.getDouble("spectator-spawn.z");
                this.pendingSpectatorYaw = (float)config.getDouble("spectator-spawn.yaw");
                this.pendingSpectatorPitch = (float)config.getDouble("spectator-spawn.pitch");
            }
        }
        if (config.contains("cages")) {
            for (String key : config.getConfigurationSection("cages").getKeys(false)) {
                int cageNum = Integer.parseInt(key);
                Map cageData = config.getConfigurationSection("cages." + key).getValues(false);
                String cageWorld = (String)cageData.get("world");
                if (Bukkit.getWorld((String)cageWorld) == null) {
                    this.plugin.getLogger().warning("Arena " + this.name + ": world '" + cageWorld + "' for cage " + cageNum + " is not loaded! Will retry when world loads.");
                    this.pendingCages.put(cageNum, cageData);
                    continue;
                }
                CageLocation cage = CageLocation.deserialize(cageData);
                this.cageLocations.put(cageNum, cage);
            }
        }
        if (this.hasPendingData()) {
            this.schedulePendingRetry();
        }
        this.applyLoadedState();
    }

    public boolean isSetupComplete() {
        return this.pos1 != null && this.pos2 != null && this.spectatorSpawn != null && this.cageLocations.size() >= this.minPlayers;
    }

    public void delete() {
        this.forceEnd();
        if (this.arenaFile.exists()) {
            this.arenaFile.delete();
        }
        this.plugin.getSchematicManager().deleteSchematic(this.name);
    }

    private boolean hasPendingData() {
        return this.pendingPos1World != null || this.pendingPos2World != null || this.pendingSpectatorWorld != null || !this.pendingCages.isEmpty();
    }

    private void schedulePendingRetry() {
        int[] attempts = new int[]{0};
        int maxAttempts = 30;
        this.pendingRetryTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            attempts[0] = attempts[0] + 1;
            boolean resolved = this.retryPendingData();
            if (resolved || attempts[0] >= 30) {
                this.pendingRetryTask.cancel();
                this.pendingRetryTask = null;
                if (resolved) {
                    this.plugin.getLogger().info("Arena " + this.name + ": all pending world data resolved!");
                    this.applyLoadedState();
                } else {
                    this.plugin.getLogger().severe("Arena " + this.name + ": failed to resolve world data after 30 attempts! Check that all worlds are loading correctly.");
                }
            }
        }, 20L, 20L);
    }

    private boolean retryPendingData() {
        World world;
        if (this.pendingPos1World != null && (world = Bukkit.getWorld((String)this.pendingPos1World)) != null) {
            this.pos1 = new Location(world, (double)this.pendingPos1X, (double)this.pendingPos1Y, (double)this.pendingPos1Z);
            this.pendingPos1World = null;
            this.plugin.getLogger().info("Arena " + this.name + ": pos1 resolved in world " + world.getName());
        }
        if (this.pendingPos2World != null && (world = Bukkit.getWorld((String)this.pendingPos2World)) != null) {
            this.pos2 = new Location(world, (double)this.pendingPos2X, (double)this.pendingPos2Y, (double)this.pendingPos2Z);
            this.pendingPos2World = null;
            this.plugin.getLogger().info("Arena " + this.name + ": pos2 resolved in world " + world.getName());
        }
        if (this.pendingSpectatorWorld != null && (world = Bukkit.getWorld((String)this.pendingSpectatorWorld)) != null) {
            this.spectatorSpawn = new Location(world, this.pendingSpectatorX, this.pendingSpectatorY, this.pendingSpectatorZ, this.pendingSpectatorYaw, this.pendingSpectatorPitch);
            this.pendingSpectatorWorld = null;
            this.plugin.getLogger().info("Arena " + this.name + ": spectator spawn resolved in world " + world.getName());
        }
        Iterator<Map.Entry<Integer, Map<String, Object>>> iterator = this.pendingCages.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Map<String, Object>> entry = iterator.next();
            String cageWorld = (String)entry.getValue().get("world");
            World world2 = Bukkit.getWorld((String)cageWorld);
            if (world2 == null) continue;
            CageLocation cage = CageLocation.deserialize(entry.getValue());
            this.cageLocations.put(entry.getKey(), cage);
            iterator.remove();
            this.plugin.getLogger().info("Arena " + this.name + ": cage " + String.valueOf(entry.getKey()) + " resolved in world " + world2.getName());
        }
        return !this.hasPendingData();
    }

    private void applyLoadedState() {
        if (this.isSetupComplete()) {
            if (this.manuallyEnabled) {
                this.setState(GameState.WAITING);
                this.plugin.getLogger().info("Arena " + this.name + " loaded as ENABLED (WAITING)");
            } else {
                this.setState(GameState.DISABLED);
                this.plugin.getLogger().info("Arena " + this.name + " loaded as DISABLED");
            }
        } else {
            this.setState(GameState.DISABLED);
            this.plugin.getLogger().info("Arena " + this.name + " loaded as DISABLED (setup incomplete) pos1=" + (this.pos1 != null) + " pos2=" + (this.pos2 != null) + " spectator=" + (this.spectatorSpawn != null) + " cages=" + this.cageLocations.size() + "/" + this.minPlayers);
        }
    }

    public void retryPendingDataForWorld(String worldName) {
        boolean relevant;
        if (!this.hasPendingData()) {
            return;
        }
        boolean bl = relevant = worldName.equals(this.pendingPos1World) || worldName.equals(this.pendingPos2World) || worldName.equals(this.pendingSpectatorWorld) || this.pendingCages.values().stream().anyMatch(data -> worldName.equals(data.get("world")));
        if (!relevant) {
            return;
        }
        this.plugin.getLogger().info("Arena " + this.name + ": retrying pending data for world " + worldName);
        boolean resolved = this.retryPendingData();
        if (resolved) {
            if (this.pendingRetryTask != null) {
                this.pendingRetryTask.cancel();
                this.pendingRetryTask = null;
            }
            this.applyLoadedState();
        }
    }

    public String getName() {
        return this.name;
    }

    public GameState getState() {
        return this.state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public GameModeType getGameMode() {
        return this.gameMode;
    }

    public void setGameMode(GameModeType gameMode) {
        this.gameMode = gameMode;
    }

    public Location getPos1() {
        return this.pos1;
    }

    public void setPos1(Location pos1) {
        this.pos1 = pos1;
        this.save();
    }

    public Location getPos2() {
        return this.pos2;
    }

    public void setPos2(Location pos2) {
        this.pos2 = pos2;
        this.save();
    }

    public Location getSpectatorSpawn() {
        return this.spectatorSpawn;
    }

    public void setSpectatorSpawn(Location spectatorSpawn) {
        this.spectatorSpawn = spectatorSpawn;
        this.save();
    }

    public Map<Integer, CageLocation> getCageLocations() {
        return this.cageLocations;
    }

    public void setCageLocation(int number, CageLocation cage) {
        this.cageLocations.put(number, cage);
        this.save();
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
        this.save();
    }

    public int getMinPlayers() {
        return this.minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
        this.save();
    }

    public Map<UUID, PlayerData> getPlayers() {
        return this.players;
    }

    public int getPlayerCount() {
        return this.players.size();
    }

    public int getAliveCount() {
        return this.alivePlayers.size();
    }

    public Map<UUID, GameModeType> getVotes() {
        return this.votes;
    }

    public FortunePillars getPlugin() {
        return this.plugin;
    }

    public boolean isGracePeriodActive() {
        return this.gracePeriodActive;
    }

    public void setGameModeTask(BukkitTask task) {
        this.gameModeTask = task;
    }

    public PlayerData getPlayerData(UUID uuid) {
        return this.players.get(uuid);
    }
}

