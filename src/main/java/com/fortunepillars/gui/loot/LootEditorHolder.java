/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.InventoryHolder
 *  org.jetbrains.annotations.NotNull
 */
package com.fortunepillars.gui.loot;

import com.fortunepillars.gui.loot.LootEditorGUI;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class LootEditorHolder
implements InventoryHolder {
    private final MenuType menuType;
    private String tier;
    private String category;
    private int page;
    private Material editingMaterial;
    private LootEditorGUI.LootItemConfig config;

    public LootEditorHolder(MenuType menuType) {
        this.menuType = menuType;
    }

    public MenuType getMenuType() {
        return this.menuType;
    }

    public String getTier() {
        return this.tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getPage() {
        return this.page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public Material getEditingMaterial() {
        return this.editingMaterial;
    }

    public void setEditingMaterial(Material editingMaterial) {
        this.editingMaterial = editingMaterial;
    }

    public LootEditorGUI.LootItemConfig getConfig() {
        return this.config;
    }

    public void setConfig(LootEditorGUI.LootItemConfig config) {
        this.config = config;
    }

    @NotNull
    public Inventory getInventory() {
        return null;
    }

    public static enum MenuType {
        MAIN_MENU,
        CATEGORY_MENU,
        ITEM_BROWSER,
        ITEM_EDITOR;

    }
}

