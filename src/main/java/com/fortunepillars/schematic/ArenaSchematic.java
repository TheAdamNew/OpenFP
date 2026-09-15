/*
 * Decompiled with CFR 0.152.
 */
package com.fortunepillars.schematic;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ArenaSchematic
implements Serializable {
    private static final long serialVersionUID = 2L;
    private final String worldName;
    private final int originX;
    private final int originY;
    private final int originZ;
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final Map<String, String> blocks;
    private final Map<String, byte[]> tileEntities;

    public ArenaSchematic(String worldName, int originX, int originY, int originZ, int sizeX, int sizeY, int sizeZ) {
        this.worldName = worldName;
        this.originX = originX;
        this.originY = originY;
        this.originZ = originZ;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.blocks = new HashMap<String, String>();
        this.tileEntities = new HashMap<String, byte[]>();
    }

    public void setBlock(int x, int y, int z, String blockData) {
        if (blockData == null || blockData.equals("minecraft:air")) {
            return;
        }
        String key = x + "," + y + "," + z;
        this.blocks.put(key, blockData);
    }

    public String getBlock(int x, int y, int z) {
        String key = x + "," + y + "," + z;
        return this.blocks.get(key);
    }

    public void setTileEntity(int x, int y, int z, byte[] nbtData) {
        if (nbtData == null || nbtData.length == 0) {
            return;
        }
        String key = x + "," + y + "," + z;
        this.tileEntities.put(key, nbtData);
    }

    public byte[] getTileEntity(int x, int y, int z) {
        String key = x + "," + y + "," + z;
        return this.tileEntities.get(key);
    }

    public boolean hasTileEntity(int x, int y, int z) {
        String key = x + "," + y + "," + z;
        return this.tileEntities.containsKey(key);
    }

    public Map<String, byte[]> getTileEntities() {
        return this.tileEntities;
    }

    public int getTileEntityCount() {
        return this.tileEntities.size();
    }

    public String getBlockOrAir(int x, int y, int z) {
        String block = this.getBlock(x, y, z);
        return block != null ? block : "minecraft:air";
    }

    public boolean hasBlock(int x, int y, int z) {
        String key = x + "," + y + "," + z;
        return this.blocks.containsKey(key);
    }

    public int getTotalBlocks() {
        return this.blocks.size();
    }

    public Map<String, String> getBlocks() {
        return this.blocks;
    }

    public int getVolume() {
        return this.sizeX * this.sizeY * this.sizeZ;
    }

    public void clear() {
        this.blocks.clear();
        this.tileEntities.clear();
    }

    public String getWorldName() {
        return this.worldName;
    }

    public int getOriginX() {
        return this.originX;
    }

    public int getOriginY() {
        return this.originY;
    }

    public int getOriginZ() {
        return this.originZ;
    }

    public int getSizeX() {
        return this.sizeX;
    }

    public int getSizeY() {
        return this.sizeY;
    }

    public int getSizeZ() {
        return this.sizeZ;
    }

    public String toString() {
        return "ArenaSchematic{world='" + this.worldName + "', origin=(" + this.originX + "," + this.originY + "," + this.originZ + "), size=(" + this.sizeX + "," + this.sizeY + "," + this.sizeZ + "), blocks=" + this.blocks.size() + ", tileEntities=" + this.tileEntities.size() + "}";
    }
}

