/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryCloseEvent
 *  org.bukkit.event.inventory.InventoryDragEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.InventoryHolder
 *  org.bukkit.plugin.Plugin
 */
package com.fortunepillars.gui;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.gui.ArenaSelectGUI;
import com.fortunepillars.gui.GUI;
import com.fortunepillars.gui.MainMenuGUI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;

public class GUIManager
implements Listener {
    private final FortunePillars plugin;
    private final Map<UUID, GUI> openGUIs;
    private final Map<UUID, Boolean> suppressClose;

    public GUIManager(FortunePillars plugin) {
        this.plugin = plugin;
        this.openGUIs = new ConcurrentHashMap<UUID, GUI>();
        this.suppressClose = new ConcurrentHashMap<UUID, Boolean>();
        plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)plugin);
    }

    public void registerGUI(Player player, GUI gui) {
        this.openGUIs.put(player.getUniqueId(), gui);
    }

    public void unregisterGUI(Player player) {
        this.openGUIs.remove(player.getUniqueId());
    }

    public void suppressNextClose(Player player) {
        this.suppressClose.put(player.getUniqueId(), true);
    }

    public void openGUI(Player player, GUI gui) {
        gui.open();
    }

    public void closeGUI(Player player) {
        this.openGUIs.remove(player.getUniqueId());
        player.closeInventory();
    }

    public GUI getOpenGUI(Player player) {
        return this.openGUIs.get(player.getUniqueId());
    }

    public boolean hasGUIOpen(Player player) {
        return this.openGUIs.containsKey(player.getUniqueId());
    }

    public void openMainMenu(Player player) {
        new MainMenuGUI(this.plugin, player).open();
    }

    public void openArenaSelector(Player player) {
        new ArenaSelectGUI(this.plugin, player).open();
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=false)
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        Inventory topInventory = event.getView().getTopInventory();
        InventoryHolder inventoryHolder = topInventory.getHolder();
        if (inventoryHolder instanceof GUI) {
            GUI gui = (GUI)inventoryHolder;
            event.setCancelled(true);
            if (event.getClickedInventory() != null && event.getClickedInventory().equals((Object)topInventory)) {
                gui.handleClick(event);
            }
            return;
        }
        GUI gui = this.openGUIs.get(player.getUniqueId());
        if (gui != null) {
            event.setCancelled(true);
            if (event.getClickedInventory() != null && event.getClickedInventory().equals((Object)gui.getInventory())) {
                gui.handleClick(event);
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=false)
    public void onInventoryDrag(InventoryDragEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof GUI) {
            event.setCancelled(true);
            return;
        }
        if (this.openGUIs.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        HumanEntity humanEntity = event.getPlayer();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        UUID uid = player.getUniqueId();
        if (this.suppressClose.remove(uid) != null) {
            return;
        }
        GUI gui = this.openGUIs.remove(uid);
        if (gui != null) {
            gui.onClose(player);
        }
    }

    public FortunePillars getPlugin() {
        return this.plugin;
    }

    public void shutdown() {
        for (UUID uuid : this.openGUIs.keySet()) {
            Player player = this.plugin.getServer().getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;
            player.closeInventory();
        }
        this.openGUIs.clear();
        this.suppressClose.clear();
    }
}

