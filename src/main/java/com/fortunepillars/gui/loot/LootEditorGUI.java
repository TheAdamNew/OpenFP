/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TextComponent
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.InventoryHolder
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.metadata.FixedMetadataValue
 *  org.bukkit.metadata.MetadataValue
 *  org.bukkit.plugin.Plugin
 */
package com.fortunepillars.gui.loot;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.gui.loot.LootEditorHolder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public class LootEditorGUI {
    private final FortunePillars plugin;
    public static final Map<String, List<Material>> CATEGORIES = new LinkedHashMap<String, List<Material>>();

    public LootEditorGUI(FortunePillars plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        LootEditorHolder holder = new LootEditorHolder(LootEditorHolder.MenuType.MAIN_MENU);
        Inventory inv = Bukkit.createInventory((InventoryHolder)holder, (int)27, (Component)Component.text((String)"\u00a76\u00a7lLoot Editor - Choose Tier"));
        ItemStack filler = this.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", new String[0]);
        for (int i = 0; i < 27; ++i) {
            inv.setItem(i, filler);
        }
        inv.setItem(10, this.createItem(Material.IRON_SWORD, "\u00a7f\u00a7lNormal Tier", "\u00a77Click to edit normal tier loot", "\u00a78Basic items for survival", "", "\u00a7eClick to edit!"));
        inv.setItem(12, this.createItem(Material.DIAMOND_SWORD, "\u00a7b\u00a7lBalanced Tier", "\u00a77Click to edit balanced tier loot", "\u00a78Mid-tier items", "", "\u00a7eClick to edit!"));
        inv.setItem(14, this.createItem(Material.NETHERITE_SWORD, "\u00a76\u00a7lOP Tier", "\u00a77Click to edit OP tier loot", "\u00a78High-tier powerful items", "", "\u00a7eClick to edit!"));
        inv.setItem(16, this.createItem(Material.BOOK, "\u00a7e\u00a7lView Current Loot Tables", "\u00a77See statistics about all loot", "", "\u00a7aNormal: \u00a7f" + this.plugin.getLootManager().getNormalLootMulti().size() + " items", "\u00a7bBalanced: \u00a7f" + this.plugin.getLootManager().getBalancedLootMulti().size() + " items", "\u00a76OP: \u00a7f" + this.plugin.getLootManager().getOpLootMulti().size() + " items"));
        inv.setItem(22, this.createItem(Material.REDSTONE, "\u00a7c\u00a7lReload Loot Tables", "\u00a77Reload loot from config", "", "\u00a7eClick to reload!"));
        player.openInventory(inv);
    }

    public void openCategoryMenu(Player player, String tier) {
        LootEditorHolder holder = new LootEditorHolder(LootEditorHolder.MenuType.CATEGORY_MENU);
        holder.setTier(tier);
        Inventory inv = Bukkit.createInventory((InventoryHolder)holder, (int)54, (Component)Component.text((String)("\u00a76\u00a7lLoot Editor - " + this.capitalize(tier) + " - Categories")));
        ItemStack filler = this.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", new String[0]);
        for (int i = 45; i < 54; ++i) {
            inv.setItem(i, filler);
        }
        int slot = 0;
        for (Map.Entry<String, List<Material>> entry : CATEGORIES.entrySet()) {
            if (slot >= 45) break;
            String category = entry.getKey();
            Material icon = entry.getValue().isEmpty() ? Material.BARRIER : entry.getValue().get(0);
            int itemsInLoot = this.countItemsInLoot(tier, entry.getValue());
            inv.setItem(slot++, this.createItem(icon, "\u00a7e\u00a7l" + category, "\u00a77Click to browse " + category.toLowerCase(), "", "\u00a78Total items: \u00a7f" + entry.getValue().size(), "\u00a78In loot table: \u00a7a" + itemsInLoot, "", "\u00a7eClick to browse!"));
        }
        inv.setItem(49, this.createItem(Material.ARROW, "\u00a7c\u00a7lBack", "\u00a77Return to tier selection"));
        player.openInventory(inv);
    }

    public void openItemBrowser(Player player, String tier, String category, int page) {
        List<Material> items = CATEGORIES.get(category);
        if (items == null || items.isEmpty()) {
            return;
        }
        int itemsPerPage = 45;
        int totalPages = (int)Math.ceil((double)items.size() / (double)itemsPerPage);
        page = Math.max(0, Math.min(page, totalPages - 1));
        LootEditorHolder holder = new LootEditorHolder(LootEditorHolder.MenuType.ITEM_BROWSER);
        holder.setTier(tier);
        holder.setCategory(category);
        holder.setPage(page);
        Inventory inv = Bukkit.createInventory((InventoryHolder)holder, (int)54, (Component)Component.text((String)("\u00a76\u00a7l" + this.capitalize(tier) + " - " + category)));
        Set<Material> currentLoots = this.getCurrentLootMaterials(tier);
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, items.size());
        for (int i = startIndex; i < endIndex; ++i) {
            Material material = items.get(i);
            int slot = i - startIndex;
            boolean isInLoot = currentLoots.contains(material);
            LootItemConfig config = isInLoot ? this.getCurrentItemConfig(tier, material) : null;
            ArrayList<Object> lore = new ArrayList<Object>();
            if (isInLoot) {
                lore.add("\u00a7a\u00a7l\u2713 IN LOOT TABLE");
                lore.add("");
                if (config != null) {
                    lore.add("\u00a77Amount: \u00a7f" + config.minAmount + "-" + config.maxAmount);
                    lore.add("\u00a77Weight: \u00a7f" + config.weight);
                }
                lore.add("");
                lore.add("\u00a77Left-click: \u00a7eEdit properties");
                lore.add("\u00a77Right-click: \u00a7cRemove from loot");
            } else {
                lore.add("\u00a7c\u00a7l\u2717 NOT IN LOOT TABLE");
                lore.add("");
                lore.add("\u00a77Click to \u00a7aAdd to loot");
            }
            inv.setItem(slot, this.createItem(material, (isInLoot ? "\u00a7a" : "\u00a77") + this.formatMaterialName(material), lore.toArray(new String[0])));
        }
        ItemStack filler = this.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", new String[0]);
        for (int i = 45; i < 54; ++i) {
            inv.setItem(i, filler);
        }
        if (page > 0) {
            inv.setItem(45, this.createItem(Material.ARROW, "\u00a7e\u00a7l\u00ab Previous Page", "\u00a77Go to page " + page));
        }
        if (page < totalPages - 1) {
            inv.setItem(53, this.createItem(Material.ARROW, "\u00a7e\u00a7lNext Page \u00bb", "\u00a77Go to page " + (page + 2)));
        }
        inv.setItem(49, this.createItem(Material.BARRIER, "\u00a7c\u00a7lBack to Categories", "\u00a77Return to category selection"));
        inv.setItem(50, this.createItem(Material.PAPER, "\u00a7e\u00a7lInfo", "\u00a77Tier: \u00a7f" + this.capitalize(tier), "\u00a77Category: \u00a7f" + category, "\u00a77Items: \u00a7f" + items.size(), "\u00a77Page: \u00a7f" + (page + 1) + "/" + totalPages));
        player.openInventory(inv);
    }

    public void openItemEditor(Player player, String tier, Material material) {
        LootItemConfig config = this.getCurrentItemConfig(tier, material);
        this.openItemEditor(player, tier, material, config);
    }

    public void openItemEditor(Player player, String tier, Material material, LootItemConfig config) {
        LootEditorHolder holder = new LootEditorHolder(LootEditorHolder.MenuType.ITEM_EDITOR);
        holder.setTier(tier);
        holder.setEditingMaterial(material);
        holder.setConfig(config);
        if (player.hasMetadata("loot_editor_category")) {
            holder.setCategory(((MetadataValue)player.getMetadata("loot_editor_category").get(0)).asString());
        }
        if (player.hasMetadata("loot_editor_page")) {
            holder.setPage(((MetadataValue)player.getMetadata("loot_editor_page").get(0)).asInt());
        }
        Inventory inv = Bukkit.createInventory((InventoryHolder)holder, (int)36, (Component)Component.text((String)("\u00a76\u00a7lEdit: " + this.formatMaterialName(material))));
        ItemStack filler = this.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", new String[0]);
        for (int i = 0; i < 36; ++i) {
            inv.setItem(i, filler);
        }
        ItemStack displayItem = new ItemStack(material);
        ItemMeta displayMeta = displayItem.getItemMeta();
        if (displayMeta != null) {
            displayMeta.displayName((Component)Component.text((String)("\u00a7f\u00a7l" + this.formatMaterialName(material))));
            ArrayList<TextComponent> displayLore = new ArrayList<TextComponent>();
            displayLore.add(Component.text((String)("\u00a77Editing for: \u00a7e" + this.capitalize(tier) + " tier")));
            displayMeta.lore(displayLore);
            displayItem.setItemMeta(displayMeta);
        }
        inv.setItem(4, displayItem);
        inv.setItem(10, this.createItem(Material.RED_DYE, "\u00a7c\u00a7lMinimum Amount: \u00a7f" + config.minAmount, "", "\u00a77Left-click: \u00a7a+1", "\u00a77Right-click: \u00a7c-1", "\u00a77Shift+Left: \u00a7a+10", "\u00a77Shift+Right: \u00a7c-10", "", "\u00a78Range: 1-64"));
        inv.setItem(12, this.createItem(Material.GREEN_DYE, "\u00a7a\u00a7lMaximum Amount: \u00a7f" + config.maxAmount, "", "\u00a77Left-click: \u00a7a+1", "\u00a77Right-click: \u00a7c-1", "\u00a77Shift+Left: \u00a7a+10", "\u00a77Shift+Right: \u00a7c-10", "", "\u00a78Range: 1-64"));
        inv.setItem(14, this.createItem(Material.GOLD_NUGGET, "\u00a76\u00a7lWeight (Rarity): \u00a7f" + config.weight, "", "\u00a77Left-click: \u00a7a+1", "\u00a77Right-click: \u00a7c-1", "\u00a77Shift+Left: \u00a7a+5", "\u00a77Shift+Right: \u00a7c-5", "", "\u00a78Higher = More common", "\u00a7c1-2 \u00a77= Very Rare", "\u00a763-5 \u00a77= Rare", "\u00a7e6-10 \u00a77= Uncommon", "\u00a7a11-15 \u00a77= Common"));
        int previewAmount = Math.min(config.minAmount, material.getMaxStackSize());
        ItemStack previewItem = new ItemStack(material, Math.max(1, previewAmount));
        ItemMeta previewMeta = previewItem.getItemMeta();
        if (previewMeta != null) {
            previewMeta.displayName((Component)Component.text((String)"\u00a7b\u00a7lPreview"));
            ArrayList<TextComponent> lore = new ArrayList<TextComponent>();
            lore.add(Component.text((String)("\u00a77Amount: \u00a7f" + config.minAmount + " - " + config.maxAmount)));
            lore.add(Component.text((String)("\u00a77Weight: \u00a7f" + config.weight)));
            previewMeta.lore(lore);
            previewItem.setItemMeta(previewMeta);
        }
        inv.setItem(16, previewItem);
        inv.setItem(19, this.createItem(Material.COAL, "\u00a78\u00a7lPreset: Very Rare", "\u00a77Amount: 1, Weight: 1", "", "\u00a7eClick to apply!"));
        inv.setItem(20, this.createItem(Material.IRON_INGOT, "\u00a77\u00a7lPreset: Rare", "\u00a77Amount: 1-2, Weight: 3", "", "\u00a7eClick to apply!"));
        inv.setItem(21, this.createItem(Material.GOLD_INGOT, "\u00a76\u00a7lPreset: Uncommon", "\u00a77Amount: 2-4, Weight: 6", "", "\u00a7eClick to apply!"));
        inv.setItem(22, this.createItem(Material.DIAMOND, "\u00a7b\u00a7lPreset: Common", "\u00a77Amount: 4-8, Weight: 10", "", "\u00a7eClick to apply!"));
        inv.setItem(23, this.createItem(Material.EMERALD, "\u00a7a\u00a7lPreset: Very Common", "\u00a77Amount: 8-16, Weight: 15", "", "\u00a7eClick to apply!"));
        inv.setItem(30, this.createItem(Material.LIME_CONCRETE, "\u00a7a\u00a7l\u00a7nSAVE & ADD TO LOOT", "", "\u00a77Click to save this item", "\u00a77to the " + this.capitalize(tier) + " tier."));
        inv.setItem(31, this.createItem(Material.ARROW, "\u00a77\u00a7lCancel & Go Back", "\u00a77Return without saving"));
        inv.setItem(32, this.createItem(Material.RED_CONCRETE, "\u00a7c\u00a7l\u00a7nREMOVE FROM LOOT", "", "\u00a77Click to remove this item", "\u00a77from the " + this.capitalize(tier) + " tier."));
        player.openInventory(inv);
        player.setMetadata("loot_editor_tier", (MetadataValue)new FixedMetadataValue((Plugin)this.plugin, (Object)tier));
        player.setMetadata("loot_editor_item", (MetadataValue)new FixedMetadataValue((Plugin)this.plugin, (Object)material));
        player.setMetadata("loot_editor_config", (MetadataValue)new FixedMetadataValue((Plugin)this.plugin, (Object)config));
    }

    public Set<Material> getCurrentLootMaterials(String tier) {
        HashSet<Material> materials = new HashSet<Material>();
        List<String> items = this.plugin.getConfig().getStringList("loot.custom-items." + tier.toLowerCase());
        for (String itemStr : items) {
            try {
                String[] parts = itemStr.split(":");
                Material mat = Material.valueOf(parts[0].toUpperCase());
                materials.add(mat);
            }
            catch (Exception exception) {}
        }
        return materials;
    }

    public LootItemConfig getCurrentItemConfig(String tier, Material material) {
        List<String> items = this.plugin.getConfig().getStringList("loot.custom-items." + tier.toLowerCase());
        for (String itemStr : items) {
            try {
                String[] parts = itemStr.split(":");
                if (Material.valueOf(parts[0].toUpperCase()) != material) continue;
                int minAmount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
                int maxAmount = parts.length > 2 ? Integer.parseInt(parts[2]) : minAmount;
                int weight = parts.length > 3 ? Integer.parseInt(parts[3]) : 10;
                return new LootItemConfig(minAmount, maxAmount, weight);
            }
            catch (Exception exception) {
            }
        }
        return new LootItemConfig(1, 1, 10);
    }

    public void saveItemToConfig(String tier, Material material, LootItemConfig config) {
        String path = "loot.custom-items." + tier.toLowerCase();
        ArrayList<String> items = new ArrayList<String>(this.plugin.getConfig().getStringList(path));
        items.removeIf((String item) -> {
            try {
                String[] parts = item.split(":");
                return Material.valueOf(parts[0].toUpperCase()) == material;
            }
            catch (Exception e) {
                return false;
            }
        });
        String entry = material.name() + ":" + config.minAmount + ":" + config.maxAmount + ":" + config.weight;
        items.add(entry);
        this.plugin.getConfig().set(path, items);
        this.plugin.saveConfig();
        this.plugin.getLootManager().reload();
    }

    public void removeItemFromConfig(String tier, Material material) {
        String path = "loot.custom-items." + tier.toLowerCase();
        ArrayList<String> items = new ArrayList<String>(this.plugin.getConfig().getStringList(path));
        items.removeIf(item -> {
            try {
                String[] parts = item.split(":");
                return Material.valueOf((String)parts[0].toUpperCase()) == material;
            }
            catch (Exception e) {
                return false;
            }
        });
        this.plugin.getConfig().set(path, items);
        this.plugin.saveConfig();
        this.plugin.getLootManager().reload();
    }

    private int countItemsInLoot(String tier, List<Material> materials) {
        Set<Material> inLoot = this.getCurrentLootMaterials(tier);
        int count = 0;
        for (Material mat : materials) {
            if (!inLoot.contains(mat)) continue;
            ++count;
        }
        return count;
    }

    private ItemStack createItem(Material material, String name, String ... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName((Component)Component.text((String)name));
            if (lore.length > 0) {
                ArrayList<TextComponent> loreList = new ArrayList<TextComponent>();
                for (String line : lore) {
                    loreList.add(Component.text((String)line));
                }
                meta.lore(loreList);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public String formatMaterialName(Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        String[] words = name.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }
        return result.toString().trim();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase();
    }

    static {
        CATEGORIES.put("Weapons", Arrays.asList(Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD, Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE, Material.BOW, Material.CROSSBOW, Material.TRIDENT, Material.MACE, Material.ARROW, Material.SPECTRAL_ARROW, Material.TIPPED_ARROW));
        CATEGORIES.put("Armor", Arrays.asList(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS, Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS, Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS, Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS, Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS, Material.SHIELD, Material.ELYTRA, Material.TURTLE_HELMET));
        CATEGORIES.put("Tools", Arrays.asList(Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE, Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL, Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE, Material.SHEARS, Material.FLINT_AND_STEEL, Material.FISHING_ROD, Material.COMPASS, Material.CLOCK, Material.SPYGLASS, Material.LEAD, Material.NAME_TAG));
        CATEGORIES.put("Food", Arrays.asList(Material.BREAD, Material.COOKED_BEEF, Material.COOKED_PORKCHOP, Material.COOKED_CHICKEN, Material.COOKED_MUTTON, Material.COOKED_SALMON, Material.COOKED_COD, Material.BAKED_POTATO, Material.GOLDEN_CARROT, Material.APPLE, Material.GOLDEN_APPLE, Material.ENCHANTED_GOLDEN_APPLE, Material.MELON_SLICE, Material.SWEET_BERRIES, Material.GLOW_BERRIES, Material.PUMPKIN_PIE, Material.CAKE, Material.COOKIE, Material.MUSHROOM_STEW, Material.RABBIT_STEW, Material.BEETROOT_SOUP, Material.SUSPICIOUS_STEW, Material.DRIED_KELP));
        CATEGORIES.put("Building Blocks", Arrays.asList(Material.COBBLESTONE, Material.STONE, Material.DIRT, Material.GRASS_BLOCK, Material.OAK_PLANKS, Material.OAK_LOG, Material.GLASS, Material.WHITE_WOOL, Material.STONE_BRICKS, Material.BRICKS, Material.SAND, Material.GRAVEL, Material.OBSIDIAN, Material.CRYING_OBSIDIAN, Material.NETHERRACK, Material.END_STONE, Material.COBWEB, Material.LADDER, Material.SCAFFOLDING, Material.IRON_BLOCK, Material.GOLD_BLOCK, Material.DIAMOND_BLOCK, Material.EMERALD_BLOCK, Material.SLIME_BLOCK, Material.HONEY_BLOCK, Material.HAY_BLOCK));
        CATEGORIES.put("Special Items", Arrays.asList(Material.ENDER_PEARL, Material.ENDER_EYE, Material.TOTEM_OF_UNDYING, Material.ENCHANTED_BOOK, Material.EXPERIENCE_BOTTLE, Material.TNT, Material.END_CRYSTAL, Material.FIREWORK_ROCKET, Material.WATER_BUCKET, Material.LAVA_BUCKET, Material.MILK_BUCKET, Material.SNOWBALL, Material.EGG, Material.WIND_CHARGE, Material.FIRE_CHARGE, Material.GOAT_HORN));
        CATEGORIES.put("Potions & Brewing", Arrays.asList(Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.BREWING_STAND, Material.BLAZE_POWDER, Material.NETHER_WART, Material.GHAST_TEAR, Material.SPIDER_EYE, Material.FERMENTED_SPIDER_EYE, Material.MAGMA_CREAM, Material.GLISTERING_MELON_SLICE, Material.RABBIT_FOOT, Material.GLASS_BOTTLE, Material.DRAGON_BREATH, Material.BLAZE_ROD, Material.PHANTOM_MEMBRANE));
        CATEGORIES.put("Redstone", Arrays.asList(Material.REDSTONE, Material.REDSTONE_TORCH, Material.REPEATER, Material.COMPARATOR, Material.PISTON, Material.STICKY_PISTON, Material.DISPENSER, Material.DROPPER, Material.HOPPER, Material.OBSERVER, Material.LEVER, Material.TRIPWIRE_HOOK, Material.TARGET, Material.SCULK_SENSOR, Material.CALIBRATED_SCULK_SENSOR, Material.STRING, Material.DAYLIGHT_DETECTOR));
        ArrayList<Material> spawnEggs = new ArrayList<Material>();
        for (Material mat : Material.values()) {
            if (!mat.name().endsWith("_SPAWN_EGG")) continue;
            spawnEggs.add(mat);
        }
        CATEGORIES.put("Spawn Eggs", spawnEggs);
        CATEGORIES.put("Valuables", Arrays.asList(Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT, Material.IRON_INGOT, Material.NETHERITE_INGOT, Material.NETHERITE_SCRAP, Material.ANCIENT_DEBRIS, Material.LAPIS_LAZULI, Material.REDSTONE, Material.COAL, Material.COPPER_INGOT, Material.AMETHYST_SHARD, Material.QUARTZ));
        ArrayList<Material> musicDiscs = new ArrayList<Material>();
        for (Material mat : Material.values()) {
            if (!mat.name().startsWith("MUSIC_DISC_")) continue;
            musicDiscs.add(mat);
        }
        CATEGORIES.put("Music Discs", musicDiscs);
    }

    public static class LootItemConfig {
        public int minAmount;
        public int maxAmount;
        public int weight;

        public LootItemConfig(int minAmount, int maxAmount, int weight) {
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            this.weight = weight;
        }

        public LootItemConfig copy() {
            return new LootItemConfig(this.minAmount, this.maxAmount, this.weight);
        }
    }
}

