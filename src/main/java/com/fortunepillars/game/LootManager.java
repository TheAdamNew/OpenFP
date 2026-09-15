/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.block.Chest
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.EnchantmentStorageMeta
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.inventory.meta.PotionMeta
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 */
package com.fortunepillars.game;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.game.GameModeType;
import com.fortunepillars.game.loot.LootMode;
import com.fortunepillars.utils.SoundUtil;
import com.fortunepillars.utils.Utils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LootManager {
    private final FortunePillars plugin;
    private final Map<String, List<LootItem>> lootTables;
    private final List<LootItem> normalLootMulti;
    private final List<LootItem> balancedLootMulti;
    private final List<LootItem> opLootMulti;
    private final List<LootItem> normalLootSingle;
    private final List<LootItem> balancedLootSingle;
    private final List<LootItem> opLootSingle;
    private final List<LootItem> normalLoot;
    private final List<LootItem> balancedLoot;
    private final List<LootItem> opLoot;
    private final List<Material> spawnEggs;
    private final Random random;
    private final Set<Material> excludedItems;
    private final List<LootItem> configNormalLoot;
    private final List<LootItem> configBalancedLoot;
    private final List<LootItem> configOpLoot;

    public LootManager(FortunePillars plugin) {
        this.plugin = plugin;
        this.lootTables = new HashMap<String, List<LootItem>>();
        this.normalLootMulti = new ArrayList<LootItem>();
        this.balancedLootMulti = new ArrayList<LootItem>();
        this.opLootMulti = new ArrayList<LootItem>();
        this.normalLootSingle = new ArrayList<LootItem>();
        this.balancedLootSingle = new ArrayList<LootItem>();
        this.opLootSingle = new ArrayList<LootItem>();
        this.normalLoot = new ArrayList<LootItem>();
        this.balancedLoot = new ArrayList<LootItem>();
        this.opLoot = new ArrayList<LootItem>();
        this.configNormalLoot = new ArrayList<LootItem>();
        this.configBalancedLoot = new ArrayList<LootItem>();
        this.configOpLoot = new ArrayList<LootItem>();
        this.spawnEggs = new ArrayList<Material>();
        this.excludedItems = new HashSet<Material>();
        this.random = new Random();
        this.initializeExcludedItems();
        this.initializeSpawnEggs();
        this.initializeDefaultLootTables();
        this.loadLootFromConfig();
    }

    private void initializeExcludedItems() {
        this.excludedItems.add(Material.COMMAND_BLOCK);
        this.excludedItems.add(Material.CHAIN_COMMAND_BLOCK);
        this.excludedItems.add(Material.REPEATING_COMMAND_BLOCK);
        this.excludedItems.add(Material.COMMAND_BLOCK_MINECART);
        this.excludedItems.add(Material.BARRIER);
        this.excludedItems.add(Material.LIGHT);
        this.excludedItems.add(Material.STRUCTURE_BLOCK);
        this.excludedItems.add(Material.STRUCTURE_VOID);
        this.excludedItems.add(Material.JIGSAW);
        this.excludedItems.add(Material.DEBUG_STICK);
        this.excludedItems.add(Material.BEDROCK);
        this.excludedItems.add(Material.END_PORTAL_FRAME);
        this.excludedItems.add(Material.SPAWNER);
        this.excludedItems.add(Material.KNOWLEDGE_BOOK);
        this.excludedItems.add(Material.BUNDLE);
        this.excludedItems.add(Material.PETRIFIED_OAK_SLAB);
        this.excludedItems.add(Material.PLAYER_HEAD);
        this.excludedItems.add(Material.PLAYER_WALL_HEAD);
    }

    public void loadLootFromConfig() {
        this.configNormalLoot.clear();
        this.configBalancedLoot.clear();
        this.configOpLoot.clear();
        ConfigurationSection lootSection = this.plugin.getConfigManager().getConfig().getConfigurationSection("loot");
        if (lootSection == null) {
            return;
        }
        this.removeDefaultItems(lootSection);
        ConfigurationSection customSection = lootSection.getConfigurationSection("custom-items");
        if (customSection != null) {
            List<String> normalItems = customSection.getStringList("normal");
            for (String itemStr : normalItems) {
                LootItem item = this.parseConfigLootItem(itemStr);
                if (item == null) continue;
                this.configNormalLoot.add(item);
                this.normalLootMulti.add(item);
                this.normalLootSingle.add(new LootItem(item.material(), 1, 1, item.rarity(), item.weight()));
            }
            List<String> balancedItems = customSection.getStringList("balanced");
            for (String itemStr : balancedItems) {
                LootItem item = this.parseConfigLootItem(itemStr);
                if (item == null) continue;
                this.configBalancedLoot.add(item);
                this.balancedLootMulti.add(item);
                this.balancedLootSingle.add(new LootItem(item.material(), 1, 1, item.rarity(), item.weight()));
            }
            List<String> opItems = customSection.getStringList("op");
            for (String itemStr : opItems) {
                LootItem item = this.parseConfigLootItem(itemStr);
                if (item == null) continue;
                this.configOpLoot.add(item);
                this.opLootMulti.add(item);
                this.opLootSingle.add(new LootItem(item.material(), 1, 1, item.rarity(), item.weight()));
            }
            List<String> allItems = customSection.getStringList("all");
            for (String itemStr : allItems) {
                LootItem item = this.parseConfigLootItem(itemStr);
                if (item == null) continue;
                this.normalLootMulti.add(item);
                this.balancedLootMulti.add(item);
                this.opLootMulti.add(item);
                LootItem singleItem = new LootItem(item.material(), 1, 1, item.rarity(), item.weight());
                this.normalLootSingle.add(singleItem);
                this.balancedLootSingle.add(singleItem);
                this.opLootSingle.add(singleItem);
            }
        }
        this.lootTables.put("normal_multi", new ArrayList<LootItem>(this.normalLootMulti));
        this.lootTables.put("balanced_multi", new ArrayList<LootItem>(this.balancedLootMulti));
        this.lootTables.put("op_multi", new ArrayList<LootItem>(this.opLootMulti));
        this.lootTables.put("normal_single", new ArrayList<LootItem>(this.normalLootSingle));
        this.lootTables.put("balanced_single", new ArrayList<LootItem>(this.balancedLootSingle));
        this.lootTables.put("op_single", new ArrayList<LootItem>(this.opLootSingle));
        int totalCustom = this.configNormalLoot.size() + this.configBalancedLoot.size() + this.configOpLoot.size();
        if (totalCustom > 0) {
            this.plugin.getLogger().info("Loaded " + totalCustom + " custom loot items from config");
        }
    }

    private void removeDefaultItems(ConfigurationSection lootSection) {
        ConfigurationSection removeSection = lootSection.getConfigurationSection("remove-defaults");
        if (removeSection == null) {
            return;
        }
        int removedCount = 0;
        List<String> normalRemove = removeSection.getStringList("normal");
        for (String materialName : normalRemove) {
            try {
                Material material = Material.valueOf(materialName.toUpperCase().trim());
                removedCount += this.removeItemsFromList(this.normalLootMulti, material);
                removedCount += this.removeItemsFromList(this.normalLootSingle, material);
            }
            catch (IllegalArgumentException e) {
                this.plugin.getLogger().warning("Invalid material in remove-defaults.normal: " + materialName);
            }
        }
        List<String> balancedRemove = removeSection.getStringList("balanced");
        for (String materialName : balancedRemove) {
            try {
                Material material = Material.valueOf(materialName.toUpperCase().trim());
                removedCount += this.removeItemsFromList(this.balancedLootMulti, material);
                removedCount += this.removeItemsFromList(this.balancedLootSingle, material);
            }
            catch (IllegalArgumentException e) {
                this.plugin.getLogger().warning("Invalid material in remove-defaults.balanced: " + materialName);
            }
        }
        List<String> opRemove = removeSection.getStringList("op");
        for (String materialName : opRemove) {
            try {
                Material material = Material.valueOf(materialName.toUpperCase().trim());
                removedCount += this.removeItemsFromList(this.opLootMulti, material);
                removedCount += this.removeItemsFromList(this.opLootSingle, material);
            }
            catch (IllegalArgumentException e) {
                this.plugin.getLogger().warning("Invalid material in remove-defaults.op: " + materialName);
            }
        }
        List<String> allRemove = removeSection.getStringList("all");
        for (String materialName : allRemove) {
            try {
                Material material = Material.valueOf(materialName.toUpperCase().trim());
                removedCount += this.removeItemsFromList(this.normalLootMulti, material);
                removedCount += this.removeItemsFromList(this.normalLootSingle, material);
                removedCount += this.removeItemsFromList(this.balancedLootMulti, material);
                removedCount += this.removeItemsFromList(this.balancedLootSingle, material);
                removedCount += this.removeItemsFromList(this.opLootMulti, material);
                removedCount += this.removeItemsFromList(this.opLootSingle, material);
            }
            catch (IllegalArgumentException e) {
                this.plugin.getLogger().warning("Invalid material in remove-defaults.all: " + materialName);
            }
        }
        if (removedCount > 0) {
            this.plugin.getLogger().info("Removed " + removedCount + " default items based on config");
        }
    }

    private int removeItemsFromList(List<LootItem> list, Material material) {
        int removed = 0;
        Iterator<LootItem> iterator = list.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().material() != material) continue;
            iterator.remove();
            ++removed;
        }
        return removed;
    }

    private LootItem parseConfigLootItem(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        try {
            String[] parts = str.split(":");
            Material material = Material.valueOf((String)parts[0].toUpperCase().trim());
            if (this.excludedItems.contains(material)) {
                return null;
            }
            int minAmount = 1;
            int maxAmount = 1;
            int weight = 10;
            if (parts.length == 2) {
                maxAmount = minAmount = Integer.parseInt(parts[1].trim());
            } else if (parts.length == 3) {
                maxAmount = minAmount = Integer.parseInt(parts[1].trim());
                weight = Integer.parseInt(parts[2].trim());
            } else if (parts.length >= 4) {
                minAmount = Integer.parseInt(parts[1].trim());
                maxAmount = Integer.parseInt(parts[2].trim());
                weight = Integer.parseInt(parts[3].trim());
            }
            if (minAmount < 1) {
                minAmount = 1;
            }
            if (maxAmount < minAmount) {
                maxAmount = minAmount;
            }
            if (weight < 1) {
                weight = 1;
            }
            return new LootItem(material, minAmount, maxAmount, LootRarity.COMMON, weight);
        }
        catch (NumberFormatException e) {
            this.plugin.getLogger().warning("Invalid number format in loot item: " + str);
            return null;
        }
        catch (IllegalArgumentException e) {
            this.plugin.getLogger().warning("Invalid material in custom loot: " + str);
            return null;
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Invalid loot item in config: " + str);
            return null;
        }
    }

    public void loadLootTables() {
        List opItems;
        List balancedItems;
        ConfigurationSection lootSection = this.plugin.getConfig().getConfigurationSection("loot");
        if (lootSection == null) {
            return;
        }
        this.lootTables.clear();
        this.normalLoot.clear();
        this.balancedLoot.clear();
        this.opLoot.clear();
        this.normalLootMulti.clear();
        this.balancedLootMulti.clear();
        this.opLootMulti.clear();
        this.normalLootSingle.clear();
        this.balancedLootSingle.clear();
        this.opLootSingle.clear();
        boolean loaded = false;
        ConfigurationSection normalSection = lootSection.getConfigurationSection("normal");
        if (normalSection != null) {
            this.loadLootTableFromConfig("normal", normalSection);
            loaded = true;
        }
        if (!(balancedItems = lootSection.getStringList("balanced.items")).isEmpty()) {
            this.loadSimpleLootTable("balanced", balancedItems);
            loaded = true;
        }
        if (!(opItems = lootSection.getStringList("op.items")).isEmpty()) {
            this.loadSimpleLootTable("op", opItems);
            loaded = true;
        }
        if (!loaded) {
            this.initializeDefaultLootTables();
        } else {
            if (this.lootTables.containsKey("normal")) {
                this.normalLoot.addAll((Collection<LootItem>)this.lootTables.get("normal"));
            }
            if (this.lootTables.containsKey("balanced")) {
                this.balancedLoot.addAll((Collection<LootItem>)this.lootTables.get("balanced"));
            }
            if (this.lootTables.containsKey("op")) {
                this.opLoot.addAll((Collection<LootItem>)this.lootTables.get("op"));
            }
        }
        this.loadLootFromConfig();
    }

    private void loadLootTableFromConfig(String name, ConfigurationSection section) {
        if (section == null) {
            return;
        }
        ArrayList<LootItem> items = new ArrayList<LootItem>();
        int commonChance = section.getInt("chances.common", 60);
        for (Object itemStr : section.getStringList("common")) {
            LootItem item = this.parseLootItem((String)itemStr, LootRarity.COMMON, commonChance);
            if (item == null) continue;
            items.add(item);
        }
        int uncommonChance = section.getInt("chances.uncommon", 30);
        for (String itemStr : section.getStringList("uncommon")) {
            LootItem item = this.parseLootItem(itemStr, LootRarity.UNCOMMON, uncommonChance);
            if (item == null) continue;
            items.add(item);
        }
        int rareChance = section.getInt("chances.rare", 10);
        for (String itemStr : section.getStringList("rare")) {
            LootItem item = this.parseLootItem(itemStr, LootRarity.RARE, rareChance);
            if (item == null) continue;
            items.add(item);
        }
        this.lootTables.put(name, items);
    }

    private void loadSimpleLootTable(String name, List<String> itemStrings) {
        ArrayList<LootItem> items = new ArrayList<LootItem>();
        for (String itemStr : itemStrings) {
            LootItem item = this.parseLootItem(itemStr, LootRarity.COMMON, 100);
            if (item == null) continue;
            items.add(item);
        }
        this.lootTables.put(name, items);
    }

    private LootItem parseLootItem(String str, LootRarity rarity, int chance) {
        try {
            String[] parts = str.split(":");
            Material material = Material.valueOf((String)parts[0].toUpperCase());
            int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
            return new LootItem(material, amount, amount, rarity, chance);
        }
        catch (Exception e) {
            return null;
        }
    }

    private void initializeDefaultLootTables() {
        this.addDualLoot(Material.WOODEN_SWORD, 1, 1, 15, true, false);
        this.addDualLoot(Material.STONE_SWORD, 1, 1, 12, true, false);
        this.addDualLoot(Material.IRON_SWORD, 1, 1, 8, true, false);
        this.addDualLoot(Material.GOLDEN_SWORD, 1, 1, 6, true, false);
        this.addDualLoot(Material.DIAMOND_SWORD, 1, 1, 4, false, true);
        this.addDualLoot(Material.NETHERITE_SWORD, 1, 1, 1, false, true);
        this.addDualLoot(Material.WOODEN_AXE, 1, 1, 10, true, false);
        this.addDualLoot(Material.STONE_AXE, 1, 1, 8, true, false);
        this.addDualLoot(Material.IRON_AXE, 1, 1, 6, true, false);
        this.addDualLoot(Material.DIAMOND_AXE, 1, 1, 3, false, true);
        this.addDualLoot(Material.BOW, 1, 1, 10, true, false);
        this.addDualLoot(Material.CROSSBOW, 1, 1, 6, true, true);
        this.addMultiLoot(Material.ARROW, 8, 16, 15, true, false);
        this.addSingleLoot(Material.ARROW, 1, 15, true, false);
        this.addMultiLoot(Material.SPECTRAL_ARROW, 4, 8, 5, true, false);
        this.addSingleLoot(Material.SPECTRAL_ARROW, 1, 5, true, false);
        this.addDualLoot(Material.TRIDENT, 1, 1, 2, false, true);
        this.addDualLoot(Material.MACE, 1, 1, 1, false, true);
        this.addDualLoot(Material.LEATHER_HELMET, 1, 1, 10, true, false);
        this.addDualLoot(Material.LEATHER_CHESTPLATE, 1, 1, 10, true, false);
        this.addDualLoot(Material.LEATHER_LEGGINGS, 1, 1, 10, true, false);
        this.addDualLoot(Material.LEATHER_BOOTS, 1, 1, 10, true, false);
        this.addDualLoot(Material.CHAINMAIL_HELMET, 1, 1, 6, true, false);
        this.addDualLoot(Material.CHAINMAIL_CHESTPLATE, 1, 1, 6, true, false);
        this.addDualLoot(Material.CHAINMAIL_LEGGINGS, 1, 1, 6, true, false);
        this.addDualLoot(Material.CHAINMAIL_BOOTS, 1, 1, 6, true, false);
        this.addDualLoot(Material.IRON_HELMET, 1, 1, 5, true, false);
        this.addDualLoot(Material.IRON_CHESTPLATE, 1, 1, 5, true, false);
        this.addDualLoot(Material.IRON_LEGGINGS, 1, 1, 5, true, false);
        this.addDualLoot(Material.IRON_BOOTS, 1, 1, 5, true, false);
        this.addDualLoot(Material.GOLDEN_HELMET, 1, 1, 4, true, false);
        this.addDualLoot(Material.GOLDEN_CHESTPLATE, 1, 1, 4, true, false);
        this.addDualLoot(Material.GOLDEN_LEGGINGS, 1, 1, 4, true, false);
        this.addDualLoot(Material.GOLDEN_BOOTS, 1, 1, 4, true, false);
        this.addDualLoot(Material.DIAMOND_HELMET, 1, 1, 2, false, true);
        this.addDualLoot(Material.DIAMOND_CHESTPLATE, 1, 1, 2, false, true);
        this.addDualLoot(Material.DIAMOND_LEGGINGS, 1, 1, 2, false, true);
        this.addDualLoot(Material.DIAMOND_BOOTS, 1, 1, 2, false, true);
        this.addDualLoot(Material.NETHERITE_HELMET, 1, 1, 1, false, true);
        this.addDualLoot(Material.NETHERITE_CHESTPLATE, 1, 1, 1, false, true);
        this.addDualLoot(Material.NETHERITE_LEGGINGS, 1, 1, 1, false, true);
        this.addDualLoot(Material.NETHERITE_BOOTS, 1, 1, 1, false, true);
        this.addDualLoot(Material.SHIELD, 1, 1, 8, true, false);
        this.addDualLoot(Material.ELYTRA, 1, 1, 1, false, true);
        this.addDualLoot(Material.TURTLE_HELMET, 1, 1, 3, false, true);
        this.addDualLoot(Material.COBBLESTONE, 16, 64, 15, true, false);
        this.addDualLoot(Material.OAK_PLANKS, 16, 64, 12, true, false);
        this.addDualLoot(Material.STONE, 16, 32, 10, true, false);
        this.addDualLoot(Material.DIRT, 16, 64, 10, true, false);
        this.addDualLoot(Material.GRASS_BLOCK, 8, 32, 8, true, false);
        this.addDualLoot(Material.SAND, 8, 32, 8, true, false);
        this.addDualLoot(Material.RED_SAND, 8, 32, 6, true, false);
        this.addDualLoot(Material.GRAVEL, 8, 32, 6, true, false);
        this.addDualLoot(Material.CLAY, 8, 16, 5, true, false);
        this.addDualLoot(Material.OAK_LOG, 4, 16, 10, true, false);
        this.addDualLoot(Material.BIRCH_LOG, 4, 16, 8, true, false);
        this.addDualLoot(Material.SPRUCE_LOG, 4, 16, 8, true, false);
        this.addDualLoot(Material.JUNGLE_LOG, 4, 16, 6, true, false);
        this.addDualLoot(Material.ACACIA_LOG, 4, 16, 6, true, false);
        this.addDualLoot(Material.DARK_OAK_LOG, 4, 16, 6, true, false);
        this.addDualLoot(Material.MANGROVE_LOG, 4, 16, 5, true, false);
        this.addDualLoot(Material.CHERRY_LOG, 4, 16, 5, true, false);
        this.addDualLoot(Material.OAK_LEAVES, 8, 32, 8, true, false);
        this.addDualLoot(Material.BIRCH_LEAVES, 8, 32, 6, true, false);
        this.addDualLoot(Material.SPRUCE_LEAVES, 8, 32, 6, true, false);
        this.addDualLoot(Material.JUNGLE_LEAVES, 8, 32, 5, true, false);
        this.addDualLoot(Material.ACACIA_LEAVES, 8, 32, 5, true, false);
        this.addDualLoot(Material.DARK_OAK_LEAVES, 8, 32, 5, true, false);
        this.addDualLoot(Material.MANGROVE_LEAVES, 8, 32, 4, true, false);
        this.addDualLoot(Material.CHERRY_LEAVES, 8, 32, 4, true, false);
        this.addDualLoot(Material.GLASS, 8, 32, 8, true, false);
        this.addDualLoot(Material.WHITE_WOOL, 8, 32, 7, true, false);
        this.addDualLoot(Material.GRAY_WOOL, 8, 32, 5, true, false);
        this.addDualLoot(Material.LIGHT_GRAY_WOOL, 8, 32, 5, true, false);
        this.addDualLoot(Material.STONE_BRICKS, 8, 32, 8, true, false);
        this.addDualLoot(Material.MOSSY_COBBLESTONE, 8, 32, 6, true, false);
        this.addDualLoot(Material.MOSSY_STONE_BRICKS, 4, 16, 5, true, false);
        this.addDualLoot(Material.CRACKED_STONE_BRICKS, 4, 16, 5, true, false);
        this.addDualLoot(Material.CHISELED_STONE_BRICKS, 4, 16, 4, true, false);
        this.addDualLoot(Material.ANDESITE, 8, 32, 6, true, false);
        this.addDualLoot(Material.DIORITE, 8, 32, 6, true, false);
        this.addDualLoot(Material.GRANITE, 8, 32, 6, true, false);
        this.addDualLoot(Material.POLISHED_ANDESITE, 8, 32, 5, true, false);
        this.addDualLoot(Material.POLISHED_DIORITE, 8, 32, 5, true, false);
        this.addDualLoot(Material.POLISHED_GRANITE, 8, 32, 5, true, false);
        this.addDualLoot(Material.TUFF, 8, 32, 6, true, false);
        this.addDualLoot(Material.CALCITE, 8, 32, 5, true, false);
        this.addDualLoot(Material.DEEPSLATE, 8, 32, 6, true, false);
        this.addDualLoot(Material.DRIPSTONE_BLOCK, 4, 16, 5, true, false);
        this.addDualLoot(Material.POINTED_DRIPSTONE, 4, 16, 4, true, false);
        this.addDualLoot(Material.OAK_SLAB, 8, 32, 8, true, false);
        this.addDualLoot(Material.COBBLESTONE_SLAB, 8, 32, 7, true, false);
        this.addDualLoot(Material.STONE_BRICK_SLAB, 8, 32, 6, true, false);
        this.addDualLoot(Material.BRICK_SLAB, 8, 32, 6, true, false);
        this.addDualLoot(Material.BRICK_STAIRS, 4, 16, 5, true, false);
        this.addDualLoot(Material.BRICKS, 8, 32, 7, true, false);
        this.addDualLoot(Material.NETHERRACK, 16, 64, 8, true, false);
        this.addDualLoot(Material.SOUL_SAND, 8, 32, 6, true, false);
        this.addDualLoot(Material.SOUL_SOIL, 8, 32, 6, true, false);
        this.addDualLoot(Material.CRIMSON_NYLIUM, 8, 32, 5, true, false);
        this.addDualLoot(Material.WARPED_NYLIUM, 8, 32, 5, true, false);
        this.addDualLoot(Material.BASALT, 8, 32, 6, true, false);
        this.addDualLoot(Material.SMOOTH_BASALT, 8, 32, 5, true, false);
        this.addDualLoot(Material.BLACKSTONE, 8, 32, 6, true, false);
        this.addDualLoot(Material.POLISHED_BLACKSTONE, 8, 32, 5, true, false);
        this.addDualLoot(Material.NETHER_BRICKS, 8, 32, 6, true, false);
        this.addDualLoot(Material.RED_NETHER_BRICKS, 8, 32, 5, true, false);
        this.addDualLoot(Material.NETHER_BRICK_SLAB, 8, 32, 5, true, false);
        this.addDualLoot(Material.MAGMA_BLOCK, 4, 16, 5, true, false);
        this.addDualLoot(Material.GLOWSTONE, 4, 16, 5, true, false);
        this.addDualLoot(Material.OBSIDIAN, 4, 16, 4, false, true);
        this.addDualLoot(Material.CRYING_OBSIDIAN, 2, 8, 3, false, true);
        this.addDualLoot(Material.IRON_BLOCK, 1, 4, 3, true, true);
        this.addDualLoot(Material.GOLD_BLOCK, 1, 2, 2, false, true);
        this.addDualLoot(Material.DIAMOND_BLOCK, 1, 1, 1, false, true);
        this.addDualLoot(Material.EMERALD_BLOCK, 1, 1, 1, false, true);
        this.addDualLoot(Material.ANCIENT_DEBRIS, 1, 1, 1, false, true);
        this.addDualLoot(Material.NETHERITE_SCRAP, 1, 2, 1, false, true);
        this.addDualLoot(Material.SEA_LANTERN, 4, 16, 5, true, false);
        this.addDualLoot(Material.SHROOMLIGHT, 4, 16, 4, false, true);
        this.addDualLoot(Material.PACKED_ICE, 8, 32, 5, true, false);
        this.addDualLoot(Material.BLUE_ICE, 4, 16, 4, true, false);
        this.addDualLoot(Material.SLIME_BLOCK, 4, 16, 5, true, false);
        this.addDualLoot(Material.HONEY_BLOCK, 4, 16, 4, true, false);
        this.addDualLoot(Material.HAY_BLOCK, 8, 24, 6, true, false);
        this.addDualLoot(Material.COBWEB, 1, 4, 6, true, false);
        this.addDualLoot(Material.LADDER, 4, 16, 8, true, false);
        this.addDualLoot(Material.SCAFFOLDING, 8, 24, 6, true, false);
        this.addDualLoot(Material.TNT, 1, 8, 5, true, true);
        this.addDualLoot(Material.END_CRYSTAL, 1, 4, 2, false, true);
        this.addDualLoot(Material.END_STONE, 16, 64, 4, false, true);
        this.addDualLoot(Material.PURPUR_BLOCK, 8, 32, 3, false, true);
        this.addDualLoot(Material.PURPUR_PILLAR, 8, 32, 3, false, true);
        this.addDualLoot(Material.END_STONE_BRICKS, 8, 32, 3, false, true);
        this.addDualLoot(Material.AMETHYST_BLOCK, 4, 16, 3, false, true);
        this.addDualLoot(Material.BUDDING_AMETHYST, 1, 1, 1, false, true);
        this.addDualLoot(Material.LARGE_AMETHYST_BUD, 2, 8, 2, false, true);
        this.addDualLoot(Material.MEDIUM_AMETHYST_BUD, 2, 8, 2, false, true);
        this.addDualLoot(Material.SMALL_AMETHYST_BUD, 2, 8, 2, false, true);
        this.addDualLoot(Material.BREAD, 2, 8, 12, true, false);
        this.addDualLoot(Material.COOKED_BEEF, 2, 8, 10, true, false);
        this.addDualLoot(Material.COOKED_PORKCHOP, 2, 8, 10, true, false);
        this.addDualLoot(Material.COOKED_CHICKEN, 2, 8, 10, true, false);
        this.addDualLoot(Material.COOKED_MUTTON, 2, 8, 8, true, false);
        this.addDualLoot(Material.COOKED_SALMON, 2, 8, 8, true, false);
        this.addDualLoot(Material.GOLDEN_CARROT, 2, 8, 6, true, false);
        this.addDualLoot(Material.APPLE, 2, 8, 8, true, false);
        this.addDualLoot(Material.GOLDEN_APPLE, 1, 4, 4, true, true);
        this.addDualLoot(Material.ENCHANTED_GOLDEN_APPLE, 1, 1, 1, false, true);
        this.addDualLoot(Material.COOKED_COD, 2, 8, 8, true, false);
        this.addDualLoot(Material.BAKED_POTATO, 2, 8, 8, true, false);
        this.addDualLoot(Material.PUMPKIN_PIE, 2, 6, 6, true, false);
        this.addDualLoot(Material.CAKE, 1, 1, 3, true, false);
        this.addDualLoot(Material.COOKIE, 4, 16, 6, true, false);
        this.addDualLoot(Material.MELON_SLICE, 4, 12, 7, true, false);
        this.addDualLoot(Material.SWEET_BERRIES, 4, 16, 6, true, false);
        this.addDualLoot(Material.GLOW_BERRIES, 4, 16, 5, true, false);
        this.addDualLoot(Material.DRIED_KELP, 8, 24, 6, true, false);
        this.addDualLoot(Material.RABBIT_STEW, 1, 2, 4, true, false);
        this.addDualLoot(Material.MUSHROOM_STEW, 1, 2, 4, true, false);
        this.addDualLoot(Material.BEETROOT_SOUP, 1, 2, 4, true, false);
        this.addDualLoot(Material.SUSPICIOUS_STEW, 1, 2, 3, true, false);
        this.addDualLoot(Material.IRON_PICKAXE, 1, 1, 6, true, false);
        this.addDualLoot(Material.DIAMOND_PICKAXE, 1, 1, 3, false, true);
        this.addDualLoot(Material.NETHERITE_PICKAXE, 1, 1, 1, false, true);
        this.addDualLoot(Material.FLINT_AND_STEEL, 1, 1, 5, true, false);
        this.addDualLoot(Material.FISHING_ROD, 1, 1, 4, true, false);
        this.addDualLoot(Material.SHEARS, 1, 1, 4, true, false);
        this.addDualLoot(Material.IRON_SHOVEL, 1, 1, 5, true, false);
        this.addDualLoot(Material.DIAMOND_SHOVEL, 1, 1, 2, false, true);
        this.addDualLoot(Material.IRON_HOE, 1, 1, 4, true, false);
        this.addDualLoot(Material.DIAMOND_HOE, 1, 1, 2, false, true);
        this.addDualLoot(Material.SPYGLASS, 1, 1, 3, true, false);
        this.addDualLoot(Material.COMPASS, 1, 1, 4, true, false);
        this.addDualLoot(Material.CLOCK, 1, 1, 3, true, false);
        this.addDualLoot(Material.LEAD, 1, 3, 4, true, false);
        this.addDualLoot(Material.NAME_TAG, 1, 1, 3, true, false);
        this.addDualLoot(Material.POTION, 1, 4, 8, true, false);
        this.addDualLoot(Material.SPLASH_POTION, 1, 4, 6, true, true);
        this.addDualLoot(Material.LINGERING_POTION, 1, 4, 4, true, true);
        this.addDualLoot(Material.TIPPED_ARROW, 4, 12, 5, true, true);
        this.addDualLoot(Material.ENDER_PEARL, 2, 8, 8, true, true);
        this.addDualLoot(Material.ENDER_EYE, 1, 4, 4, true, true);
        this.addDualLoot(Material.EXPERIENCE_BOTTLE, 2, 8, 6, true, false);
        this.addDualLoot(Material.FIREWORK_ROCKET, 4, 16, 5, true, false);
        this.addDualLoot(Material.TOTEM_OF_UNDYING, 1, 1, 1, false, true);
        this.addDualLoot(Material.WATER_BUCKET, 1, 1, 6, true, false);
        this.addDualLoot(Material.LAVA_BUCKET, 1, 1, 3, true, true);
        this.addDualLoot(Material.MILK_BUCKET, 1, 1, 5, true, false);
        this.addDualLoot(Material.POWDER_SNOW_BUCKET, 1, 1, 3, true, false);
        this.addDualLoot(Material.SNOWBALL, 4, 16, 8, true, false);
        this.addDualLoot(Material.EGG, 4, 16, 6, true, false);
        this.addDualLoot(Material.ENCHANTED_BOOK, 1, 1, 4, true, true);
        this.addDualLoot(Material.BOOK, 1, 4, 6, true, false);
        this.addDualLoot(Material.WIND_CHARGE, 4, 16, 4, true, true);
        this.addDualLoot(Material.FIRE_CHARGE, 4, 12, 5, true, false);
        this.addDualLoot(Material.GOAT_HORN, 1, 1, 2, true, false);
        this.addDualLoot(Material.BREWING_STAND, 1, 1, 3, true, false);
        this.addDualLoot(Material.BLAZE_POWDER, 4, 16, 5, true, false);
        this.addDualLoot(Material.NETHER_WART, 4, 16, 5, true, false);
        this.addDualLoot(Material.GHAST_TEAR, 1, 4, 3, true, true);
        this.addDualLoot(Material.PHANTOM_MEMBRANE, 4, 8, 4, true, false);
        this.addDualLoot(Material.GLASS_BOTTLE, 4, 12, 6, true, false);
        this.addDualLoot(Material.SPIDER_EYE, 2, 8, 5, true, false);
        this.addDualLoot(Material.FERMENTED_SPIDER_EYE, 1, 4, 4, true, false);
        this.addDualLoot(Material.MAGMA_CREAM, 2, 8, 4, true, false);
        this.addDualLoot(Material.GLISTERING_MELON_SLICE, 2, 8, 4, true, false);
        this.addDualLoot(Material.RABBIT_FOOT, 1, 2, 3, true, false);
        this.addDualLoot(Material.DRAGON_BREATH, 1, 2, 1, false, true);
        this.addDualLoot(Material.BLAZE_ROD, 2, 6, 4, true, true);
        this.addDualLoot(Material.PISTON, 2, 4, 5, true, false);
        this.addDualLoot(Material.STICKY_PISTON, 2, 4, 4, true, false);
        this.addDualLoot(Material.DISPENSER, 1, 2, 5, true, false);
        this.addDualLoot(Material.DROPPER, 1, 2, 4, true, false);
        this.addDualLoot(Material.OBSERVER, 1, 2, 4, true, false);
        this.addDualLoot(Material.REDSTONE, 8, 16, 6, true, false);
        this.addDualLoot(Material.REDSTONE_TORCH, 4, 8, 5, true, false);
        this.addDualLoot(Material.LEVER, 2, 4, 5, true, false);
        this.addDualLoot(Material.TRIPWIRE_HOOK, 2, 4, 4, true, false);
        this.addDualLoot(Material.STRING, 4, 8, 8, true, false);
        this.addDualLoot(Material.HOPPER, 1, 2, 4, true, false);
        this.addDualLoot(Material.COMPARATOR, 1, 2, 3, true, false);
        this.addDualLoot(Material.REPEATER, 1, 4, 4, true, false);
        this.addDualLoot(Material.DAYLIGHT_DETECTOR, 1, 1, 3, true, false);
        this.addDualLoot(Material.TARGET, 1, 2, 3, true, false);
        this.addDualLoot(Material.SCULK_SENSOR, 1, 2, 2, false, true);
        this.addDualLoot(Material.CALIBRATED_SCULK_SENSOR, 1, 1, 1, false, true);
        this.addDualLoot(Material.ZOMBIE_SPAWN_EGG, 1, 2, 8, true, false);
        this.addDualLoot(Material.SKELETON_SPAWN_EGG, 1, 2, 8, true, false);
        this.addDualLoot(Material.SPIDER_SPAWN_EGG, 1, 2, 7, true, false);
        this.addDualLoot(Material.CREEPER_SPAWN_EGG, 1, 2, 6, true, true);
        this.addDualLoot(Material.CAVE_SPIDER_SPAWN_EGG, 1, 2, 5, true, false);
        this.addDualLoot(Material.SILVERFISH_SPAWN_EGG, 1, 2, 5, true, false);
        this.addDualLoot(Material.SLIME_SPAWN_EGG, 1, 2, 5, true, false);
        this.addDualLoot(Material.DROWNED_SPAWN_EGG, 1, 2, 5, true, false);
        this.addDualLoot(Material.HUSK_SPAWN_EGG, 1, 2, 5, true, false);
        this.addDualLoot(Material.STRAY_SPAWN_EGG, 1, 2, 5, true, false);
        this.addDualLoot(Material.ZOMBIE_VILLAGER_SPAWN_EGG, 1, 2, 4, true, false);
        this.addDualLoot(Material.PHANTOM_SPAWN_EGG, 1, 2, 4, true, false);
        this.addDualLoot(Material.ENDERMAN_SPAWN_EGG, 1, 2, 4, true, true);
        this.addDualLoot(Material.WITCH_SPAWN_EGG, 1, 2, 4, true, true);
        this.addDualLoot(Material.PILLAGER_SPAWN_EGG, 1, 2, 4, true, false);
        this.addDualLoot(Material.VINDICATOR_SPAWN_EGG, 1, 2, 3, true, true);
        this.addDualLoot(Material.EVOKER_SPAWN_EGG, 1, 1, 2, false, true);
        this.addDualLoot(Material.VEX_SPAWN_EGG, 1, 2, 3, true, true);
        this.addDualLoot(Material.GUARDIAN_SPAWN_EGG, 1, 2, 3, true, false);
        this.addDualLoot(Material.ELDER_GUARDIAN_SPAWN_EGG, 1, 1, 1, false, true);
        this.addDualLoot(Material.SHULKER_SPAWN_EGG, 1, 2, 2, false, true);
        this.addDualLoot(Material.BLAZE_SPAWN_EGG, 1, 2, 4, true, true);
        this.addDualLoot(Material.GHAST_SPAWN_EGG, 1, 1, 3, true, true);
        this.addDualLoot(Material.MAGMA_CUBE_SPAWN_EGG, 1, 2, 4, true, false);
        this.addDualLoot(Material.WITHER_SKELETON_SPAWN_EGG, 1, 1, 2, false, true);
        this.addDualLoot(Material.PIGLIN_SPAWN_EGG, 1, 2, 4, true, false);
        this.addDualLoot(Material.PIGLIN_BRUTE_SPAWN_EGG, 1, 1, 2, false, true);
        this.addDualLoot(Material.HOGLIN_SPAWN_EGG, 1, 2, 3, true, true);
        this.addDualLoot(Material.ZOGLIN_SPAWN_EGG, 1, 1, 2, false, true);
        this.addDualLoot(Material.ZOMBIFIED_PIGLIN_SPAWN_EGG, 1, 2, 4, true, false);
        this.addDualLoot(Material.RAVAGER_SPAWN_EGG, 1, 1, 1, false, true);
        this.addDualLoot(Material.WARDEN_SPAWN_EGG, 1, 1, 1, false, true);
        this.addDualLoot(Material.BREEZE_SPAWN_EGG, 1, 1, 2, false, true);
        this.addDualLoot(Material.BOGGED_SPAWN_EGG, 1, 2, 4, true, false);
        this.addDualLoot(Material.WOLF_SPAWN_EGG, 1, 2, 5, true, false);
        this.addDualLoot(Material.IRON_GOLEM_SPAWN_EGG, 1, 1, 2, false, true);
        this.addDualLoot(Material.SNOW_GOLEM_SPAWN_EGG, 1, 2, 3, true, false);
        this.addDualLoot(Material.BEE_SPAWN_EGG, 1, 3, 4, true, false);
        this.addDualLoot(Material.LLAMA_SPAWN_EGG, 1, 2, 4, true, false);
        this.addDualLoot(Material.FOX_SPAWN_EGG, 1, 2, 4, true, false);
        this.addDualLoot(Material.CAT_SPAWN_EGG, 1, 2, 4, true, false);
        this.addDualLoot(Material.HORSE_SPAWN_EGG, 1, 1, 3, true, false);
        this.addDualLoot(Material.DONKEY_SPAWN_EGG, 1, 1, 3, true, false);
        this.addDualLoot(Material.MULE_SPAWN_EGG, 1, 1, 2, true, false);
        this.addDualLoot(Material.SKELETON_HORSE_SPAWN_EGG, 1, 1, 1, false, true);
        this.addDualLoot(Material.ZOMBIE_HORSE_SPAWN_EGG, 1, 1, 1, false, true);
        this.addDualLoot(Material.POLAR_BEAR_SPAWN_EGG, 1, 2, 3, true, false);
        this.addDualLoot(Material.PANDA_SPAWN_EGG, 1, 2, 3, true, false);
        this.addDualLoot(Material.GOAT_SPAWN_EGG, 1, 2, 4, true, false);
        this.addDualLoot(Material.ALLAY_SPAWN_EGG, 1, 1, 2, false, true);
        this.addDualLoot(Material.CAMEL_SPAWN_EGG, 1, 1, 2, false, true);
        this.addDualLoot(Material.SNIFFER_SPAWN_EGG, 1, 1, 1, false, true);
        this.addDualLoot(Material.ARMADILLO_SPAWN_EGG, 1, 2, 3, true, false);
        this.addDualLoot(Material.CHICKEN_SPAWN_EGG, 1, 3, 5, true, false);
        this.addDualLoot(Material.COW_SPAWN_EGG, 1, 2, 5, true, false);
        this.addDualLoot(Material.PIG_SPAWN_EGG, 1, 2, 5, true, false);
        this.addDualLoot(Material.SHEEP_SPAWN_EGG, 1, 2, 5, true, false);
        this.addDualLoot(Material.RABBIT_SPAWN_EGG, 1, 3, 4, true, false);
        this.addDualLoot(Material.MOOSHROOM_SPAWN_EGG, 1, 1, 2, false, true);
        this.addDualLoot(Material.DOLPHIN_SPAWN_EGG, 1, 1, 3, true, false);
        this.addDualLoot(Material.TURTLE_SPAWN_EGG, 1, 2, 3, true, false);
        this.addDualLoot(Material.AXOLOTL_SPAWN_EGG, 1, 2, 3, true, false);
        this.addDualLoot(Material.SQUID_SPAWN_EGG, 1, 2, 4, true, false);
        this.addDualLoot(Material.GLOW_SQUID_SPAWN_EGG, 1, 2, 3, true, false);
        this.addDualLoot(Material.COD_SPAWN_EGG, 1, 3, 4, true, false);
        this.addDualLoot(Material.SALMON_SPAWN_EGG, 1, 3, 4, true, false);
        this.addDualLoot(Material.TROPICAL_FISH_SPAWN_EGG, 1, 3, 4, true, false);
        this.addDualLoot(Material.PUFFERFISH_SPAWN_EGG, 1, 2, 3, true, false);
        this.addDualLoot(Material.FROG_SPAWN_EGG, 1, 2, 3, true, false);
        this.addDualLoot(Material.TADPOLE_SPAWN_EGG, 1, 3, 3, true, false);
        this.addDualLoot(Material.MUSIC_DISC_13, 1, 1, 2, false, true);
        this.addDualLoot(Material.MUSIC_DISC_CAT, 1, 1, 2, false, true);
        this.addDualLoot(Material.MUSIC_DISC_BLOCKS, 1, 1, 2, false, true);
        this.addDualLoot(Material.MUSIC_DISC_CHIRP, 1, 1, 2, false, true);
        this.addDualLoot(Material.MUSIC_DISC_FAR, 1, 1, 2, false, true);
        this.addDualLoot(Material.MUSIC_DISC_MALL, 1, 1, 2, false, true);
        this.addDualLoot(Material.MUSIC_DISC_MELLOHI, 1, 1, 2, false, true);
        this.addDualLoot(Material.MUSIC_DISC_STAL, 1, 1, 2, false, true);
        this.addDualLoot(Material.MUSIC_DISC_STRAD, 1, 1, 2, false, true);
        this.addDualLoot(Material.MUSIC_DISC_WARD, 1, 1, 2, false, true);
        this.addDualLoot(Material.MUSIC_DISC_11, 1, 1, 1, false, true);
        this.addDualLoot(Material.MUSIC_DISC_WAIT, 1, 1, 2, false, true);
        this.addDualLoot(Material.MUSIC_DISC_OTHERSIDE, 1, 1, 1, false, true);
        this.addDualLoot(Material.MUSIC_DISC_PIGSTEP, 1, 1, 1, false, true);
        this.addDualLoot(Material.MUSIC_DISC_5, 1, 1, 1, false, true);
        this.addDualLoot(Material.MUSIC_DISC_RELIC, 1, 1, 1, false, true);
        this.addDualLoot(Material.MUSIC_DISC_PRECIPICE, 1, 1, 1, false, true);
        this.addDualLoot(Material.MUSIC_DISC_CREATOR, 1, 1, 1, false, true);
        this.addDualLoot(Material.MUSIC_DISC_CREATOR_MUSIC_BOX, 1, 1, 1, false, true);
        this.lootTables.put("normal_multi", new ArrayList<LootItem>(this.normalLootMulti));
        this.lootTables.put("balanced_multi", new ArrayList<LootItem>(this.balancedLootMulti));
        this.lootTables.put("op_multi", new ArrayList<LootItem>(this.opLootMulti));
        this.lootTables.put("normal_single", new ArrayList<LootItem>(this.normalLootSingle));
        this.lootTables.put("balanced_single", new ArrayList<LootItem>(this.balancedLootSingle));
        this.lootTables.put("op_single", new ArrayList<LootItem>(this.opLootSingle));
        this.normalLoot.addAll(this.normalLootMulti);
        this.balancedLoot.addAll(this.balancedLootMulti);
        this.opLoot.addAll(this.opLootMulti);
    }

    private void addDualLoot(Material material, int minAmount, int maxAmount, int weight, boolean balanced, boolean op) {
        this.addMultiLoot(material, minAmount, maxAmount, weight, balanced, op);
        this.addSingleLoot(material, minAmount, weight, balanced, op);
    }

    private void addMultiLoot(Material material, int minAmount, int maxAmount, int weight, boolean balanced, boolean op) {
        LootItem item = new LootItem(material, minAmount, maxAmount, LootRarity.COMMON, weight);
        this.normalLootMulti.add(item);
        if (balanced) {
            this.balancedLootMulti.add(item);
        }
        if (op) {
            this.opLootMulti.add(item);
        }
    }

    private void addSingleLoot(Material material, int amount, int weight, boolean balanced, boolean op) {
        LootItem item = new LootItem(material, 1, 1, LootRarity.COMMON, weight);
        this.normalLootSingle.add(item);
        if (balanced) {
            this.balancedLootSingle.add(item);
        }
        if (op) {
            this.opLootSingle.add(item);
        }
    }

    private void initializeSpawnEggs() {
        for (Material material : Material.values()) {
            if (!material.name().endsWith("_SPAWN_EGG")) continue;
            this.spawnEggs.add(material);
        }
    }

    public void giveLootByTier(Player player, LootMode.LootTier tier, LootMode.DropStyle style, int count) {
        if (player == null || !player.isOnline()) {
            return;
        }
        int actualCount = 0;
        for (int i = 0; i < count; ++i) {
            ItemStack item = this.getRandomLootByTierAndStyle(tier, style);
            if (item == null) continue;
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack[]{item});
            for (ItemStack drop : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
            ++actualCount;
        }
        if (actualCount > 0) {
            if (this.plugin.getConfigManager().isChatNotificationEnabled("loot-received")) {
                String tierColor = switch (tier) {
                    default -> throw new MatchException(null, null);
                    case LootMode.LootTier.NORMAL -> "<white>";
                    case LootMode.LootTier.BALANCED -> "<aqua>";
                    case LootMode.LootTier.OP -> "<gold>";
                };
                String styleText = style == LootMode.DropStyle.SINGLE ? "single" : "multi";
                player.sendMessage(FortunePillars.parseWithPrefix(tierColor + "+" + actualCount + " " + tier.name().toLowerCase() + " loot (" + styleText + ")"));
            }
            SoundUtil.playLootReceived(player);
        }
    }

    private ItemStack getRandomLootByTierAndStyle(LootMode.LootTier tier, LootMode.DropStyle style) {
        List<LootItem> lootTable;
        if (style == LootMode.DropStyle.SINGLE) {
            lootTable = switch (tier) {
                default -> throw new MatchException(null, null);
                case LootMode.LootTier.NORMAL -> this.normalLootSingle;
                case LootMode.LootTier.BALANCED -> this.balancedLootSingle;
                case LootMode.LootTier.OP -> this.opLootSingle;
            };
        } else {
            lootTable = switch (tier) {
                default -> throw new MatchException(null, null);
                case LootMode.LootTier.NORMAL -> this.normalLootMulti;
                case LootMode.LootTier.BALANCED -> this.balancedLootMulti;
                case LootMode.LootTier.OP -> this.opLootMulti;
            };
        }
        if (lootTable.isEmpty()) {
            return null;
        }
        int totalWeight = lootTable.stream().mapToInt(LootItem::weight).sum();
        int roll = ThreadLocalRandom.current().nextInt(totalWeight);
        int current = 0;
        for (LootItem loot : lootTable) {
            if (roll >= (current += loot.weight())) continue;
            return this.processItem(this.createItem(loot));
        }
        return this.processItem(this.createItem(lootTable.get(0)));
    }

    public void giveRandomLoot(Player player, int count) {
        this.giveLootByTier(player, LootMode.LootTier.NORMAL, LootMode.DropStyle.MULTI, count);
    }

    public void distributeLoot(Arena arena) {
        List<Player> alivePlayers = arena.getAlivePlayers();
        if (alivePlayers.isEmpty()) {
            return;
        }
        int minItems = this.plugin.getConfigManager().getLootItemsMin();
        int maxItems = this.plugin.getConfigManager().getLootItemsMax();
        for (Player player : alivePlayers) {
            int itemCount = Utils.randomInt(minItems, maxItems);
            this.giveRandomLoot(player, itemCount);
        }
    }

    private ItemStack createItem(LootItem lootItem) {
        int amount = Utils.randomInt(lootItem.minAmount(), lootItem.maxAmount());
        return new ItemStack(lootItem.material(), amount);
    }

    private ItemStack processItem(ItemStack item) {
        Material type = item.getType();
        if (this.isEnchantable(type) && ThreadLocalRandom.current().nextInt(100) < 30) {
            this.addRandomEnchantment(item);
        }
        if (type == Material.POTION || type == Material.SPLASH_POTION || type == Material.LINGERING_POTION) {
            this.addRandomPotionEffect(item);
        }
        if (type == Material.ENCHANTED_BOOK) {
            this.addRandomBookEnchantment(item);
        }
        return item;
    }

    private boolean isEnchantable(Material material) {
        String name = material.name();
        return name.contains("SWORD") || name.contains("AXE") || name.contains("BOW") || name.contains("CROSSBOW") || name.contains("TRIDENT") || name.contains("MACE") || name.contains("HELMET") || name.contains("CHESTPLATE") || name.contains("LEGGINGS") || name.contains("BOOTS") || name.contains("PICKAXE") || name.contains("SHOVEL") || name.contains("HOE") || material == Material.SHIELD || material == Material.FISHING_ROD || material == Material.ELYTRA || material == Material.TURTLE_HELMET;
    }

    private void addRandomEnchantment(ItemStack item) {
        ArrayList<Enchantment> applicable = new ArrayList<Enchantment>();
        for (Enchantment enchant : Enchantment.values()) {
            if (!enchant.canEnchantItem(item)) continue;
            applicable.add(enchant);
        }
        if (applicable.isEmpty()) {
            return;
        }
        Enchantment enchant = (Enchantment)applicable.get(ThreadLocalRandom.current().nextInt(applicable.size()));
        int level = Utils.randomInt(1, Math.min(enchant.getMaxLevel(), 3));
        item.addUnsafeEnchantment(enchant, level);
    }

    private void addRandomPotionEffect(ItemStack item) {
        PotionMeta meta = (PotionMeta)item.getItemMeta();
        if (meta == null) {
            return;
        }
        PotionEffectType[] positiveEffects = new PotionEffectType[]{PotionEffectType.SPEED, PotionEffectType.STRENGTH, PotionEffectType.REGENERATION, PotionEffectType.FIRE_RESISTANCE, PotionEffectType.INVISIBILITY, PotionEffectType.JUMP_BOOST, PotionEffectType.RESISTANCE, PotionEffectType.INSTANT_HEALTH, PotionEffectType.NIGHT_VISION, PotionEffectType.WATER_BREATHING, PotionEffectType.SLOW_FALLING};
        PotionEffectType[] negativeEffects = new PotionEffectType[]{PotionEffectType.SLOWNESS, PotionEffectType.POISON, PotionEffectType.WEAKNESS, PotionEffectType.INSTANT_DAMAGE, PotionEffectType.BLINDNESS, PotionEffectType.WITHER};
        PotionEffectType effectType = ThreadLocalRandom.current().nextInt(100) < 70 ? positiveEffects[ThreadLocalRandom.current().nextInt(positiveEffects.length)] : negativeEffects[ThreadLocalRandom.current().nextInt(negativeEffects.length)];
        int duration = Utils.randomInt(100, 600);
        int amplifier = Utils.randomInt(0, 2);
        if (effectType == PotionEffectType.INSTANT_HEALTH || effectType == PotionEffectType.INSTANT_DAMAGE) {
            duration = 1;
        }
        meta.addCustomEffect(new PotionEffect(effectType, duration, amplifier), true);
        meta.setColor(effectType.getColor());
        item.setItemMeta((ItemMeta)meta);
    }

    private void addRandomBookEnchantment(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        Enchantment[] enchants = Enchantment.values();
        Enchantment enchant = enchants[ThreadLocalRandom.current().nextInt(enchants.length)];
        int level = Utils.randomInt(1, enchant.getMaxLevel());
        if (meta instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta)meta;
            storageMeta.addStoredEnchant(enchant, level, true);
            item.setItemMeta((ItemMeta)storageMeta);
        }
    }

    public void fillChest(Chest chest, GameModeType mode) {
        List<ItemStack> loot = this.generateLoot(mode, 5 + this.random.nextInt(5));
        chest.getInventory().clear();
        for (ItemStack item : loot) {
            int slot = this.random.nextInt(27);
            if (chest.getInventory().getItem(slot) == null) {
                chest.getInventory().setItem(slot, item);
                continue;
            }
            chest.getInventory().addItem(new ItemStack[]{item});
        }
    }

    public List<ItemStack> generateLoot(GameModeType mode, int itemCount) {
        String tableName = switch (mode) {
            case GameModeType.BALANCED -> "balanced";
            case GameModeType.SPEED_UHC -> "speed_uhc";
            default -> "normal";
        };
        List<LootItem> table = this.lootTables.getOrDefault(tableName, this.lootTables.get("normal"));
        if (table == null || table.isEmpty()) {
            table = this.normalLoot;
        }
        if (table.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<ItemStack> result = new ArrayList<ItemStack>();
        for (int i = 0; i < itemCount; ++i) {
            LootItem item = this.selectRandomItem(table);
            if (item == null) continue;
            result.add(this.processItem(this.createItem(item)));
        }
        return result;
    }

    private LootItem selectRandomItem(List<LootItem> table) {
        int totalWeight = table.stream().mapToInt(LootItem::weight).sum();
        int roll = this.random.nextInt(totalWeight);
        int cumulative = 0;
        for (LootItem item : table) {
            if (roll >= (cumulative += item.weight())) continue;
            return item;
        }
        return table.get(this.random.nextInt(table.size()));
    }

    public List<LootItem> getLootTable(GameModeType gameMode) {
        if (gameMode == GameModeType.BALANCED) {
            return this.balancedLoot.isEmpty() ? this.lootTables.getOrDefault("balanced", this.normalLoot) : this.balancedLoot;
        }
        if (gameMode == GameModeType.SPEED_UHC) {
            return this.lootTables.getOrDefault("speed_uhc", this.normalLoot);
        }
        return this.normalLoot;
    }

    public boolean isExcludedItem(Material material) {
        return this.excludedItems.contains(material);
    }

    public void reload() {
        this.normalLootMulti.clear();
        this.balancedLootMulti.clear();
        this.opLootMulti.clear();
        this.normalLootSingle.clear();
        this.balancedLootSingle.clear();
        this.opLootSingle.clear();
        this.normalLoot.clear();
        this.balancedLoot.clear();
        this.opLoot.clear();
        this.lootTables.clear();
        this.initializeDefaultLootTables();
        this.loadLootFromConfig();
    }

    public List<LootItem> getNormalLoot() {
        return new ArrayList<LootItem>(this.normalLoot);
    }

    public List<LootItem> getBalancedLoot() {
        return new ArrayList<LootItem>(this.balancedLoot);
    }

    public List<LootItem> getOpLoot() {
        return new ArrayList<LootItem>(this.opLoot);
    }

    public List<LootItem> getNormalLootMulti() {
        return new ArrayList<LootItem>(this.normalLootMulti);
    }

    public List<LootItem> getBalancedLootMulti() {
        return new ArrayList<LootItem>(this.balancedLootMulti);
    }

    public List<LootItem> getOpLootMulti() {
        return new ArrayList<LootItem>(this.opLootMulti);
    }

    public List<LootItem> getNormalLootSingle() {
        return new ArrayList<LootItem>(this.normalLootSingle);
    }

    public List<LootItem> getBalancedLootSingle() {
        return new ArrayList<LootItem>(this.balancedLootSingle);
    }

    public List<LootItem> getOpLootSingle() {
        return new ArrayList<LootItem>(this.opLootSingle);
    }

    public List<Material> getSpawnEggs() {
        return new ArrayList<Material>(this.spawnEggs);
    }

    public record LootItem(Material material, int minAmount, int maxAmount, LootRarity rarity, int weight) {
        public ItemStack createItem() {
            int amount = Utils.randomInt(this.minAmount, this.maxAmount);
            return new ItemStack(this.material, amount);
        }

        public int getWeight() {
            return this.weight;
        }
    }

    public static enum LootRarity {
        COMMON,
        UNCOMMON,
        RARE,
        LEGENDARY;

    }
}

