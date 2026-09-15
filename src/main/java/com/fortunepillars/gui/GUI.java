/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.entity.Player
 *  org.bukkit.event.inventory.ClickType
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.InventoryHolder
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 */
package com.fortunepillars.gui;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.utils.ItemBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public abstract class GUI
implements InventoryHolder {
    protected final FortunePillars plugin;
    protected final Player player;
    protected final Inventory inventory;
    protected final Map<Integer, Consumer<InventoryClickEvent>> clickHandlers;
    protected final String title;
    protected final int size;

    public GUI(FortunePillars plugin, Player player, String title, int size) {
        this.plugin = plugin;
        this.player = player;
        this.title = title;
        this.size = size;
        this.clickHandlers = new HashMap<Integer, Consumer<InventoryClickEvent>>();
        this.inventory = Bukkit.createInventory((InventoryHolder)this, (int)size, (Component)FortunePillars.parse(title));
    }

    public abstract void setup();

    public void open() {
        this.setup();
        this.plugin.getServer().getScheduler().runTask((Plugin)this.plugin, () -> {
            this.plugin.getGuiManager().registerGUI(this.player, this);
            this.player.openInventory(this.inventory);
        });
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= this.size) {
            return;
        }
        Consumer<InventoryClickEvent> handler = this.clickHandlers.get(slot);
        if (handler != null) {
            handler.accept(event);
        }
    }

    public void handleClick(Player player, int slot, ItemStack clicked, ClickType clickType) {
        Consumer<InventoryClickEvent> handler = this.clickHandlers.get(slot);
        if (handler != null) {
            handler.accept(null);
        }
    }

    public void onClose(Player player) {
    }

    protected void setItem(int slot, ItemStack item) {
        if (slot >= 0 && slot < this.size) {
            this.inventory.setItem(slot, item);
            this.clickHandlers.remove(slot);
        }
    }

    protected void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> onClick) {
        if (slot >= 0 && slot < this.size) {
            this.inventory.setItem(slot, item);
            if (onClick != null) {
                this.clickHandlers.put(slot, onClick);
            } else {
                this.clickHandlers.remove(slot);
            }
        }
    }

    protected void fillBorder(Material material) {
        int i;
        ItemStack filler = ItemBuilder.filler(material);
        int rows = this.size / 9;
        for (i = 0; i < 9; ++i) {
            this.setItem(i, filler);
        }
        for (i = this.size - 9; i < this.size; ++i) {
            this.setItem(i, filler);
        }
        for (i = 1; i < rows - 1; ++i) {
            this.setItem(i * 9, filler);
            this.setItem(i * 9 + 8, filler);
        }
    }

    protected void fill(Material material) {
        ItemStack filler = ItemBuilder.filler(material);
        for (int i = 0; i < this.size; ++i) {
            if (this.inventory.getItem(i) != null && this.inventory.getItem(i).getType() != Material.AIR) continue;
            this.setItem(i, filler);
        }
    }

    protected void clearSlot(int slot) {
        if (slot >= 0 && slot < this.size) {
            this.inventory.setItem(slot, null);
            this.clickHandlers.remove(slot);
        }
    }

    protected void clear() {
        this.inventory.clear();
        this.clickHandlers.clear();
    }

    public void refresh() {
        this.clear();
        this.setup();
    }

    protected void updateSlot(int slot, ItemStack item) {
        if (slot >= 0 && slot < this.size) {
            this.inventory.setItem(slot, item);
        }
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getTitle() {
        return this.title;
    }

    public int getSize() {
        return this.size;
    }

    public FortunePillars getPlugin() {
        return this.plugin;
    }
}

