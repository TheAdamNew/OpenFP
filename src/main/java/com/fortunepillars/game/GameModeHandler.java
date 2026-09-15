/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.WorldBorder
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.TNTPrimed
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package com.fortunepillars.game;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.game.GameModeType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class GameModeHandler {
    private final FortunePillars plugin;
    private final Map<String, BukkitTask> gameModeTasks;
    private final Map<String, List<BukkitTask>> arenaTaskList;

    public GameModeHandler(FortunePillars plugin) {
        this.plugin = plugin;
        this.gameModeTasks = new HashMap<String, BukkitTask>();
        this.arenaTaskList = new HashMap<String, List<BukkitTask>>();
    }

    public void startGameMode(Arena arena, GameModeType mode) {
        String arenaName = arena.getName();
        this.stopGameMode(arena);
        this.plugin.getLogger().info("Starting game mode " + mode.getDisplayName() + " for arena " + arenaName);
        ArrayList<BukkitTask> tasks = new ArrayList<BukkitTask>();
        switch (mode) {
            case SWAPPER: {
                tasks.add(this.startSwapperTask(arena));
                break;
            }
            case SHUFFLE: {
                tasks.add(this.startShuffleTask(arena));
                break;
            }
            case LAVA_RISING: {
                tasks.add(this.startLavaRisingTask(arena));
                break;
            }
            case BORDER_SHRINK: {
                tasks.add(this.startBorderShrinkTask(arena));
                break;
            }
            case TNT_RAIN: {
                tasks.add(this.startTNTRainTask(arena));
                break;
            }
            case SPEED_UHC: {
                this.applySpeedUHC(arena);
                break;
            }
            case BALANCED: {
                this.applyBalancedLoot(arena);
                break;
            }
        }
        tasks.removeIf(Objects::isNull);
        if (!tasks.isEmpty()) {
            this.arenaTaskList.put(arenaName, tasks);
        }
    }

    public void stopGameMode(Arena arena) {
        BukkitTask mainTask;
        String arenaName = arena.getName();
        List<BukkitTask> tasks = this.arenaTaskList.remove(arenaName);
        if (tasks != null) {
            for (BukkitTask task : tasks) {
                if (task == null) continue;
                task.cancel();
            }
        }
        if ((mainTask = this.gameModeTasks.remove(arenaName)) != null) {
            mainTask.cancel();
        }
    }

    private BukkitTask startSwapperTask(Arena arena) {
        return Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            if (!arena.isActive()) {
                return;
            }
            List<Player> alivePlayers = arena.getAlivePlayers();
            if (alivePlayers.size() < 2) {
                return;
            }
            Collections.shuffle(alivePlayers);
            for (int i = 0; i < alivePlayers.size() - 1; i += 2) {
                Player p1 = alivePlayers.get(i);
                Player p2 = alivePlayers.get(i + 1);
                Location loc1 = p1.getLocation().clone();
                Location loc2 = p2.getLocation().clone();
                p1.teleport(loc2);
                p2.teleport(loc1);
                p1.sendMessage(FortunePillars.parseWithPrefix("<yellow>\ud83d\udd04 You swapped positions with <white>" + p2.getName() + "</white>!"));
                p2.sendMessage(FortunePillars.parseWithPrefix("<yellow>\ud83d\udd04 You swapped positions with <white>" + p1.getName() + "</white>!"));
            }
            arena.broadcastMessage(FortunePillars.parseWithPrefix("<gold><bold>SWAP!</bold></gold> <yellow>All players have been swapped!"));
        }, 600L, 600L);
    }

    private BukkitTask startShuffleTask(Arena arena) {
        return Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            if (!arena.isActive()) {
                return;
            }
            List<Player> alivePlayers = arena.getAlivePlayers();
            if (alivePlayers.size() < 2) {
                return;
            }
            ArrayList<ItemStack[]> inventories = new ArrayList<ItemStack[]>();
            ArrayList<ItemStack[]> armors = new ArrayList<ItemStack[]>();
            for (Player player : alivePlayers) {
                inventories.add((ItemStack[])player.getInventory().getContents().clone());
                armors.add((ItemStack[])player.getInventory().getArmorContents().clone());
            }
            Collections.shuffle(inventories);
            Collections.shuffle(armors);
            for (int i = 0; i < alivePlayers.size(); ++i) {
                Player player;
                player = alivePlayers.get(i);
                player.getInventory().setContents((ItemStack[])inventories.get(i));
                player.getInventory().setArmorContents((ItemStack[])armors.get(i));
                player.updateInventory();
            }
            arena.broadcastMessage(FortunePillars.parseWithPrefix("<light_purple><bold>SHUFFLE!</bold></light_purple> <gray>Inventories have been randomized!"));
        }, 1200L, 1200L);
    }

    private BukkitTask startLavaRisingTask(Arena arena) {
        int[] currentY = new int[]{arena.getCorner1() != null ? Math.min(arena.getCorner1().getBlockY(), arena.getCorner2().getBlockY()) : 0};
        return Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            if (!arena.isActive()) {
                return;
            }
            if (arena.getCorner1() == null || arena.getCorner2() == null) {
                return;
            }
            Location min = this.getMinLocation(arena.getCorner1(), arena.getCorner2());
            Location max = this.getMaxLocation(arena.getCorner1(), arena.getCorner2());
            int maxY = (int)max.getY() - 5;
            if (currentY[0] >= maxY) {
                return;
            }
            int x = (int)min.getX();
            while ((double)x <= max.getX()) {
                int z = (int)min.getZ();
                while ((double)z <= max.getZ()) {
                    Location loc = new Location(min.getWorld(), (double)x, (double)currentY[0], (double)z);
                    if (loc.getBlock().getType() == Material.AIR || loc.getBlock().getType() == Material.CAVE_AIR) {
                        loc.getBlock().setType(Material.LAVA);
                    }
                    ++z;
                }
                ++x;
            }
            currentY[0] = currentY[0] + 1;
            arena.broadcastMessage(FortunePillars.parseWithPrefix("<red>\ud83d\udd25 The lava is rising! Current height: " + currentY[0]));
        }, 600L, 300L);
    }

    private BukkitTask startBorderShrinkTask(Arena arena) {
        if (arena.getCenter() == null) {
            return null;
        }
        WorldBorder border = arena.getCenter().getWorld().getWorldBorder();
        border.setCenter(arena.getCenter());
        border.setSize(200.0);
        return Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            if (!arena.isActive()) {
                return;
            }
            double currentSize = border.getSize();
            if (currentSize <= 20.0) {
                return;
            }
            double newSize = currentSize - 10.0;
            border.setSize(newSize, 5L);
            arena.broadcastMessage(FortunePillars.parseWithPrefix("<red>\u26a0 Border shrinking! New size: " + (int)newSize + " blocks"));
        }, 1200L, 600L);
    }

    private BukkitTask startTNTRainTask(Arena arena) {
        return Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            if (!arena.isActive()) {
                return;
            }
            List<Player> players = arena.getAlivePlayers();
            if (players.isEmpty()) {
                return;
            }
            Random random = new Random();
            int tntCount = Math.min(players.size(), 3);
            for (int i = 0; i < tntCount; ++i) {
                Player target = players.get(random.nextInt(players.size()));
                Location spawnLoc = target.getLocation().clone().add((double)(random.nextInt(10) - 5), (double)(15 + random.nextInt(5)), (double)(random.nextInt(10) - 5));
                TNTPrimed tnt = (TNTPrimed)target.getWorld().spawn(spawnLoc, TNTPrimed.class);
                tnt.setFuseTicks(60);
            }
            arena.broadcastMessage(FortunePillars.parseWithPrefix("<red><bold>\ud83d\udca3 TNT INCOMING!</bold></red>"));
        }, 900L, 400L);
    }

    private void applySpeedUHC(Arena arena) {
        BukkitTask task = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            if (!arena.isActive()) {
                return;
            }
            for (Player player : arena.getAlivePlayers()) {
                player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.GOLDEN_APPLE, 1)});
                player.sendMessage(FortunePillars.parseWithPrefix("<gold>You received a Golden Apple!"));
            }
        }, 2400L, 2400L);
        this.arenaTaskList.computeIfAbsent(arena.getName(), k -> new ArrayList()).add(task);
    }

    private void applyBalancedLoot(Arena arena) {
        this.plugin.getLogger().info("Balanced loot mode activated for " + arena.getName());
    }

    private Location getMinLocation(Location loc1, Location loc2) {
        return new Location(loc1.getWorld(), Math.min(loc1.getX(), loc2.getX()), Math.min(loc1.getY(), loc2.getY()), Math.min(loc1.getZ(), loc2.getZ()));
    }

    private Location getMaxLocation(Location loc1, Location loc2) {
        return new Location(loc1.getWorld(), Math.max(loc1.getX(), loc2.getX()), Math.max(loc1.getY(), loc2.getY()), Math.max(loc1.getZ(), loc2.getZ()));
    }

    public void shutdown() {
        for (List<BukkitTask> tasks : this.arenaTaskList.values()) {
            for (BukkitTask task : tasks) {
                if (task == null) continue;
                task.cancel();
            }
        }
        this.arenaTaskList.clear();
        for (BukkitTask task : this.gameModeTasks.values()) {
            if (task == null) continue;
            task.cancel();
        }
        this.gameModeTasks.clear();
    }
}

