/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.World
 *  org.bukkit.block.Block
 *  org.bukkit.block.BlockState
 *  org.bukkit.block.CommandBlock
 *  org.bukkit.block.Container
 *  org.bukkit.block.CreatureSpawner
 *  org.bukkit.block.data.BlockData
 *  org.bukkit.entity.EntityType
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.util.io.BukkitObjectInputStream
 *  org.bukkit.util.io.BukkitObjectOutputStream
 */
package com.fortunepillars.schematic;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.schematic.ArenaSchematic;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.Container;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class SchematicManager {
    private final FortunePillars plugin;
    private final File schematicsFolder;
    private final Map<String, ArenaSchematic> loadedSchematics;
    private static final int BLOCKS_PER_TICK = 2500;

    public SchematicManager(FortunePillars plugin) {
        this.plugin = plugin;
        this.schematicsFolder = new File(plugin.getDataFolder(), "schematics");
        this.loadedSchematics = new ConcurrentHashMap<String, ArenaSchematic>();
        if (!this.schematicsFolder.exists()) {
            this.schematicsFolder.mkdirs();
        }
    }

    public void saveArena(Arena arena, Consumer<Boolean> callback) {
        Location pos1 = arena.getPos1();
        Location pos2 = arena.getPos2();
        if (pos1 == null || pos2 == null) {
            this.plugin.getLogger().severe("Cannot save schematic for " + arena.getName() + " - positions are null!");
            callback.accept(false);
            return;
        }
        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
            try {
                ArenaSchematic schematic = this.captureBlocks(pos1, pos2);
                Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
                    try {
                        this.saveSchematicToFile(arena.getName(), schematic);
                        this.loadedSchematics.put(arena.getName(), schematic);
                        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> callback.accept(true));
                    }
                    catch (Exception e) {
                        this.plugin.getLogger().severe("Failed to save schematic file: " + e.getMessage());
                        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> callback.accept(false));
                    }
                });
            }
            catch (Exception e) {
                this.plugin.getLogger().severe("Failed to capture blocks: " + e.getMessage());
                callback.accept(false);
            }
        });
    }

    private ArenaSchematic captureBlocks(Location pos1, Location pos2) {
        World world = pos1.getWorld();
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        int sizeX = maxX - minX + 1;
        int sizeY = maxY - minY + 1;
        int sizeZ = maxZ - minZ + 1;
        ArenaSchematic schematic = new ArenaSchematic(world.getName(), minX, minY, minZ, sizeX, sizeY, sizeZ);
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    Block block = world.getBlockAt(x, y, z);
                    int relX = x - minX;
                    int relY = y - minY;
                    int relZ = z - minZ;
                    BlockData blockData = block.getBlockData();
                    schematic.setBlock(relX, relY, relZ, blockData.getAsString());
                    BlockState state = block.getState();
                    byte[] tileData = this.captureTileEntity(state);
                    if (tileData == null) continue;
                    schematic.setTileEntity(relX, relY, relZ, tileData);
                }
            }
        }
        return schematic;
    }

    private byte[] captureTileEntity(BlockState state) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            if (state instanceof Container) {
                Container container = (Container)state;
                dos.writeUTF("CONTAINER");
                ItemStack[] contents = container.getInventory().getContents();
                dos.writeInt(contents.length);
                for (int i = 0; i < contents.length; ++i) {
                    ItemStack item = contents[i];
                    if (item == null || item.getType().isAir()) continue;
                    dos.writeInt(i);
                    dos.writeUTF(this.serializeItemStack(item));
                }
                dos.writeInt(-1);
                dos.close();
                return baos.toByteArray();
            }
            if (state instanceof CreatureSpawner) {
                CreatureSpawner spawner = (CreatureSpawner)state;
                dos.writeUTF("SPAWNER");
                dos.writeUTF(spawner.getSpawnedType() != null ? spawner.getSpawnedType().name() : "PIG");
                dos.writeInt(spawner.getDelay());
                dos.writeInt(spawner.getMinSpawnDelay());
                dos.writeInt(spawner.getMaxSpawnDelay());
                dos.writeInt(spawner.getSpawnCount());
                dos.writeInt(spawner.getMaxNearbyEntities());
                dos.writeInt(spawner.getRequiredPlayerRange());
                dos.writeInt(spawner.getSpawnRange());
                dos.close();
                return baos.toByteArray();
            }
            if (state instanceof CommandBlock) {
                CommandBlock commandBlock = (CommandBlock)state;
                dos.writeUTF("COMMAND_BLOCK");
                dos.writeUTF(commandBlock.getCommand() != null ? commandBlock.getCommand() : "");
                dos.writeUTF(commandBlock.getName());
                dos.close();
                return baos.toByteArray();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    private String serializeItemStack(ItemStack item) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BukkitObjectOutputStream boos = new BukkitObjectOutputStream((OutputStream)baos);
            boos.writeObject((Object)item);
            boos.close();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
        catch (Exception e) {
            return item.getType().name() + ":" + item.getAmount();
        }
    }

    private ItemStack deserializeItemStack(String data) {
        try {
            byte[] bytes = Base64.getDecoder().decode(data);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            BukkitObjectInputStream bois = new BukkitObjectInputStream((InputStream)bais);
            ItemStack item = (ItemStack)bois.readObject();
            bois.close();
            return item;
        }
        catch (Exception e) {
            String[] parts = data.split(":");
            try {
                Material mat = Material.valueOf((String)parts[0]);
                int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
                return new ItemStack(mat, amount);
            }
            catch (Exception ex) {
                return null;
            }
        }
    }

    private void placeBlocksInBatches(World world, List<BlockPlacement> placements, int startIndex, String arenaName, long startTime, ArenaSchematic schematic, Runnable onComplete) {
        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
            int endIndex = Math.min(startIndex + 2500, placements.size());
            for (int i = startIndex; i < endIndex; ++i) {
                BlockPlacement placement = (BlockPlacement)placements.get(i);
                try {
                    Block block = world.getBlockAt(placement.x, placement.y, placement.z);
                    BlockData blockData = Bukkit.createBlockData((String)placement.blockData);
                    block.setBlockData(blockData, false);
                    int relX = placement.x - schematic.getOriginX();
                    int relY = placement.y - schematic.getOriginY();
                    int relZ = placement.z - schematic.getOriginZ();
                    if (!schematic.hasTileEntity(relX, relY, relZ)) continue;
                    byte[] tileData = schematic.getTileEntity(relX, relY, relZ);
                    this.restoreTileEntity(block, tileData);
                    continue;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            if (endIndex < placements.size()) {
                this.placeBlocksInBatches(world, placements, endIndex, arenaName, startTime, schematic, onComplete);
            } else if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    private void restoreTileEntity(Block block, byte[] tileData) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(tileData);
            DataInputStream dis = new DataInputStream(bais);
            String type = dis.readUTF();
            BlockState state = block.getState();
            switch (type) {
                case "CONTAINER": {
                    int slot;
                    if (!(state instanceof Container)) break;
                    Container container = (Container)state;
                    int size = dis.readInt();
                    while ((slot = dis.readInt()) != -1) {
                        String itemData = dis.readUTF();
                        ItemStack item = this.deserializeItemStack(itemData);
                        if (item == null || slot >= container.getInventory().getSize()) continue;
                        container.getInventory().setItem(slot, item);
                    }
                    state.update(true, false);
                    break;
                }
                case "SPAWNER": {
                    if (!(state instanceof CreatureSpawner)) break;
                    CreatureSpawner spawner = (CreatureSpawner)state;
                    String entityType = dis.readUTF();
                    try {
                        spawner.setSpawnedType(EntityType.valueOf((String)entityType));
                    }
                    catch (Exception e) {
                        spawner.setSpawnedType(EntityType.PIG);
                    }
                    spawner.setDelay(dis.readInt());
                    spawner.setMinSpawnDelay(dis.readInt());
                    spawner.setMaxSpawnDelay(dis.readInt());
                    spawner.setSpawnCount(dis.readInt());
                    spawner.setMaxNearbyEntities(dis.readInt());
                    spawner.setRequiredPlayerRange(dis.readInt());
                    spawner.setSpawnRange(dis.readInt());
                    state.update(true, false);
                    break;
                }
                case "COMMAND_BLOCK": {
                    if (!(state instanceof CommandBlock)) break;
                    CommandBlock commandBlock = (CommandBlock)state;
                    commandBlock.setCommand(dis.readUTF());
                    commandBlock.setName(dis.readUTF());
                    state.update(true, false);
                }
            }
            dis.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void restoreArena(Arena arena, Runnable onComplete) {
        String arenaName = arena.getName();
        ArenaSchematic schematic = this.loadedSchematics.get(arenaName);
        if (schematic == null) {
            schematic = this.loadSchematicFromFile(arenaName);
            if (schematic == null) {
                this.plugin.getLogger().severe("No schematic found for arena " + arenaName);
                if (onComplete != null) {
                    Bukkit.getScheduler().runTask((Plugin)this.plugin, onComplete);
                }
                return;
            }
            this.loadedSchematics.put(arenaName, schematic);
        }
        this.pasteSchematic(schematic, arenaName, onComplete);
    }

    private void pasteSchematic(ArenaSchematic schematic, String arenaName, Runnable onComplete) {
        World world = Bukkit.getWorld((String)schematic.getWorldName());
        if (world == null) {
            this.plugin.getLogger().severe("World " + schematic.getWorldName() + " not found!");
            if (onComplete != null) {
                Bukkit.getScheduler().runTask((Plugin)this.plugin, onComplete);
            }
            return;
        }
        ArenaSchematic finalSchematic = schematic;
        this.clearRegionToAir(world, schematic, () -> {
            ArrayList<BlockPlacement> placements = new ArrayList<BlockPlacement>();
            for (int x = 0; x < finalSchematic.getSizeX(); ++x) {
                for (int y = 0; y < finalSchematic.getSizeY(); ++y) {
                    for (int z = 0; z < finalSchematic.getSizeZ(); ++z) {
                        String blockDataString = finalSchematic.getBlock(x, y, z);
                        if (blockDataString == null || blockDataString.equals("minecraft:air")) continue;
                        int worldX = finalSchematic.getOriginX() + x;
                        int worldY = finalSchematic.getOriginY() + y;
                        int worldZ = finalSchematic.getOriginZ() + z;
                        placements.add(new BlockPlacement(worldX, worldY, worldZ, blockDataString));
                    }
                }
            }
            long startTime = System.currentTimeMillis();
            this.placeBlocksInBatches(world, placements, 0, arenaName, startTime, finalSchematic, onComplete);
        });
    }

    public void saveSchematicSync(String arenaName, ArenaSchematic schematic) {
        try {
            File file = new File(this.schematicsFolder, arenaName + ".fpschem");
            try (ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(file)));){
                oos.writeObject(schematic);
            }
            this.loadedSchematics.put(arenaName, schematic);
        }
        catch (IOException e) {
            this.plugin.getLogger().severe("Failed to save schematic file: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void saveSchematicToFile(String arenaName, ArenaSchematic schematic) throws IOException {
        File file = new File(this.schematicsFolder, arenaName + ".fpschem");
        try (ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(file)));){
            oos.writeObject(schematic);
        }
    }

    private ArenaSchematic loadSchematicFromFile(String arenaName) {
        ArenaSchematic arenaSchematic;
        File file = new File(this.schematicsFolder, arenaName + ".fpschem");
        if (!file.exists()) {
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(file)))) {
            arenaSchematic = (ArenaSchematic) ois.readObject();
            return arenaSchematic;
        } catch (Exception e) {
            this.plugin.getLogger().severe("Failed to load schematic for arena " + arenaName + ": " + e.getMessage());
            return null;
        }
    }

    private void clearRegionToAir(World world, ArenaSchematic schematic, Runnable onComplete) {
        int minX = schematic.getOriginX();
        int minY = schematic.getOriginY();
        int minZ = schematic.getOriginZ();
        int maxX = minX + schematic.getSizeX() - 1;
        int maxY = minY + schematic.getSizeY() - 1;
        int maxZ = minZ + schematic.getSizeZ() - 1;
        ArrayList<BlockPlacement> airPlacements = new ArrayList<BlockPlacement>();
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    airPlacements.add(new BlockPlacement(x, y, z, "minecraft:air"));
                }
            }
        }
        this.clearBlocksInBatches(world, airPlacements, 0, onComplete);
    }

    private void clearBlocksInBatches(World world, List<BlockPlacement> placements, int startIndex, Runnable onComplete) {
        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
            int endIndex = Math.min(startIndex + 2500, placements.size());
            for (int i = startIndex; i < endIndex; ++i) {
                BlockPlacement placement = (BlockPlacement)placements.get(i);
                Block block = world.getBlockAt(placement.x, placement.y, placement.z);
                block.setType(Material.AIR, false);
            }
            if (endIndex < placements.size()) {
                this.clearBlocksInBatches(world, placements, endIndex, onComplete);
            } else if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public void deleteSchematic(String arenaName) {
        this.loadedSchematics.remove(arenaName);
        File file = new File(this.schematicsFolder, arenaName + ".fpschem");
        if (file.exists()) {
            file.delete();
        }
    }

    public boolean hasSchematic(String arenaName) {
        if (this.loadedSchematics.containsKey(arenaName)) {
            return true;
        }
        File file = new File(this.schematicsFolder, arenaName + ".fpschem");
        return file.exists();
    }

    public void preloadSchematics() {
        File[] files = this.schematicsFolder.listFiles((dir, name) -> name.endsWith(".fpschem"));
        if (files == null || files.length == 0) {
            return;
        }
        int loaded = 0;
        for (File file : files) {
            String arenaName = file.getName().replace(".fpschem", "");
            ArenaSchematic schematic = this.loadSchematicFromFile(arenaName);
            if (schematic == null) continue;
            this.loadedSchematics.put(arenaName, schematic);
            ++loaded;
        }
        if (loaded > 0) {
            this.plugin.getLogger().info("Preloaded " + loaded + " schematic(s)");
        }
    }

    public String getSchematicInfo(String arenaName) {
        ArenaSchematic schematic = this.loadedSchematics.get(arenaName);
        if (schematic == null) {
            schematic = this.loadSchematicFromFile(arenaName);
        }
        if (schematic == null) {
            return "No schematic found";
        }
        return String.format("Blocks: %d | TileEntities: %d | Size: %dx%dx%d", schematic.getTotalBlocks(), schematic.getTileEntityCount(), schematic.getSizeX(), schematic.getSizeY(), schematic.getSizeZ());
    }

    private record BlockPlacement(int x, int y, int z, String blockData) {
    }
}

