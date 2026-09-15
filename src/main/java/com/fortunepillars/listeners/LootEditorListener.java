/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryDragEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.InventoryHolder
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.metadata.FixedMetadataValue
 *  org.bukkit.metadata.MetadataValue
 *  org.bukkit.plugin.Plugin
 */
package com.fortunepillars.listeners;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.gui.loot.LootEditorGUI;
import com.fortunepillars.gui.loot.LootEditorHolder;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public class LootEditorListener
implements Listener {
    private final FortunePillars plugin;
    private final LootEditorGUI gui;

    public LootEditorListener(FortunePillars plugin) {
        this.plugin = plugin;
        this.gui = new LootEditorGUI(plugin);
    }

    public LootEditorGUI getGui() {
        return this.gui;
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=false)
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) {
            return;
        }
        InventoryHolder inventoryHolder = event.getInventory().getHolder();
        if (!(inventoryHolder instanceof LootEditorHolder)) {
            return;
        }
        LootEditorHolder holder = (LootEditorHolder)inventoryHolder;
        event.setCancelled(true);
        if (clickedInventory != event.getView().getTopInventory()) {
            return;
        }
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }
        if (clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        }
        int slot = event.getSlot();
        boolean isLeftClick = event.isLeftClick();
        boolean isRightClick = event.isRightClick();
        boolean isShiftClick = event.isShiftClick();
        switch (holder.getMenuType()) {
            case MAIN_MENU: {
                this.handleMainMenu(player, clicked, slot);
                break;
            }
            case CATEGORY_MENU: {
                this.handleCategoryMenu(player, holder, clicked, slot);
                break;
            }
            case ITEM_BROWSER: {
                this.handleItemBrowser(player, holder, clicked, slot, isRightClick);
                break;
            }
            case ITEM_EDITOR: {
                this.handleItemEditor(player, holder, clicked, slot, isLeftClick, isShiftClick);
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof LootEditorHolder) {
            event.setCancelled(true);
        }
    }

    private void handleMainMenu(Player player, ItemStack clicked, int slot) {
        switch (clicked.getType()) {
            case IRON_SWORD: {
                this.gui.openCategoryMenu(player, "normal");
                break;
            }
            case DIAMOND_SWORD: {
                this.gui.openCategoryMenu(player, "balanced");
                break;
            }
            case NETHERITE_SWORD: {
                this.gui.openCategoryMenu(player, "op");
                break;
            }
            case BOOK: {
                player.sendMessage(FortunePillars.parseWithPrefix("<gold>Loot Table Statistics:"));
                player.sendMessage(FortunePillars.parse("<gray>Normal: <white>" + this.plugin.getLootManager().getNormalLootMulti().size() + " items"));
                player.sendMessage(FortunePillars.parse("<gray>Balanced: <white>" + this.plugin.getLootManager().getBalancedLootMulti().size() + " items"));
                player.sendMessage(FortunePillars.parse("<gray>OP: <white>" + this.plugin.getLootManager().getOpLootMulti().size() + " items"));
                break;
            }
            case REDSTONE: {
                this.plugin.getLootManager().reload();
                player.sendMessage(FortunePillars.parseWithPrefix("<green>Loot tables reloaded!"));
                this.gui.openMainMenu(player);
            }
        }
    }

    private void handleCategoryMenu(Player player, LootEditorHolder holder, ItemStack clicked, int slot) {
        if (slot == 49 && clicked.getType() == Material.ARROW) {
            this.gui.openMainMenu(player);
            return;
        }
        String category = this.getCategoryFromIcon(clicked.getType());
        if (category != null) {
            player.setMetadata("loot_editor_category", (MetadataValue)new FixedMetadataValue((Plugin)this.plugin, (Object)category));
            player.setMetadata("loot_editor_page", (MetadataValue)new FixedMetadataValue((Plugin)this.plugin, (Object)0));
            this.gui.openItemBrowser(player, holder.getTier(), category, 0);
        }
    }

    private void handleItemBrowser(Player player, LootEditorHolder holder, ItemStack clicked, int slot, boolean isRightClick) {
        String tier = holder.getTier();
        String category = holder.getCategory();
        int currentPage = holder.getPage();
        if (slot == 49 && clicked.getType() == Material.BARRIER) {
            this.gui.openCategoryMenu(player, tier);
            return;
        }
        if (slot == 45 && clicked.getType() == Material.ARROW) {
            this.gui.openItemBrowser(player, tier, category, currentPage - 1);
            return;
        }
        if (slot == 53 && clicked.getType() == Material.ARROW) {
            this.gui.openItemBrowser(player, tier, category, currentPage + 1);
            return;
        }
        if (slot == 50 && clicked.getType() == Material.PAPER) {
            return;
        }
        if (slot < 45 && slot >= 0) {
            Material material = clicked.getType();
            player.setMetadata("loot_editor_category", (MetadataValue)new FixedMetadataValue((Plugin)this.plugin, (Object)category));
            player.setMetadata("loot_editor_page", (MetadataValue)new FixedMetadataValue((Plugin)this.plugin, (Object)currentPage));
            if (isRightClick) {
                this.gui.removeItemFromConfig(tier, material);
                player.sendMessage(FortunePillars.parseWithPrefix("<red>Removed <white>" + this.gui.formatMaterialName(material) + "<red> from <yellow>" + tier + "<red> tier."));
                this.gui.openItemBrowser(player, tier, category, currentPage);
            } else {
                this.gui.openItemEditor(player, tier, material);
            }
        }
    }

    private void handleItemEditor(Player player, LootEditorHolder holder, ItemStack clicked, int slot, boolean isLeftClick, boolean isShiftClick) {
        String tier = holder.getTier();
        Material material = holder.getEditingMaterial();
        LootEditorGUI.LootItemConfig config = holder.getConfig();
        if (tier == null || material == null || config == null) {
            player.sendMessage(FortunePillars.parseWithPrefix("<red>Error: Session data lost!"));
            this.gui.openMainMenu(player);
            return;
        }
        LootEditorGUI.LootItemConfig workingConfig = config.copy();
        boolean needsRefresh = false;
        int change = isShiftClick ? (isLeftClick ? 10 : -10) : (isLeftClick ? 1 : -1);
        switch (slot) {
            case 10: {
                workingConfig.minAmount = Math.max(1, Math.min(64, workingConfig.minAmount + change));
                if (workingConfig.maxAmount < workingConfig.minAmount) {
                    workingConfig.maxAmount = workingConfig.minAmount;
                }
                needsRefresh = true;
                break;
            }
            case 12: {
                workingConfig.maxAmount = Math.max(workingConfig.minAmount, Math.min(64, workingConfig.maxAmount + change));
                needsRefresh = true;
                break;
            }
            case 14: {
                int weightChange = isShiftClick ? (isLeftClick ? 5 : -5) : (isLeftClick ? 1 : -1);
                workingConfig.weight = Math.max(1, Math.min(100, workingConfig.weight + weightChange));
                needsRefresh = true;
                break;
            }
            case 19: {
                workingConfig.minAmount = 1;
                workingConfig.maxAmount = 1;
                workingConfig.weight = 1;
                needsRefresh = true;
                player.sendMessage(FortunePillars.parseWithPrefix("<gray>Applied <white>Very Rare<gray> preset."));
                break;
            }
            case 20: {
                workingConfig.minAmount = 1;
                workingConfig.maxAmount = 2;
                workingConfig.weight = 3;
                needsRefresh = true;
                player.sendMessage(FortunePillars.parseWithPrefix("<gray>Applied <white>Rare<gray> preset."));
                break;
            }
            case 21: {
                workingConfig.minAmount = 2;
                workingConfig.maxAmount = 4;
                workingConfig.weight = 6;
                needsRefresh = true;
                player.sendMessage(FortunePillars.parseWithPrefix("<gray>Applied <white>Uncommon<gray> preset."));
                break;
            }
            case 22: {
                workingConfig.minAmount = 4;
                workingConfig.maxAmount = 8;
                workingConfig.weight = 10;
                needsRefresh = true;
                player.sendMessage(FortunePillars.parseWithPrefix("<gray>Applied <white>Common<gray> preset."));
                break;
            }
            case 23: {
                workingConfig.minAmount = 8;
                workingConfig.maxAmount = 16;
                workingConfig.weight = 15;
                needsRefresh = true;
                player.sendMessage(FortunePillars.parseWithPrefix("<gray>Applied <white>Very Common<gray> preset."));
                break;
            }
            case 30: {
                this.gui.saveItemToConfig(tier, material, workingConfig);
                player.sendMessage(FortunePillars.parseWithPrefix("<green>\u2713 Saved! <white>" + this.gui.formatMaterialName(material) + "<green> added to <yellow>" + tier + "<green> tier."));
                player.sendMessage(FortunePillars.parse("<gray>Amount: " + workingConfig.minAmount + "-" + workingConfig.maxAmount + ", Weight: " + workingConfig.weight));
                String category = holder.getCategory();
                int page = holder.getPage();
                if (category != null) {
                    this.gui.openItemBrowser(player, tier, category, page);
                } else {
                    this.gui.openCategoryMenu(player, tier);
                }
                return;
            }
            case 31: {
                String category = holder.getCategory();
                int page = holder.getPage();
                if (category != null) {
                    this.gui.openItemBrowser(player, tier, category, page);
                } else {
                    this.gui.openCategoryMenu(player, tier);
                }
                return;
            }
            case 32: {
                this.gui.removeItemFromConfig(tier, material);
                player.sendMessage(FortunePillars.parseWithPrefix("<red>\u2713 Removed <white>" + this.gui.formatMaterialName(material) + "<red> from <yellow>" + tier + "<red> tier."));
                String category = holder.getCategory();
                int page = holder.getPage();
                if (category != null) {
                    this.gui.openItemBrowser(player, tier, category, page);
                } else {
                    this.gui.openCategoryMenu(player, tier);
                }
                return;
            }
        }
        if (needsRefresh) {
            this.gui.openItemEditor(player, tier, material, workingConfig);
        }
    }

    private String getCategoryFromIcon(Material icon) {
        for (Map.Entry<String, List<Material>> entry : LootEditorGUI.CATEGORIES.entrySet()) {
            if (entry.getValue().isEmpty() || entry.getValue().get(0) != icon) continue;
            return entry.getKey();
        }
        return null;
    }
}

