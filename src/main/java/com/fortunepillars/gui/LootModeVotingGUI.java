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
import com.fortunepillars.game.loot.LootMode;
import com.fortunepillars.gui.GUI;
import com.fortunepillars.gui.VotingGUI;
import com.fortunepillars.utils.ItemBuilder;
import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LootModeVotingGUI
extends GUI {
    private final Arena arena;

    public LootModeVotingGUI(FortunePillars plugin, Player player, Arena arena) {
        super(plugin, player, "<gradient:#00FFFF:#0080FF>Loot Drop System</gradient>", 54);
        this.arena = arena;
    }

    @Override
    public void setup() {
        this.fill(Material.CYAN_STAINED_GLASS_PANE);
        if (this.arena == null) {
            this.setItem(22, ItemBuilder.of(Material.BARRIER).name("<red>Not in an Arena!</red>").lore("", "<gray>You must join an arena", "<gray>before you can vote.").bedrockSafe().build());
            this.setItem(49, ItemBuilder.of(Material.ARROW).name("<yellow>Close").bedrockSafe().build(), event -> this.player.closeInventory());
            return;
        }
        if (!this.isVotingAllowed()) {
            this.setItem(22, ItemBuilder.of(Material.CLOCK).name("<red>Voting Closed!</red>").lore("", "<gray>Voting is only available during the waiting phase.").bedrockSafe().build());
            this.setItem(49, ItemBuilder.of(Material.ARROW).name("<yellow>Close").bedrockSafe().build(), event -> this.player.closeInventory());
            return;
        }
        LootMode playerVote = this.plugin.getLootModeVoteManager().getPlayerVote(this.player, this.arena);
        this.setItem(4, ItemBuilder.of(Material.CHEST).name("<gradient:#FFD700:#FFA500>Loot Drop System</gradient>").lore("", "<gray>Choose your loot style!", "", "<gray>Current vote: " + (playerVote != null ? playerVote.getColoredDisplayName() : "<dark_gray>None")).glow().bedrockSafe().build());
        this.setItem(9, ItemBuilder.of(Material.SHULKER_BOX).name("<gold><bold>MULTI-ITEM MODE</bold></gold>").lore("", "<gray>Items drop in <white><bold>STACKS</bold></white>").glow().bedrockSafe().build());
        this.setItem(18, ItemBuilder.of(Material.DROPPER).name("<aqua><bold>SINGLE-ITEM MODE</bold></aqua>").lore("", "<gray>Items drop as <white><bold>1x ONLY</bold></white>").glow().bedrockSafe().build());
        this.setItem(10, this.createLootModeItem(LootMode.MULTI_NORMAL_FAST, playerVote), event -> this.voteLootMode(LootMode.MULTI_NORMAL_FAST));
        this.setItem(11, this.createLootModeItem(LootMode.MULTI_NORMAL_MEDIUM, playerVote), event -> this.voteLootMode(LootMode.MULTI_NORMAL_MEDIUM));
        this.setItem(12, this.createLootModeItem(LootMode.MULTI_NORMAL_SLOW, playerVote), event -> this.voteLootMode(LootMode.MULTI_NORMAL_SLOW));
        this.setItem(14, this.createLootModeItem(LootMode.MULTI_BALANCED_FAST, playerVote), event -> this.voteLootMode(LootMode.MULTI_BALANCED_FAST));
        this.setItem(15, this.createLootModeItem(LootMode.MULTI_BALANCED_MEDIUM, playerVote), event -> this.voteLootMode(LootMode.MULTI_BALANCED_MEDIUM));
        this.setItem(16, this.createLootModeItem(LootMode.MULTI_BALANCED_SLOW, playerVote), event -> this.voteLootMode(LootMode.MULTI_BALANCED_SLOW));
        this.setItem(19, this.createLootModeItem(LootMode.MULTI_OP_FAST, playerVote), event -> this.voteLootMode(LootMode.MULTI_OP_FAST));
        this.setItem(20, this.createLootModeItem(LootMode.MULTI_OP_MEDIUM, playerVote), event -> this.voteLootMode(LootMode.MULTI_OP_MEDIUM));
        this.setItem(21, this.createLootModeItem(LootMode.MULTI_OP_SLOW, playerVote), event -> this.voteLootMode(LootMode.MULTI_OP_SLOW));
        this.setItem(28, this.createLootModeItem(LootMode.SINGLE_NORMAL_FAST, playerVote), event -> this.voteLootMode(LootMode.SINGLE_NORMAL_FAST));
        this.setItem(29, this.createLootModeItem(LootMode.SINGLE_NORMAL_MEDIUM, playerVote), event -> this.voteLootMode(LootMode.SINGLE_NORMAL_MEDIUM));
        this.setItem(30, this.createLootModeItem(LootMode.SINGLE_NORMAL_SLOW, playerVote), event -> this.voteLootMode(LootMode.SINGLE_NORMAL_SLOW));
        this.setItem(32, this.createLootModeItem(LootMode.SINGLE_BALANCED_FAST, playerVote), event -> this.voteLootMode(LootMode.SINGLE_BALANCED_FAST));
        this.setItem(33, this.createLootModeItem(LootMode.SINGLE_BALANCED_MEDIUM, playerVote), event -> this.voteLootMode(LootMode.SINGLE_BALANCED_MEDIUM));
        this.setItem(34, this.createLootModeItem(LootMode.SINGLE_BALANCED_SLOW, playerVote), event -> this.voteLootMode(LootMode.SINGLE_BALANCED_SLOW));
        this.setItem(37, this.createLootModeItem(LootMode.SINGLE_OP_FAST, playerVote), event -> this.voteLootMode(LootMode.SINGLE_OP_FAST));
        this.setItem(38, this.createLootModeItem(LootMode.SINGLE_OP_MEDIUM, playerVote), event -> this.voteLootMode(LootMode.SINGLE_OP_MEDIUM));
        this.setItem(39, this.createLootModeItem(LootMode.SINGLE_OP_SLOW, playerVote), event -> this.voteLootMode(LootMode.SINGLE_OP_SLOW));
        this.setItem(13, this.createVoteSummaryItem());
        this.setItem(46, ItemBuilder.of(Material.ENDER_PEARL).name("<light_purple><bold>Random Vote</bold></light_purple>").lore("", "<gray>Vote for a random loot mode!").glow().bedrockSafe().build(), event -> this.voteRandom());
        this.setItem(48, ItemBuilder.of(Material.WATER_BUCKET).name("<aqua><bold>Clear Vote</bold></aqua>").lore("", "<gray>Remove your loot mode vote").bedrockSafe().build(), event -> this.clearVote());
        this.setItem(45, ItemBuilder.of(Material.ARROW).name("<yellow><bold>\u2190 Back to Vote Menu</bold></yellow>").lore("", "<gray>Return to game mode voting").bedrockSafe().build(), event -> {
            this.player.playSound(this.player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            new VotingGUI(this.plugin, this.player, this.arena).open();
        });
        this.setItem(53, ItemBuilder.of(Material.BARRIER).name("<red><bold>Close</bold></red>").bedrockSafe().build(), event -> this.player.closeInventory());
    }

    private ItemStack createLootModeItem(LootMode mode, LootMode playerVote) {
        boolean isSelected = mode == playerVote;
        int votes = this.plugin.getLootModeVoteManager().getVoteCount(this.arena, mode);
        String prefix = isSelected ? "<green>\u2713 " : "";
        Material icon = switch (mode) {
            default -> throw new MatchException(null, null);
            case LootMode.MULTI_NORMAL_FAST -> Material.HOPPER;
            case LootMode.MULTI_NORMAL_MEDIUM -> Material.CHEST;
            case LootMode.MULTI_NORMAL_SLOW -> Material.BARREL;
            case LootMode.MULTI_BALANCED_FAST -> Material.IRON_BLOCK;
            case LootMode.MULTI_BALANCED_MEDIUM -> Material.IRON_INGOT;
            case LootMode.MULTI_BALANCED_SLOW -> Material.IRON_ORE;
            case LootMode.MULTI_OP_FAST -> Material.DIAMOND_BLOCK;
            case LootMode.MULTI_OP_MEDIUM -> Material.DIAMOND;
            case LootMode.MULTI_OP_SLOW -> Material.EMERALD_BLOCK;
            case LootMode.SINGLE_NORMAL_FAST -> Material.DROPPER;
            case LootMode.SINGLE_NORMAL_MEDIUM -> Material.DISPENSER;
            case LootMode.SINGLE_NORMAL_SLOW -> Material.COBWEB;
            case LootMode.SINGLE_BALANCED_FAST -> Material.IRON_SWORD;
            case LootMode.SINGLE_BALANCED_MEDIUM -> Material.IRON_CHESTPLATE;
            case LootMode.SINGLE_BALANCED_SLOW -> Material.IRON_HELMET;
            case LootMode.SINGLE_OP_FAST -> Material.DIAMOND_SWORD;
            case LootMode.SINGLE_OP_MEDIUM -> Material.DIAMOND_CHESTPLATE;
            case LootMode.SINGLE_OP_SLOW -> Material.NETHERITE_INGOT;
        };
        ArrayList<Object> lore = new ArrayList<Object>();
        lore.add("");
        lore.add("<gray>" + mode.getDescription());
        lore.add("");
        lore.add("<yellow>Interval: <white>" + mode.getMinIntervalSeconds() + "s");
        lore.add("<yellow>Items: <white>" + mode.getMinItems() + "-" + mode.getMaxItems());
        lore.add("");
        lore.add("<gray>Votes: <white>" + votes);
        lore.add("");
        lore.add(isSelected ? "<green>Selected!" : "<yellow>Click to vote!");
        return ItemBuilder.of(icon).name(prefix + mode.getColoredDisplayName()).lore(lore.toArray(new String[0])).glow(isSelected).hideFlags().bedrockSafe().build();
    }

    private ItemStack createVoteSummaryItem() {
        LootMode leadingMode = this.plugin.getLootModeVoteManager().getLeadingMode(this.arena);
        int totalVotes = this.plugin.getLootModeVoteManager().getTotalVotes(this.arena);
        ArrayList<Object> lore = new ArrayList<Object>();
        lore.add("");
        lore.add("<yellow>Total Votes: <white><bold>" + totalVotes + "</bold>");
        lore.add("");
        if (leadingMode != null) {
            int leadingVotes = this.plugin.getLootModeVoteManager().getVoteCount(this.arena, leadingMode);
            lore.add("<gold><bold>Leading Mode:</bold></gold>");
            lore.add(leadingMode.getColoredDisplayName());
            lore.add("<gray>with <white><bold>" + leadingVotes + "</bold> votes");
        } else {
            lore.add("<gray>No votes yet");
        }
        return ItemBuilder.of(Material.BOOK).name("<gradient:#FFD700:#FFA500>Vote Summary</gradient>").lore(lore.toArray(new String[0])).glow().bedrockSafe().build();
    }

    private void voteLootMode(LootMode mode) {
        if (!this.isVotingAllowed()) {
            this.player.sendMessage(FortunePillars.parseWithPrefix("<red>Voting is no longer available!"));
            this.player.playSound(this.player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }
        this.plugin.getLootModeVoteManager().castVote(this.player, this.arena, mode);
        this.player.sendMessage(FortunePillars.parseWithPrefix("<green>\u2713 Voted for " + mode.getColoredDisplayName() + "!"));
        this.player.playSound(this.player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
        this.refresh();
    }

    private void voteRandom() {
        if (!this.isVotingAllowed()) {
            this.player.sendMessage(FortunePillars.parseWithPrefix("<red>Voting is no longer available!"));
            this.player.playSound(this.player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }
        LootMode[] allModes = LootMode.values();
        LootMode randomMode = allModes[(int)(Math.random() * (double)allModes.length)];
        this.plugin.getLootModeVoteManager().castVote(this.player, this.arena, randomMode);
        this.player.sendMessage(FortunePillars.parseWithPrefix("<light_purple>Randomly voted for " + randomMode.getColoredDisplayName() + "!"));
        this.player.playSound(this.player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.5f);
        this.refresh();
    }

    private void clearVote() {
        LootMode currentVote = this.plugin.getLootModeVoteManager().getPlayerVote(this.player, this.arena);
        if (currentVote == null) {
            this.player.sendMessage(FortunePillars.parseWithPrefix("<yellow>You haven't voted yet!"));
            this.player.playSound(this.player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 0.8f);
            return;
        }
        this.plugin.getLootModeVoteManager().removeVote(this.player, this.arena);
        this.player.sendMessage(FortunePillars.parseWithPrefix("<aqua>Your vote has been cleared!"));
        this.player.playSound(this.player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);
        this.refresh();
    }

    private boolean isVotingAllowed() {
        if (this.arena == null) {
            return false;
        }
        GameState state = this.arena.getState();
        return state == GameState.WAITING || state == GameState.STARTING;
    }
}

