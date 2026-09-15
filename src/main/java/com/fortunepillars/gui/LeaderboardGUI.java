/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 */
package com.fortunepillars.gui;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.gui.GUI;
import com.fortunepillars.gui.LeaderboardType;
import com.fortunepillars.gui.MainMenuGUI;
import com.fortunepillars.leaderboard.LeaderboardManager;
import com.fortunepillars.utils.ItemBuilder;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class LeaderboardGUI
extends GUI {
    private LeaderboardType currentType = LeaderboardType.WINS;

    public LeaderboardGUI(FortunePillars plugin, Player player) {
        super(plugin, player, "<gradient:#FFD700:#FFA500>Leaderboards</gradient>", 54);
    }

    @Override
    public void setup() {
        this.fillBorder(Material.YELLOW_STAINED_GLASS_PANE);
        this.populateLeaderboard();
    }

    private void populateLeaderboard() {
        int[] entrySlots;
        for (int slot : entrySlots = new int[]{19, 20, 21, 22, 23, 24, 25, 28, 29, 30}) {
            this.clearSlot(slot);
        }
        this.setItem(10, this.createTypeItem(LeaderboardType.WINS, Material.GOLDEN_SWORD), event -> this.switchType(LeaderboardType.WINS));
        this.setItem(11, this.createTypeItem(LeaderboardType.KILLS, Material.DIAMOND_SWORD), event -> this.switchType(LeaderboardType.KILLS));
        this.setItem(12, this.createTypeItem(LeaderboardType.WINSTREAK, Material.BLAZE_POWDER), event -> this.switchType(LeaderboardType.WINSTREAK));
        this.setItem(4, ItemBuilder.of(this.getMaterialForType(this.currentType)).name("<gold><bold>" + this.currentType.getDisplayName() + "</bold></gold>").lore("", "<gray>Top 10 players").glow().hideFlags().bedrockSafe().build());
        List<LeaderboardManager.LeaderboardEntry> entries = this.plugin.getLeaderboardManager().getLeaderboard(this.currentType.getKey());
        for (int i = 0; i < 10; ++i) {
            int slot = entrySlots[i];
            if (i < entries.size()) {
                LeaderboardManager.LeaderboardEntry entry = entries.get(i);
                this.setItem(slot, this.createEntryItem(i + 1, entry));
                continue;
            }
            this.setItem(slot, this.createEmptyEntryItem(i + 1));
        }
        this.setItem(45, ItemBuilder.of(Material.ARROW).name("<yellow><bold>\u2190 Back</bold></yellow>").lore("", "<gray>Return to main menu").bedrockSafe().build(), event -> {
            this.player.playSound(this.player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            new MainMenuGUI(this.plugin, this.player).open();
        });
        this.setItem(49, ItemBuilder.of(Material.SUNFLOWER).name("<yellow><bold>Refresh</bold></yellow>").lore("", "<gray>Click to refresh leaderboard").bedrockSafe().build(), event -> {
            this.player.playSound(this.player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            this.plugin.getLeaderboardManager().forceUpdate();
            this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, this::refresh, 10L);
        });
        this.setItem(53, ItemBuilder.of(Material.BARRIER).name("<red>Close").bedrockSafe().build(), event -> this.player.closeInventory());
    }

    private void switchType(LeaderboardType type) {
        if (this.currentType == type) {
            return;
        }
        this.currentType = type;
        this.player.playSound(this.player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
        this.populateLeaderboard();
    }

    private ItemStack createTypeItem(LeaderboardType type, Material material) {
        boolean selected = this.currentType == type;
        return ItemBuilder.of(material).name((selected ? "<green>\u25b6 " : "<gray>") + type.getDisplayName()).lore("", selected ? "<green>Currently viewing" : "<yellow>Click to view").glow(selected).hideFlags().bedrockSafe().build();
    }

    private ItemStack createEntryItem(int rank, LeaderboardManager.LeaderboardEntry entry) {
        String rankColor = switch (rank) {
            case 1 -> "<gold>";
            case 2 -> "<gray>";
            case 3 -> "<#CD7F32>";
            default -> "<white>";
        };
        Material material = switch (rank) {
            case 1 -> Material.GOLDEN_HELMET;
            case 2 -> Material.IRON_HELMET;
            case 3 -> Material.CHAINMAIL_HELMET;
            default -> Material.LEATHER_HELMET;
        };
        Object medal = switch (rank) {
            case 1 -> "#1 ";
            case 2 -> "#2 ";
            case 3 -> "#3 ";
            default -> "#" + rank + " ";
        };
        return ItemBuilder.of(material).name(rankColor + (String)medal + "<yellow>" + entry.playerName()).lore("", "<gray>" + this.currentType.getDisplayName() + ": <white>" + entry.value()).hideFlags().bedrockSafe().build();
    }

    private ItemStack createEmptyEntryItem(int rank) {
        return ItemBuilder.of(Material.GRAY_DYE).name("<dark_gray>#" + rank + " - Empty").lore("", "<gray>No player yet").bedrockSafe().build();
    }

    private Material getMaterialForType(LeaderboardType type) {
        return switch (type) {
            default -> throw new MatchException(null, null);
            case LeaderboardType.WINS -> Material.GOLDEN_SWORD;
            case LeaderboardType.KILLS -> Material.DIAMOND_SWORD;
            case LeaderboardType.WINSTREAK -> Material.BLAZE_POWDER;
        };
    }
}

