/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 */
package com.fortunepillars.gui;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.gui.ArenaSelectGUI;
import com.fortunepillars.gui.CosmeticsGUI;
import com.fortunepillars.gui.GUI;
import com.fortunepillars.gui.LeaderboardGUI;
import com.fortunepillars.gui.VotingGUI;
import com.fortunepillars.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class MainMenuGUI
extends GUI {
    public MainMenuGUI(FortunePillars plugin, Player player) {
        super(plugin, player, "<gradient:#FFD700:#FFA500>Fortune Pillars</gradient>", 45);
    }

    @Override
    public void setup() {
        this.fillBorder(Material.ORANGE_STAINED_GLASS_PANE);
        this.setItem(4, ItemBuilder.of(Material.NETHER_STAR).name("<gold><bold>FORTUNE PILLARS</bold></gold>").lore("", "<gray>The ultimate PvP experience!", "", "<yellow>Select an option below").glow().bedrockSafe().build());
        this.setItem(11, ItemBuilder.of(Material.DIAMOND_SWORD).name("<green><bold>Quick Join</bold></green>").lore("", "<gray>Join a random arena", "", "<yellow>Click to play!").glow().hideFlags().bedrockSafe().build(), event -> {
            this.player.closeInventory();
            this.player.performCommand("tof join");
            this.player.playSound(this.player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
        });
        this.setItem(13, ItemBuilder.of(Material.COMPASS).name("<aqua><bold>Select Arena</bold></aqua>").lore("", "<gray>Choose a specific arena", "", "<yellow>Click to browse!").bedrockSafe().build(), event -> {
            this.player.playSound(this.player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            new ArenaSelectGUI(this.plugin, this.player).open();
        });
        this.setItem(15, ItemBuilder.of(Material.ENDER_CHEST).name("<light_purple><bold>Cosmetics</bold></light_purple>").lore("", "<gray>Customize your appearance!", "", "<yellow>Click to open!").glow().bedrockSafe().build(), event -> {
            this.player.playSound(this.player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            new CosmeticsGUI(this.plugin, this.player).open();
        });
        this.setItem(20, ItemBuilder.of(Material.BOOK).name("<yellow><bold>Your Stats</bold></yellow>").lore("", "<gray>View your statistics", "", "<yellow>Click to view!").bedrockSafe().build(), event -> {
            this.player.closeInventory();
            this.player.performCommand("tof stats");
            this.player.playSound(this.player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
        });
        this.setItem(22, ItemBuilder.of(Material.GOLD_BLOCK).name("<gold><bold>Leaderboards</bold></gold>").lore("", "<gray>Top players", "", "<yellow>Click to view!").bedrockSafe().build(), event -> {
            this.player.playSound(this.player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            new LeaderboardGUI(this.plugin, this.player).open();
        });
        this.setItem(24, ItemBuilder.of(Material.COMPARATOR).name("<gray><bold>Settings</bold></gray>").lore("", "<gray>Configure preferences", "", "<yellow>Coming soon!").bedrockSafe().build(), event -> {
            this.player.sendMessage(FortunePillars.parseWithPrefix("<yellow>Settings coming soon!"));
            this.player.playSound(this.player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
        });
        Arena playerArena = this.plugin.getArenaManager().getPlayerArena(this.player);
        if (playerArena != null) {
            this.setItem(31, ItemBuilder.of(Material.PAPER).name("<aqua><bold>Vote for Game Mode</bold></aqua>").lore("", "<gray>Vote for game settings", "<gray>Arena: <yellow>" + playerArena.getName(), "<gray>State: " + playerArena.getState().getColoredName(), "", "<yellow>Click to vote!").glow().bedrockSafe().build(), event -> {
                this.player.playSound(this.player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                new VotingGUI(this.plugin, this.player, playerArena).open();
            });
        }
        this.setItem(40, ItemBuilder.of(Material.BARRIER).name("<red>Close").bedrockSafe().build(), event -> this.player.closeInventory());
    }
}

