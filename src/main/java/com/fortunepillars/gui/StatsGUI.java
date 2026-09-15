/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 */
package com.fortunepillars.gui;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.gui.GUI;
import com.fortunepillars.gui.LeaderboardGUI;
import com.fortunepillars.player.PlayerManager;
import com.fortunepillars.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class StatsGUI
extends GUI {
    private final OfflinePlayer target;
    private PlayerManager.PlayerStats stats;

    public StatsGUI(FortunePillars plugin, Player player, OfflinePlayer target) {
        super(plugin, player, "<gradient:#FFD700:#FFA500>" + target.getName() + "'s Stats</gradient>", 45);
        this.target = target;
    }

    @Override
    public void setup() {
        this.fillBorder(Material.ORANGE_STAINED_GLASS_PANE);
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            this.stats = this.plugin.getDatabase().getPlayerStats(this.target.getUniqueId());
            this.plugin.getServer().getScheduler().runTask((Plugin)this.plugin, this::populateStats);
        });
        this.setItem(22, ItemBuilder.of(Material.CLOCK).name("<yellow>Loading stats...").bedrockSafe().build());
    }

    private void populateStats() {
        if (this.stats == null) {
            this.setItem(22, ItemBuilder.of(Material.BARRIER).name("<red>Failed to load stats").bedrockSafe().build());
            return;
        }
        this.setItem(13, ItemBuilder.of(Material.PLAYER_HEAD).skullOwner(this.target.getName()).name("<gold><bold>" + this.target.getName()).lore("", "<gray>View detailed statistics below").bedrockSafe().build());
        this.setItem(20, ItemBuilder.of(Material.GOLDEN_SWORD).name("<gold><bold>Victories</bold></gold>").lore("", "<yellow>Total Wins: <white>" + this.stats.getWins(), "<yellow>Win Rate: <white>" + String.format("%.1f%%", this.stats.getWinRate()), "", "<gray>Games won out of " + this.stats.getGamesPlayed() + " played").hideFlags().bedrockSafe().build());
        this.setItem(21, ItemBuilder.of(Material.DIAMOND_SWORD).name("<red><bold>Kills</bold></red>").lore("", "<yellow>Total Kills: <white>" + this.stats.getKills(), "<yellow>K/D Ratio: <white>" + String.format("%.2f", this.stats.getKDR()), "", "<gray>Enemies eliminated").hideFlags().bedrockSafe().build());
        this.setItem(22, ItemBuilder.of(Material.SKELETON_SKULL).name("<dark_red><bold>Deaths</bold></dark_red>").lore("", "<yellow>Total Deaths: <white>" + this.stats.getDeaths(), "", "<gray>Times eliminated by enemies").bedrockSafe().build());
        this.setItem(23, ItemBuilder.of(Material.BOOK).name("<aqua><bold>Games Played</bold></aqua>").lore("", "<yellow>Total Games: <white>" + this.stats.getGamesPlayed(), "", "<gray>Total matches participated in").bedrockSafe().build());
        this.setItem(24, ItemBuilder.of(Material.BLAZE_POWDER).name("<light_purple><bold>Winstreak</bold></light_purple>").lore("", "<yellow>Current Streak: <white>" + this.stats.getWinstreak(), "<yellow>Best Streak: <white>" + this.stats.getHighestWinstreak(), "", "<gray>Consecutive wins").glow(this.stats.getWinstreak() >= 5).bedrockSafe().build());
        this.setItem(40, ItemBuilder.of(Material.GOLD_INGOT).name("<yellow><bold>View Leaderboards</bold></yellow>").lore("", "<gray>Click to view top players").bedrockSafe().build(), click -> {
            this.player.closeInventory();
            new LeaderboardGUI(this.plugin, this.player).open();
        });
        this.setItem(44, ItemBuilder.of(Material.BARRIER).name("<red>Close").bedrockSafe().build(), click -> this.player.closeInventory());
    }
}

