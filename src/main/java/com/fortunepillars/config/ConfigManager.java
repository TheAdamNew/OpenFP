/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.bossbar.BossBar$Color
 *  net.kyori.adventure.bossbar.BossBar$Overlay
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.configuration.file.FileConfiguration
 */
package com.fortunepillars.config;

import com.fortunepillars.FortunePillars;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final FortunePillars plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(FortunePillars plugin) {
        this.plugin = plugin;
        this.loadConfig();
    }

    private void loadConfig() {
        this.plugin.saveDefaultConfig();
        this.config = this.plugin.getConfig();
        this.configFile = new File(this.plugin.getDataFolder(), "config.yml");
    }

    public void reloadConfig() {
        this.plugin.reloadConfig();
        this.config = this.plugin.getConfig();
    }

    public void saveConfig() {
        this.plugin.saveConfig();
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public int getMinPlayers() {
        return this.config.getInt("game.min-players", 2);
    }

    public int getMaxPlayers() {
        return this.config.getInt("game.max-players", 8);
    }

    public int getCountdownSeconds() {
        return this.config.getInt("game.countdown-seconds", 30);
    }

    public int getGracePeriodSeconds() {
        return this.config.getInt("game.grace-period-seconds", 10);
    }

    public boolean isVotingEnabled() {
        return this.config.getBoolean("voting.enabled", true);
    }

    public boolean isChatNotificationsEnabled() {
        return this.config.getBoolean("notifications.chat.enabled", true);
    }

    public boolean isChatNotificationEnabled(String key) {
        if (!this.isChatNotificationsEnabled()) {
            return false;
        }
        return this.config.getBoolean("notifications.chat." + key, true);
    }

    public boolean isCountdownBossBarEnabled() {
        return this.config.getBoolean("notifications.bossbar.enabled", false);
    }

    public BossBar.Color getCountdownBossBarColor() {
        String raw = this.config.getString("notifications.bossbar.color", "YELLOW");
        try {
            return BossBar.Color.valueOf((String)raw.toUpperCase());
        }
        catch (IllegalArgumentException e) {
            return BossBar.Color.YELLOW;
        }
    }

    public BossBar.Overlay getCountdownBossBarStyle() {
        String raw = this.config.getString("notifications.bossbar.style", "PROGRESS");
        try {
            return BossBar.Overlay.valueOf((String)raw.toUpperCase());
        }
        catch (IllegalArgumentException e) {
            return BossBar.Overlay.PROGRESS;
        }
    }

    public boolean isCountdownActionBarEnabled() {
        return this.config.getBoolean("notifications.actionbar.enabled", false);
    }

    public int getLootIntervalMin() {
        return this.config.getInt("loot.interval-min", 3);
    }

    public int getLootIntervalMax() {
        return this.config.getInt("loot.interval-max", 5);
    }

    public int getLootItemsMin() {
        return this.config.getInt("loot.items-min", 1);
    }

    public int getLootItemsMax() {
        return this.config.getInt("loot.items-max", 3);
    }

    public boolean includeSpawnEggs() {
        return this.config.getBoolean("loot.include-spawn-eggs", true);
    }

    public int getSwapperInterval() {
        return this.config.getInt("game-modes.swapper.interval", 30);
    }

    public int getShuffleInterval() {
        return this.config.getInt("game-modes.shuffle.interval", 60);
    }

    public String getDefaultCageMaterial() {
        return this.config.getString("arena.default-cage-material", "GLASS");
    }

    public int getCageSize() {
        return this.config.getInt("arena.cage-size", 3);
    }

    public int getMaxCages() {
        return this.config.getInt("arena.max-cages", 20);
    }

    public Location getLobbyLocation() {
        String worldName = this.config.getString("lobby.world", "world");
        World world = this.plugin.getServer().getWorld(worldName);
        if (world == null) {
            world = (World)this.plugin.getServer().getWorlds().get(0);
        }
        double x = this.config.getDouble("lobby.x", 0.5);
        double y = this.config.getDouble("lobby.y", 100.0);
        double z = this.config.getDouble("lobby.z", 0.5);
        float yaw = (float)this.config.getDouble("lobby.yaw", 0.0);
        float pitch = (float)this.config.getDouble("lobby.pitch", 0.0);
        return new Location(world, x, y, z, yaw, pitch);
    }

    public void setLobbyLocation(Location location) {
        this.config.set("lobby.world", (Object)location.getWorld().getName());
        this.config.set("lobby.x", (Object)location.getX());
        this.config.set("lobby.y", (Object)location.getY());
        this.config.set("lobby.z", (Object)location.getZ());
        this.config.set("lobby.yaw", (Object)Float.valueOf(location.getYaw()));
        this.config.set("lobby.pitch", (Object)Float.valueOf(location.getPitch()));
        this.saveConfig();
    }

    public boolean areCosmeticsEnabled() {
        return this.config.getBoolean("cosmetics.enabled", true);
    }

    public boolean isWinFireworksEnabled() {
        return this.config.getBoolean("cosmetics.win-fireworks", true);
    }

    public int getFireworkCount() {
        return this.config.getInt("cosmetics.firework-count", 5);
    }

    public boolean isArenaChatEnabled() {
        return this.config.getBoolean("chat.arena-chat-enabled", true);
    }

    public List<String> getArenaChatStates() {
        List states = this.config.getStringList("chat.arena-chat-states");
        if (states.isEmpty()) {
            return Arrays.asList("WAITING", "STARTING", "IN_GAME");
        }
        return states;
    }

    public boolean isSpectatorChatIsolated() {
        return this.config.getBoolean("chat.spectator-chat-isolated", true);
    }

    public boolean showArenaNameInChat() {
        return this.config.getBoolean("chat.show-arena-name", true);
    }

    public boolean areSoundsEnabled() {
        return this.config.getBoolean("sounds.enabled", true);
    }

    public boolean isSoundEnabled(String soundKey) {
        return this.areSoundsEnabled() && this.config.getBoolean("sounds." + soundKey + ".enabled", true);
    }

    public String getSoundName(String soundKey) {
        return this.config.getString("sounds." + soundKey + ".sound", "none");
    }

    public float getSoundVolume(String soundKey) {
        return (float)this.config.getDouble("sounds." + soundKey + ".volume", 1.0);
    }

    public float getSoundPitch(String soundKey) {
        return (float)this.config.getDouble("sounds." + soundKey + ".pitch", 1.0);
    }

    public String getScoreboardTitle() {
        return this.config.getString("scoreboard.title", "<gradient:#FFD700:#FFA500><bold>FortunePillars</bold></gradient>");
    }

    public long getScoreboardUpdateInterval() {
        return this.config.getLong("scoreboard.update-interval", 20L);
    }

    public boolean isScoreboardEnabled() {
        return this.config.getBoolean("scoreboard.enabled", true);
    }

    public List<String> getScoreboardLines() {
        List lines = this.config.getStringList("scoreboard.lines");
        if (lines.isEmpty()) {
            return Arrays.asList("", "<gray>Arena: <white>{arena}", "", "<gray>Status: {state}", "<gray>Mode: <yellow>{mode}", " ", "<gray>Time: <white>{time}", "  ", "<gray>Alive: <green>{alive}", "<gray>Spectating: <yellow>{spectators}", "<gray>Total: <white>{total}", "   ", "<gray>Your Kills: <red>{kills}", "    ", "{spectator_text}", "     ", "<gold>play.yourserver.com");
        }
        return lines;
    }

    public List<String> getBlacklistedWorlds() {
        return this.config.getStringList("world-blacklist");
    }

    public boolean isWorldBlacklisted(String worldName) {
        return this.getBlacklistedWorlds().stream().anyMatch(w -> w.equalsIgnoreCase(worldName));
    }

    public boolean isWorldBlacklisted(World world) {
        return world != null && this.isWorldBlacklisted(world.getName());
    }

    public void addBlacklistedWorld(String worldName) {
        ArrayList<String> blacklist = new ArrayList<String>(this.getBlacklistedWorlds());
        if (!blacklist.contains(worldName)) {
            blacklist.add(worldName);
            this.config.set("world-blacklist", blacklist);
            this.saveConfig();
        }
    }

    public void removeBlacklistedWorld(String worldName) {
        ArrayList<String> blacklist = new ArrayList<String>(this.getBlacklistedWorlds());
        blacklist.removeIf(w -> w.equalsIgnoreCase(worldName));
        this.config.set("world-blacklist", blacklist);
        this.saveConfig();
    }
}

