/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package com.fortunepillars.gui;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.arena.GameState;
import com.fortunepillars.gui.GUI;
import com.fortunepillars.gui.MainMenuGUI;
import com.fortunepillars.utils.ItemBuilder;
import java.util.ArrayList;
import java.util.Collection;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ArenaSelectGUI
extends GUI {
    public ArenaSelectGUI(FortunePillars plugin, Player player) {
        super(plugin, player, "<gradient:#FFD700:#FFA500>Select Arena</gradient>", 54);
    }

    @Override
    public void setup() {
        this.fillBorder(Material.ORANGE_STAINED_GLASS_PANE);
        Collection<Arena> arenas = this.plugin.getArenaManager().getAllArenas();
        if (arenas.isEmpty()) {
            this.setItem(22, ItemBuilder.of(Material.BARRIER).name("<red>No Arenas Available").lore("", "<gray>There are no arenas set up yet.", "<gray>Please wait for an admin to create one.").bedrockSafe().build());
            this.setItem(49, ItemBuilder.of(Material.BARRIER).name("<red>Close").bedrockSafe().build(), event -> this.player.closeInventory());
            return;
        }
        this.setItem(4, ItemBuilder.of(Material.NETHER_STAR).name("<green><bold>Quick Join</bold></green>").lore("", "<gray>Automatically join the best", "<gray>available arena", "", "<yellow>Click to quick join!").glow().bedrockSafe().build(), event -> {
            this.player.closeInventory();
            if (this.plugin.getArenaManager().quickJoin(this.player)) {
                this.player.playSound(this.player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
            } else {
                this.player.sendMessage(FortunePillars.parseWithPrefix("<red>No available arenas found!"));
                this.player.playSound(this.player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            }
        });
        int[] availableSlots = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
        int index = 0;
        for (Arena arena : arenas) {
            if (index >= availableSlots.length) break;
            int slot = availableSlots[index++];
            Arena arenaRef = arena;
            this.setItem(slot, this.createArenaItem(arena), event -> this.joinArena(arenaRef));
        }
        this.setItem(45, ItemBuilder.of(Material.ARROW).name("<yellow><bold>\u2190 Back</bold></yellow>").lore("", "<gray>Return to main menu").bedrockSafe().build(), event -> {
            this.player.playSound(this.player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            new MainMenuGUI(this.plugin, this.player).open();
        });
        this.setItem(49, ItemBuilder.of(Material.SUNFLOWER).name("<yellow><bold>Refresh</bold></yellow>").lore("", "<gray>Refresh arena list").bedrockSafe().build(), event -> {
            this.player.playSound(this.player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
            this.refresh();
        });
        this.setItem(53, ItemBuilder.of(Material.BARRIER).name("<red>Close").bedrockSafe().build(), event -> this.player.closeInventory());
    }

    private ItemStack createArenaItem(Arena arena) {
        GameState state = arena.getState();
        Material material = this.getStateItemMaterial(state);
        boolean canJoin = state.isJoinable() && arena.isSetupComplete() && arena.getPlayerCount() < arena.getMaxPlayers();
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add("<gray>State: " + state.getColoredName());
        lore.add("<gray>Players: <white>" + arena.getPlayerCount() + "/" + arena.getMaxPlayers());
        lore.add("<gray>Mode: <yellow>" + arena.getGameMode().getDisplayName());
        lore.add("");
        if (!arena.isSetupComplete()) {
            lore.add("<red>Arena not fully configured");
        } else if (canJoin) {
            lore.add("<green>Click to join!");
        } else if (state == GameState.IN_GAME) {
            lore.add("<yellow>Game in progress...");
        } else {
            lore.add("<red>Cannot join right now");
        }
        return ItemBuilder.of(material).name("<yellow><bold>" + arena.getName().toUpperCase() + "</bold></yellow>").lore(lore).glow(canJoin).bedrockSafe().build();
    }

    private Material getStateItemMaterial(GameState state) {
        return switch (state) {
            default -> throw new MatchException(null, null);
            case GameState.DISABLED -> Material.GRAY_WOOL;
            case GameState.WAITING -> Material.LIME_WOOL;
            case GameState.STARTING -> Material.YELLOW_WOOL;
            case GameState.IN_GAME -> Material.RED_WOOL;
            case GameState.ENDING -> Material.ORANGE_WOOL;
            case GameState.RESETTING -> Material.BLUE_WOOL;
        };
    }

    private void joinArena(Arena arena) {
        this.player.closeInventory();
        if (!arena.getState().isJoinable()) {
            this.player.sendMessage(FortunePillars.parseWithPrefix("<red>This arena is not available right now!"));
            this.player.playSound(this.player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }
        if (!arena.isSetupComplete()) {
            this.player.sendMessage(FortunePillars.parseWithPrefix("<red>This arena is not fully configured!"));
            this.player.playSound(this.player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }
        if (arena.getPlayerCount() >= arena.getMaxPlayers()) {
            this.player.sendMessage(FortunePillars.parseWithPrefix("<red>This arena is full!"));
            this.player.playSound(this.player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }
        if (this.plugin.getArenaManager().joinArena(this.player, arena.getName())) {
            this.player.playSound(this.player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
        }
    }
}

