/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.block.Block
 *  org.bukkit.block.data.BlockData
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Item
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package com.fortunepillars.arena;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.arena.GameState;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.invoke.CallSite;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class ArenaResetManager {
    private final FortunePillars plugin;
    private final Map<String, Map<Location, BlockData>> arenaSnapshots;
    private final Map<String, BukkitTask> resetTasks;

    public ArenaResetManager(FortunePillars plugin) {
        this.plugin = plugin;
        this.arenaSnapshots = new ConcurrentHashMap<String, Map<Location, BlockData>>();
        this.resetTasks = new ConcurrentHashMap<String, BukkitTask>();
    }

    public void saveArenaSnapshot(Arena arena) {
        if (arena == null || arena.getCorner1() == null || arena.getCorner2() == null) {
            this.plugin.getLogger().warning("Cannot save snapshot - arena bounds not set for " + arena.getName());
            return;
        }
        this.plugin.getLogger().info("Saving arena snapshot for " + arena.getName() + "...");
        HashMap<Location, BlockData> snapshot = new HashMap<Location, BlockData>();
        Location min = this.getMinLocation(arena.getCorner1(), arena.getCorner2());
        Location max = this.getMaxLocation(arena.getCorner1(), arena.getCorner2());
        World world = min.getWorld();
        if (world == null) {
            return;
        }
        int blockCount = 0;
        for (int x = min.getBlockX(); x <= max.getBlockX(); ++x) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); ++y) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); ++z) {
                    Location loc = new Location(world, (double)x, (double)y, (double)z);
                    Block block = loc.getBlock();
                    snapshot.put(loc, block.getBlockData().clone());
                    ++blockCount;
                }
            }
        }
        this.arenaSnapshots.put(arena.getName(), snapshot);
        this.plugin.getLogger().info("Saved " + blockCount + " blocks for arena " + arena.getName());
        this.saveSnapshotToFile(arena.getName(), snapshot);
    }

    public void resetArena(Arena arena) {
        if (arena == null) {
            return;
        }
        String arenaName = arena.getName();
        BukkitTask existingTask = this.resetTasks.remove(arenaName);
        if (existingTask != null) {
            existingTask.cancel();
        }
        arena.setState(GameState.RESETTING);
        this.plugin.getLogger().info("Starting arena reset for " + arenaName);
        for (Player player : new ArrayList<Player>(arena.getOnlinePlayers())) {
            this.plugin.getArenaManager().setPlayerArena(player, null);
            arena.removePlayer(player, false);
            Location lobby = this.plugin.getConfigManager().getLobbyLocation();
            if (lobby == null) continue;
            player.teleport(lobby);
        }
        this.clearDroppedItems(arena);
        Map<Location, BlockData> snapshot = this.arenaSnapshots.get(arenaName);
        if (snapshot == null && (snapshot = this.loadSnapshotFromFile(arenaName)) != null) {
            this.arenaSnapshots.put(arenaName, snapshot);
        }
        if (snapshot == null || snapshot.isEmpty()) {
            this.plugin.getLogger().warning("No snapshot found for arena " + arenaName + "! Cannot reset.");
            arena.setState(GameState.WAITING);
            return;
        }
        this.resetBlocksAsync(arena, snapshot);
    }

    private void resetBlocksAsync(Arena arena, Map<Location, BlockData> snapshot) {
        ArrayList<Map.Entry<Location, BlockData>> entries = new ArrayList<Map.Entry<Location, BlockData>>(snapshot.entrySet());
        int batchSize = 1000;
        int totalBlocks = entries.size();
        int[] index = new int[]{0};
        BukkitTask task = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            for (int processed = 0; index[0] < totalBlocks && processed < batchSize; ++processed) {
                Map.Entry entry = (Map.Entry)entries.get(index[0]);
                Location loc = (Location)entry.getKey();
                BlockData data = (BlockData)entry.getValue();
                Block block = loc.getBlock();
                if (!block.getBlockData().equals((Object)data)) {
                    block.setBlockData(data, false);
                }
                index[0] = index[0] + 1;
            }
            if (index[0] >= totalBlocks) {
                BukkitTask t = this.resetTasks.remove(arena.getName());
                if (t != null) {
                    t.cancel();
                }
                this.finishReset(arena);
            }
        }, 1L, 1L);
        this.resetTasks.put(arena.getName(), task);
    }

    private void finishReset(Arena arena) {
        this.clearDroppedItems(arena);
        arena.getVotes().clear();
        arena.getPlayers().clear();
        arena.setState(GameState.WAITING);
        this.plugin.getLogger().info("Arena " + arena.getName() + " has been reset and is ready!");
        Bukkit.broadcast((Component)FortunePillars.parseWithPrefix("<green>Arena <yellow>" + arena.getName() + "</yellow> is now available!"));
    }

    public void clearDroppedItems(Arena arena) {
        if (arena == null || arena.getCorner1() == null || arena.getCorner2() == null) {
            return;
        }
        World world = arena.getCorner1().getWorld();
        if (world == null) {
            return;
        }
        Location min = this.getMinLocation(arena.getCorner1(), arena.getCorner2());
        Location max = this.getMaxLocation(arena.getCorner1(), arena.getCorner2());
        for (Entity entity : world.getEntities()) {
            Location loc;
            if (!(entity instanceof Item) || !this.isInBounds(loc = entity.getLocation(), min, max)) continue;
            entity.remove();
        }
    }

    private boolean isInBounds(Location loc, Location min, Location max) {
        return loc.getX() >= min.getX() && loc.getX() <= max.getX() && loc.getY() >= min.getY() && loc.getY() <= max.getY() && loc.getZ() >= min.getZ() && loc.getZ() <= max.getZ();
    }

    private Location getMinLocation(Location loc1, Location loc2) {
        return new Location(loc1.getWorld(), Math.min(loc1.getX(), loc2.getX()), Math.min(loc1.getY(), loc2.getY()), Math.min(loc1.getZ(), loc2.getZ()));
    }

    private Location getMaxLocation(Location loc1, Location loc2) {
        return new Location(loc1.getWorld(), Math.max(loc1.getX(), loc2.getX()), Math.max(loc1.getY(), loc2.getY()), Math.max(loc1.getZ(), loc2.getZ()));
    }

    private void saveSnapshotToFile(String arenaName, Map<Location, BlockData> snapshot) {
        File folder = new File(this.plugin.getDataFolder(), "snapshots");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(folder, arenaName + ".snapshot");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));){
            HashMap<CallSite, String> serializable = new HashMap<CallSite, String>();
            for (Map.Entry<Location, BlockData> entry : snapshot.entrySet()) {
                Location loc = entry.getKey();
                String key = loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
                serializable.put((CallSite)((Object)key), entry.getValue().getAsString());
            }
            oos.writeObject(serializable);
            this.plugin.getLogger().info("Saved snapshot to file for " + arenaName);
        }
        catch (IOException e) {
            this.plugin.getLogger().warning("Failed to save snapshot file: " + e.getMessage());
        }
    }

    private Map<Location, BlockData> loadSnapshotFromFile(String arenaName) {
        File file = new File(this.plugin.getDataFolder(), "snapshots/" + arenaName + ".snapshot");
        if (!file.exists()) {
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Map<?, ?> serializable = (Map<?, ?>)ois.readObject();
            HashMap<Location, BlockData> snapshot = new HashMap<Location, BlockData>();
            for (Map.Entry<?, ?> entry : serializable.entrySet()) {
                String[] parts = ((String)entry.getKey()).split(",");
                World world = Bukkit.getWorld(parts[0]);
                if (world == null) continue;
                Location loc = new Location(world, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
                BlockData data = Bukkit.createBlockData((String)entry.getValue());
                snapshot.put(loc, data);
            }
            this.plugin.getLogger().info("Loaded snapshot from file for " + arenaName);
            return snapshot;
        } catch (Exception e) {
            this.plugin.getLogger().warning("Failed to load snapshot file: " + e.getMessage());
            return null;
        }
    }

    public boolean hasSnapshot(String arenaName) {
        if (this.arenaSnapshots.containsKey(arenaName)) {
            return true;
        }
        File file = new File(this.plugin.getDataFolder(), "snapshots/" + arenaName + ".snapshot");
        return file.exists();
    }

    public void shutdown() {
        for (BukkitTask task : this.resetTasks.values()) {
            task.cancel();
        }
        this.resetTasks.clear();
    }
}

