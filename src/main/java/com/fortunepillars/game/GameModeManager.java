/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.title.Title
 *  net.kyori.adventure.title.Title$Times
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package com.fortunepillars.game;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.arena.GameState;
import com.fortunepillars.game.GameModeType;
import com.fortunepillars.game.modes.BorderShrinkMode;
import com.fortunepillars.game.modes.LavaRisingMode;
import com.fortunepillars.game.modes.SpeedUHCMode;
import com.fortunepillars.game.modes.TNTRainMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class GameModeManager {
    private final FortunePillars plugin;
    private final Map<String, BukkitTask> swapperTasks;
    private final Map<String, BukkitTask> shuffleTasks;
    private final Map<String, LavaRisingMode> lavaRisingModes;
    private final Map<String, BorderShrinkMode> borderShrinkModes;
    private final Map<String, SpeedUHCMode> speedUHCModes;
    private final Map<String, TNTRainMode> tntRainModes;
    private final Random random;

    public GameModeManager(FortunePillars plugin) {
        this.plugin = plugin;
        this.swapperTasks = new ConcurrentHashMap<String, BukkitTask>();
        this.shuffleTasks = new ConcurrentHashMap<String, BukkitTask>();
        this.lavaRisingModes = new ConcurrentHashMap<String, LavaRisingMode>();
        this.borderShrinkModes = new ConcurrentHashMap<String, BorderShrinkMode>();
        this.speedUHCModes = new ConcurrentHashMap<String, SpeedUHCMode>();
        this.tntRainModes = new ConcurrentHashMap<String, TNTRainMode>();
        this.random = new Random();
    }

    public void startGameModeTasks(Arena arena) {
        if (arena == null) {
            this.plugin.getLogger().warning("Attempted to start game mode tasks for null arena!");
            return;
        }
        this.stopGameModeTasks(arena);
        GameModeType mode = arena.getGameMode();
        String arenaName = arena.getName();
        this.plugin.getLogger().info("Starting game mode tasks for arena '" + arenaName + "' with mode: " + mode.name());
        switch (mode) {
            case SWAPPER: {
                this.startSwapperTask(arena);
                break;
            }
            case SHUFFLE: {
                this.startShuffleTask(arena);
                break;
            }
            case LAVA_RISING: {
                this.startLavaRising(arena);
                break;
            }
            case BORDER_SHRINK: {
                this.startBorderShrink(arena);
                break;
            }
            case SPEED_UHC: {
                this.startSpeedUHC(arena);
                break;
            }
            case TNT_RAIN: {
                this.startTNTRain(arena);
                break;
            }
            case NORMAL: 
            case BALANCED: {
                this.plugin.getLogger().info("Mode " + mode.name() + " does not require special tasks");
            }
        }
    }

    public void stopGameModeTasks(Arena arena) {
        TNTRainMode tntMode;
        SpeedUHCMode uhcMode;
        BorderShrinkMode borderMode;
        LavaRisingMode lavaMode;
        BukkitTask shuffleTask;
        if (arena == null) {
            return;
        }
        String arenaName = arena.getName();
        this.plugin.getLogger().info("Stopping game mode tasks for arena: " + arenaName);
        BukkitTask swapperTask = this.swapperTasks.remove(arenaName);
        if (swapperTask != null) {
            swapperTask.cancel();
            this.plugin.getLogger().info("Stopped swapper task for " + arenaName);
        }
        if ((shuffleTask = this.shuffleTasks.remove(arenaName)) != null) {
            shuffleTask.cancel();
            this.plugin.getLogger().info("Stopped shuffle task for " + arenaName);
        }
        if ((lavaMode = this.lavaRisingModes.remove(arenaName)) != null) {
            lavaMode.stop();
            this.plugin.getLogger().info("Stopped lava rising mode for " + arenaName);
        }
        if ((borderMode = this.borderShrinkModes.remove(arenaName)) != null) {
            borderMode.stop();
            this.plugin.getLogger().info("Stopped border shrink mode for " + arenaName);
        }
        if ((uhcMode = this.speedUHCModes.remove(arenaName)) != null) {
            uhcMode.stop();
            this.plugin.getLogger().info("Stopped speed UHC mode for " + arenaName);
        }
        if ((tntMode = this.tntRainModes.remove(arenaName)) != null) {
            tntMode.stop();
            this.plugin.getLogger().info("Stopped TNT rain mode for " + arenaName);
        }
    }

    public void stopAllTasks() {
        this.plugin.getLogger().info("Stopping all game mode tasks...");
        for (BukkitTask bukkitTask : this.swapperTasks.values()) {
            bukkitTask.cancel();
        }
        this.swapperTasks.clear();
        for (BukkitTask bukkitTask : this.shuffleTasks.values()) {
            bukkitTask.cancel();
        }
        this.shuffleTasks.clear();
        for (LavaRisingMode lavaRisingMode : this.lavaRisingModes.values()) {
            lavaRisingMode.stop();
        }
        this.lavaRisingModes.clear();
        for (BorderShrinkMode borderShrinkMode : this.borderShrinkModes.values()) {
            borderShrinkMode.stop();
        }
        this.borderShrinkModes.clear();
        for (SpeedUHCMode speedUHCMode : this.speedUHCModes.values()) {
            speedUHCMode.stop();
        }
        this.speedUHCModes.clear();
        for (TNTRainMode tNTRainMode : this.tntRainModes.values()) {
            tNTRainMode.stop();
        }
        this.tntRainModes.clear();
        this.plugin.getLogger().info("All game mode tasks stopped");
    }

    private void startSwapperTask(Arena arena) {
        int interval = this.plugin.getConfigManager().getSwapperInterval();
        this.plugin.getLogger().info("Starting swapper task for " + arena.getName() + " with interval: " + interval + "s");
        BukkitTask task = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            if (arena.getState() != GameState.IN_GAME) {
                this.plugin.getLogger().info("Arena " + arena.getName() + " is no longer in game, stopping swapper");
                this.stopGameModeTasks(arena);
                return;
            }
            arena.broadcastMessage(this.plugin.getMessages().getWithPrefix("swapper-warning"));
            this.playWarningSound(arena);
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.executeSwap(arena), 100L);
        }, (long)interval * 20L, (long)interval * 20L);
        this.swapperTasks.put(arena.getName(), task);
    }

    private void executeSwap(Arena arena) {
        List<Player> alivePlayers = arena.getAlivePlayers();
        if (alivePlayers.size() < 2) {
            this.plugin.getLogger().info("Not enough players to swap in " + arena.getName());
            return;
        }
        ArrayList<Player> shuffled = new ArrayList<Player>(alivePlayers);
        Collections.shuffle(shuffled, this.random);
        HashMap<Player, Location> originalLocations = new HashMap<Player, Location>();
        for (Player player : shuffled) {
            originalLocations.put(player, player.getLocation().clone());
        }
        for (int i = 0; i < shuffled.size(); ++i) {
            Player current = (Player)shuffled.get(i);
            Player next = (Player)shuffled.get((i + 1) % shuffled.size());
            Location targetLoc = (Location)originalLocations.get(next);
            current.teleport(targetLoc);
        }
        arena.broadcastMessage(this.plugin.getMessages().getWithPrefix("swapper-swap"));
        Title swapTitle = Title.title((Component)FortunePillars.parse("<light_purple><bold>\u2194 SWAPPED! \u2194</bold></light_purple>"), (Component)Component.empty(), (Title.Times)Title.Times.times((Duration)Duration.ZERO, (Duration)Duration.ofMillis(1500L), (Duration)Duration.ofMillis(500L)));
        for (Player player : alivePlayers) {
            player.showTitle(swapTitle);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        }
        this.plugin.getLogger().info("Swapped positions for " + alivePlayers.size() + " players in " + arena.getName());
    }

    private void startShuffleTask(Arena arena) {
        int interval = this.plugin.getConfigManager().getShuffleInterval();
        this.plugin.getLogger().info("Starting shuffle task for " + arena.getName() + " with interval: " + interval + "s");
        BukkitTask task = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            if (arena.getState() != GameState.IN_GAME) {
                this.plugin.getLogger().info("Arena " + arena.getName() + " is no longer in game, stopping shuffle");
                this.stopGameModeTasks(arena);
                return;
            }
            arena.broadcastMessage(this.plugin.getMessages().getWithPrefix("shuffle-warning"));
            this.playWarningSound(arena);
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.executeShuffle(arena), 100L);
        }, (long)interval * 20L, (long)interval * 20L);
        this.shuffleTasks.put(arena.getName(), task);
    }

    private void executeShuffle(Arena arena) {
        List<Player> alivePlayers = arena.getAlivePlayers();
        if (alivePlayers.isEmpty()) {
            this.plugin.getLogger().info("No players to shuffle in " + arena.getName());
            return;
        }
        for (Player player : alivePlayers) {
            this.shufflePlayerInventory(player);
        }
        arena.broadcastMessage(this.plugin.getMessages().getWithPrefix("shuffle-shuffle"));
        Title shuffleTitle = Title.title((Component)FortunePillars.parse("<light_purple><bold>\ud83d\udd00 SHUFFLED! \ud83d\udd00</bold></light_purple>"), (Component)Component.empty(), (Title.Times)Title.Times.times((Duration)Duration.ZERO, (Duration)Duration.ofMillis(1500L), (Duration)Duration.ofMillis(500L)));
        for (Player player : alivePlayers) {
            player.showTitle(shuffleTitle);
            player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_TELEPORT, 1.0f, 1.2f);
        }
        this.plugin.getLogger().info("Shuffled inventories for " + alivePlayers.size() + " players in " + arena.getName());
    }

    private void shufflePlayerInventory(Player player) {
        ItemStack[] mainInventory = new ItemStack[36];
        for (int i = 0; i < 36; ++i) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType().isAir()) continue;
            mainInventory[i] = item.clone();
        }
        ItemStack[] armor = player.getInventory().getArmorContents();
        ItemStack[] armorClone = new ItemStack[4];
        for (int i = 0; i < 4; ++i) {
            if (armor[i] == null || armor[i].getType().isAir()) continue;
            armorClone[i] = armor[i].clone();
        }
        ItemStack offhand = player.getInventory().getItemInOffHand();
        ItemStack offhandClone = offhand != null && !offhand.getType().isAir() ? offhand.clone() : null;
        ArrayList<ItemStack> allItems = new ArrayList<ItemStack>();
        for (ItemStack item : mainInventory) {
            if (item == null) continue;
            allItems.add(item);
        }
        for (ItemStack item : armorClone) {
            if (item == null) continue;
            allItems.add(item);
        }
        if (offhandClone != null) {
            allItems.add(offhandClone);
        }
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().setItemInOffHand(null);
        Collections.shuffle(allItems, this.random);
        for (ItemStack item : allItems) {
            boolean placed = false;
            for (int attempts = 0; !placed && attempts < 50; ++attempts) {
                int slot = this.random.nextInt(41);
                if (slot < 36) {
                    if (player.getInventory().getItem(slot) != null) continue;
                    player.getInventory().setItem(slot, item);
                    placed = true;
                    continue;
                }
                if (slot < 40) {
                    ItemStack[] currentArmor;
                    int armorSlot = slot - 36;
                    if (!this.isValidArmorForSlot(item, armorSlot) || (currentArmor = player.getInventory().getArmorContents())[armorSlot] != null) continue;
                    currentArmor[armorSlot] = item;
                    player.getInventory().setArmorContents(currentArmor);
                    placed = true;
                    continue;
                }
                if (!player.getInventory().getItemInOffHand().getType().isAir()) continue;
                player.getInventory().setItemInOffHand(item);
                placed = true;
            }
            if (placed) continue;
            player.getInventory().addItem(new ItemStack[]{item});
        }
        player.updateInventory();
    }

    private boolean isValidArmorForSlot(ItemStack item, int slot) {
        String typeName = item.getType().name();
        return switch (slot) {
            case 0 -> typeName.contains("BOOTS");
            case 1 -> typeName.contains("LEGGINGS");
            case 2 -> {
                if (typeName.contains("CHESTPLATE") || typeName.equals("ELYTRA")) {
                    yield true;
                }
                yield false;
            }
            case 3 -> {
                if (typeName.contains("HELMET") || typeName.contains("HEAD") || typeName.contains("SKULL") || typeName.equals("CARVED_PUMPKIN")) {
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    private void startLavaRising(Arena arena) {
        this.plugin.getLogger().info("Starting Lava Rising mode for " + arena.getName());
        LavaRisingMode mode = new LavaRisingMode(this.plugin, arena);
        mode.start();
        this.lavaRisingModes.put(arena.getName(), mode);
    }

    private void startBorderShrink(Arena arena) {
        this.plugin.getLogger().info("Starting Border Shrink mode for " + arena.getName());
        BorderShrinkMode mode = new BorderShrinkMode(this.plugin, arena);
        mode.start();
        this.borderShrinkModes.put(arena.getName(), mode);
    }

    private void startSpeedUHC(Arena arena) {
        this.plugin.getLogger().info("Starting Speed UHC mode for " + arena.getName());
        SpeedUHCMode mode = new SpeedUHCMode(this.plugin, arena);
        mode.start();
        this.speedUHCModes.put(arena.getName(), mode);
    }

    private void startTNTRain(Arena arena) {
        this.plugin.getLogger().info("Starting TNT Rain mode for " + arena.getName());
        TNTRainMode mode = new TNTRainMode(this.plugin, arena);
        mode.start();
        this.tntRainModes.put(arena.getName(), mode);
    }

    private void playWarningSound(Arena arena) {
        for (Player player : arena.getAlivePlayers()) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.5f);
        }
    }

    public void swapTwoPlayers(Player player1, Player player2) {
        Location loc1 = player1.getLocation().clone();
        Location loc2 = player2.getLocation().clone();
        player1.teleport(loc2);
        player2.teleport(loc1);
        player1.playSound(player1.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        player2.playSound(player2.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }

    public boolean hasActiveTasks(Arena arena) {
        if (arena == null) {
            return false;
        }
        String arenaName = arena.getName();
        return this.swapperTasks.containsKey(arenaName) || this.shuffleTasks.containsKey(arenaName) || this.lavaRisingModes.containsKey(arenaName) || this.borderShrinkModes.containsKey(arenaName) || this.speedUHCModes.containsKey(arenaName) || this.tntRainModes.containsKey(arenaName);
    }
}

