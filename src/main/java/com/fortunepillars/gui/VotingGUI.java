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
import com.fortunepillars.arena.Arena;
import com.fortunepillars.arena.GameState;
import com.fortunepillars.game.GameModeType;
import com.fortunepillars.game.loot.LootMode;
import com.fortunepillars.gui.GUI;
import com.fortunepillars.gui.MainMenuGUI;
import com.fortunepillars.utils.ItemBuilder;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class VotingGUI
extends GUI {
    private final Arena arena;
    private static final Map<UUID, LootDraft> DRAFTS = new ConcurrentHashMap<UUID, LootDraft>();
    private static final int GM_HDR = 0;
    private static final int GM_NORMAL = 1;
    private static final int GM_BALANCED = 2;
    private static final int GM_SWAPPER = 3;
    private static final int GM_SHUFFLE = 4;
    private static final int GM_LAVA = 5;
    private static final int GM_BORDER = 6;
    private static final int GM_SPEED = 7;
    private static final int GM_TNT = 10;
    private static final int STYLE_HDR = 18;
    private static final int STYLE_MULTI = 19;
    private static final int STYLE_SINGL = 20;
    private static final int TIER_HDR = 27;
    private static final int TIER_NORM = 28;
    private static final int TIER_BAL = 29;
    private static final int TIER_OP = 30;
    private static final int FREQ_HDR = 36;
    private static final int FREQ_FAST = 37;
    private static final int FREQ_MED = 38;
    private static final int FREQ_SLOW = 39;
    private static final int BTN_BACK = 45;
    private static final int BTN_RANDOM = 47;
    private static final int BTN_SUBMIT = 49;
    private static final int BTN_CLEAR = 51;
    private static final int[] SEP_SLOTS = new int[]{7, 16, 25, 34, 43, 52};
    private static final int PANEL_HDR = 8;
    private static final int PANEL_GM = 17;
    private static final int PANEL_STYLE = 26;
    private static final int PANEL_TIER = 35;
    private static final int PANEL_FREQ = 44;
    private static final int PANEL_RESULT = 53;

    public VotingGUI(FortunePillars plugin, Player player, Arena arena) {
        super(plugin, player, "<gradient:#FFD700:#FFA500>\u2726 Fortune Pillars \u2014 Vote \u2726</gradient>", 54);
        this.arena = arena;
        if (!DRAFTS.containsKey(player.getUniqueId())) {
            LootMode existing = this.safeLootVote();
            if (existing != null) {
                DRAFTS.put(player.getUniqueId(), LootDraft.from(existing));
            } else {
                DRAFTS.put(player.getUniqueId(), new LootDraft());
            }
        }
    }

    @Override
    public void setup() {
        this.fill(Material.GRAY_STAINED_GLASS_PANE);
        if (!this.plugin.getConfigManager().isVotingEnabled()) {
            this.showError("<red><bold>Voting is Disabled!</bold></red>", "<gray>The server admin has disabled the voting system.", "<gray>The game will start with the default settings.");
            return;
        }
        if (this.arena == null) {
            this.showError("<red><bold>Not in an Arena!</bold></red>", "<gray>Join an arena first with <yellow>/tof join");
            return;
        }
        if (!this.votingAllowed()) {
            this.showError("<red><bold>Voting Closed!</bold></red>", "<gray>Voting is only open during the waiting phase.", "<gray>Current state: " + this.arena.getState().getColoredName());
            return;
        }
        GameModeType gVote = this.safeGameVote();
        LootDraft draft = this.getDraft();
        LootMode lVote = this.resolveFromDraft(draft);
        this.buildSeparators();
        this.buildGameModeSection(gVote);
        this.buildStyleSection(draft);
        this.buildTierSection(draft);
        this.buildFreqSection(draft);
        this.buildRightPanel(gVote, draft, lVote);
        this.buildBottomBar(gVote, lVote);
    }

    private void buildSeparators() {
        ItemStack sep = ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").bedrockSafe().build();
        for (int s : SEP_SLOTS) {
            this.setItem(s, sep);
        }
    }

    private void buildGameModeSection(GameModeType sel) {
        this.setItem(0, ItemBuilder.of(Material.IRON_SWORD).name("<yellow><bold>\u2694 Game Mode</bold></yellow>").lore("", "<gray>Pick the game type.", "<gray>Click again to deselect.").hideFlags().bedrockSafe().build());
        this.setItem(1, this.gmItem(GameModeType.NORMAL, sel), e -> this.clickGame(GameModeType.NORMAL));
        this.setItem(2, this.gmItem(GameModeType.BALANCED, sel), e -> this.clickGame(GameModeType.BALANCED));
        this.setItem(3, this.gmItem(GameModeType.SWAPPER, sel), e -> this.clickGame(GameModeType.SWAPPER));
        this.setItem(4, this.gmItem(GameModeType.SHUFFLE, sel), e -> this.clickGame(GameModeType.SHUFFLE));
        this.setItem(5, this.gmItem(GameModeType.LAVA_RISING, sel), e -> this.clickGame(GameModeType.LAVA_RISING));
        this.setItem(6, this.gmItem(GameModeType.BORDER_SHRINK, sel), e -> this.clickGame(GameModeType.BORDER_SHRINK));
        this.setItem(7, this.gmItem(GameModeType.SPEED_UHC, sel), e -> this.clickGame(GameModeType.SPEED_UHC));
        this.setItem(10, this.gmItem(GameModeType.TNT_RAIN, sel), e -> this.clickGame(GameModeType.TNT_RAIN));
    }

    private void buildStyleSection(LootDraft draft) {
        this.setItem(18, ItemBuilder.of(Material.CHEST).name("<aqua><bold>\ud83d\udce6 Drop Style</bold></aqua>").lore("", "<gray>Multi = items drop in stacks", "<gray>Single = exactly 1 item per drop").bedrockSafe().build());
        this.setItem(19, this.styleItem(LootMode.DropStyle.MULTI, draft.style), e -> this.clickStyle(LootMode.DropStyle.MULTI));
        this.setItem(20, this.styleItem(LootMode.DropStyle.SINGLE, draft.style), e -> this.clickStyle(LootMode.DropStyle.SINGLE));
    }

    private void buildTierSection(LootDraft draft) {
        this.setItem(27, ItemBuilder.of(Material.DIAMOND).name("<gold><bold>\u2696 Loot Tier</bold></gold>").lore("", "<gray>Quality of loot that drops.").hideFlags().bedrockSafe().build());
        this.setItem(28, this.tierItem(LootMode.LootTier.NORMAL, draft.tier), e -> this.clickTier(LootMode.LootTier.NORMAL));
        this.setItem(29, this.tierItem(LootMode.LootTier.BALANCED, draft.tier), e -> this.clickTier(LootMode.LootTier.BALANCED));
        this.setItem(30, this.tierItem(LootMode.LootTier.OP, draft.tier), e -> this.clickTier(LootMode.LootTier.OP));
    }

    private void buildFreqSection(LootDraft draft) {
        this.setItem(36, ItemBuilder.of(Material.CLOCK).name("<light_purple><bold>\u23f1 Drop Speed</bold></light_purple>").lore("", "<gray>How often loot falls from the sky.").bedrockSafe().build());
        this.setItem(37, this.freqItem(FreqBucket.FAST, draft.freq), e -> this.clickFreq(FreqBucket.FAST));
        this.setItem(38, this.freqItem(FreqBucket.MEDIUM, draft.freq), e -> this.clickFreq(FreqBucket.MEDIUM));
        this.setItem(39, this.freqItem(FreqBucket.SLOW, draft.freq), e -> this.clickFreq(FreqBucket.SLOW));
    }

    private void buildRightPanel(GameModeType gVote, LootDraft draft, LootMode lVote) {
        this.setItem(8, ItemBuilder.of(Material.NETHER_STAR).name("<gold><bold>\u2726 Your Votes \u2726</bold></gold>").lore("", "<gray>Your current selections.", "<gray>Click any to clear it.").glow().bedrockSafe().build());
        this.setItem(17, this.panelGameMode(gVote), e -> {
            if (gVote != null) {
                this.plugin.getVoteManager().removeVote(this.player, this.arena);
                this.msg("<yellow>Game mode vote cleared.");
                this.sfx(Sound.ENTITY_ITEM_PICKUP);
                this.reopen();
            }
        });
        this.setItem(26, this.panelStyle(draft.style), e -> {
            if (draft.style != null) {
                this.getDraft().style = null;
                this.syncLootVote();
                this.msg("<yellow>Drop style cleared.");
                this.sfx(Sound.ENTITY_ITEM_PICKUP);
                this.reopen();
            }
        });
        this.setItem(35, this.panelTier(draft.tier), e -> {
            if (draft.tier != null) {
                this.getDraft().tier = null;
                this.syncLootVote();
                this.msg("<yellow>Loot tier cleared.");
                this.sfx(Sound.ENTITY_ITEM_PICKUP);
                this.reopen();
            }
        });
        this.setItem(44, this.panelFreq(draft.freq), e -> {
            if (draft.freq != null) {
                this.getDraft().freq = null;
                this.syncLootVote();
                this.msg("<yellow>Drop speed cleared.");
                this.sfx(Sound.ENTITY_ITEM_PICKUP);
                this.reopen();
            }
        });
        this.setItem(53, this.panelResult(draft, lVote));
    }

    private void buildBottomBar(GameModeType gVote, LootMode lVote) {
        boolean ready = gVote != null && lVote != null;
        this.setItem(45, ItemBuilder.of(Material.ARROW).name("<yellow><bold>\u2190 Back</bold></yellow>").lore("", "<gray>Return to main menu.").bedrockSafe().build(), e -> {
            this.sfx(Sound.UI_BUTTON_CLICK);
            DRAFTS.remove(this.player.getUniqueId());
            new MainMenuGUI(this.plugin, this.player).open();
        });
        this.setItem(47, ItemBuilder.of(Material.ENDER_EYE).name("<light_purple><bold>\u2047 Random All</bold></light_purple>").lore("", "<gray>Randomly pick all options!").glow().bedrockSafe().build(), e -> this.doRandom());
        if (ready) {
            this.setItem(49, ItemBuilder.of(Material.EMERALD).name("<green><bold>\u2714 Submit Vote</bold></green>").lore("", "<gray>Game:  <yellow>" + gVote.getDisplayName(), "<gray>Loot:  " + lVote.getColoredDisplayName(), "", "<green>Click to confirm and close!").glow().bedrockSafe().build(), e -> {
                this.sfx(Sound.ENTITY_PLAYER_LEVELUP);
                this.msg("<green><bold>\u2714 Vote submitted! Good luck!");
                DRAFTS.remove(this.player.getUniqueId());
                this.player.closeInventory();
            });
        } else {
            this.setItem(49, ItemBuilder.of(Material.GRAY_DYE).name("<gray><bold>Submit Vote</bold></gray>").lore(new String[]{"", "<red>Still missing:", gVote == null ? "<red>  \u2717 Game Mode" : "<green>  \u2714 " + gVote.getDisplayName(), lVote == null ? "<red>  \u2717 Style + Tier + Speed" : "<green>  \u2714 " + lVote.getColoredDisplayName()}).bedrockSafe().build());
        }
        this.setItem(51, ItemBuilder.of(Material.BUCKET).name("<red><bold>\u2717 Clear All</bold></red>").lore("", "<gray>Remove all your votes.").bedrockSafe().build(), e -> this.doClearAll());
    }

    private ItemStack gmItem(GameModeType mode, GameModeType sel) {
        boolean on = mode == sel;
        int votes = 0;
        try {
            votes = this.plugin.getVoteManager().getVoteCount(this.arena, mode);
        }
        catch (Exception exception) {
            // empty catch block
        }
        return ItemBuilder.of(this.safeGmIcon(mode)).name((on ? "<green>\u2714 " : "") + "<yellow>" + mode.getDisplayName()).lore("", "<gray>" + mode.getDescription(), "", "<dark_gray>Arena votes: <gray>" + votes, "", on ? "<green>\u2714 Selected  <dark_gray>| <red>click to remove" : "<yellow>Click to vote!").glow(on).hideFlags().bedrockSafe().build();
    }

    private ItemStack styleItem(LootMode.DropStyle style, LootMode.DropStyle sel) {
        boolean on = style == sel;
        boolean multi = style == LootMode.DropStyle.MULTI;
        Material icon = multi ? Material.CHEST : Material.DROPPER;
        String label = multi ? "<aqua>\ud83d\udce6 Multi-Item" : "<white>1\ufe0f\u20e3 Single-Item";
        String desc = multi ? "<gray>Items drop in stacks (2\u201364)" : "<gray>Exactly 1 item per drop";
        int votes = 0;
        try {
            votes = this.plugin.getLootModeVoteManager().getStyleVotes(this.arena, style);
        }
        catch (Exception exception) {
            // empty catch block
        }
        return ItemBuilder.of(icon).name((on ? "<green>\u2714 " : "") + label).lore("", desc, "", "<dark_gray>Arena votes: <gray>" + votes, "", on ? "<green>\u2714 Selected  <dark_gray>| <red>click to remove" : "<yellow>Click to select!").glow(on).bedrockSafe().build();
    }

    private ItemStack tierItem(LootMode.LootTier tier, LootMode.LootTier sel) {
        boolean on = tier == sel;
        Material icon = switch (tier) {
            default -> throw new MatchException(null, null);
            case LootMode.LootTier.NORMAL -> Material.IRON_INGOT;
            case LootMode.LootTier.BALANCED -> Material.GOLD_INGOT;
            case LootMode.LootTier.OP -> Material.DIAMOND;
        };
        String label = switch (tier) {
            default -> throw new MatchException(null, null);
            case LootMode.LootTier.NORMAL -> "<white>Normal";
            case LootMode.LootTier.BALANCED -> "<aqua>Balanced";
            case LootMode.LootTier.OP -> "<gold>OP";
        };
        String desc = switch (tier) {
            default -> throw new MatchException(null, null);
            case LootMode.LootTier.NORMAL -> "<gray>All basic items";
            case LootMode.LootTier.BALANCED -> "<gray>Mid-tier gear only";
            case LootMode.LootTier.OP -> "<gray>Powerful, rare items";
        };
        int votes = 0;
        try {
            votes = this.plugin.getLootModeVoteManager().getTierVotes(this.arena, tier);
        }
        catch (Exception exception) {
            // empty catch block
        }
        return ItemBuilder.of(icon).name((on ? "<green>\u2714 " : "") + label).lore("", desc, "", "<dark_gray>Arena votes: <gray>" + votes, "", on ? "<green>\u2714 Selected  <dark_gray>| <red>click to remove" : "<yellow>Click to select!").glow(on).hideFlags().bedrockSafe().build();
    }

    private ItemStack freqItem(FreqBucket freq, FreqBucket sel) {
        boolean on = freq == sel;
        Material icon = switch (freq.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> Material.BLAZE_POWDER;
            case 1 -> Material.CLOCK;
            case 2 -> Material.COBWEB;
        };
        String label = switch (freq.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "<red>\u26a1 Fast";
            case 1 -> "<yellow>\u25c6 Medium";
            case 2 -> "<gray>\ud83d\udc22 Slow";
        };
        String interval = switch (freq.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "Every 10 seconds";
            case 1 -> "Every 20 seconds";
            case 2 -> "Every 30 seconds";
        };
        return ItemBuilder.of(icon).name((on ? "<green>\u2714 " : "") + label).lore("", "<gray>" + interval, "", on ? "<green>\u2714 Selected  <dark_gray>| <red>click to remove" : "<yellow>Click to select!").glow(on).bedrockSafe().build();
    }

    private ItemStack panelGameMode(GameModeType v) {
        if (v == null) {
            return this.emptyPanel("<yellow>\u2694 Game Mode", "No vote \u2014 pick one on the left");
        }
        return ItemBuilder.of(this.safeGmIcon(v)).name("<green>\u2714 <yellow>" + v.getDisplayName()).lore("", "<gray>" + v.getDescription(), "", "<red>Click to clear.").glow().hideFlags().bedrockSafe().build();
    }

    private ItemStack panelStyle(LootMode.DropStyle v) {
        if (v == null) {
            return this.emptyPanel("<aqua>\ud83d\udce6 Drop Style", "No vote \u2014 pick Multi or Single");
        }
        boolean multi = v == LootMode.DropStyle.MULTI;
        return ItemBuilder.of(multi ? Material.CHEST : Material.DROPPER).name("<green>\u2714 " + (multi ? "<aqua>\ud83d\udce6 Multi-Item" : "<white>1\ufe0f\u20e3 Single-Item")).lore("", multi ? "<gray>Items drop in stacks" : "<gray>1 item per drop", "", "<red>Click to clear.").glow().bedrockSafe().build();
    }

    private ItemStack panelTier(LootMode.LootTier v) {
        if (v == null) {
            return this.emptyPanel("<gold>\u2696 Loot Tier", "No vote \u2014 pick Normal/Balanced/OP");
        }
        Material icon = switch (v) {
            default -> throw new MatchException(null, null);
            case LootMode.LootTier.NORMAL -> Material.IRON_INGOT;
            case LootMode.LootTier.BALANCED -> Material.GOLD_INGOT;
            case LootMode.LootTier.OP -> Material.DIAMOND;
        };
        String name = switch (v) {
            default -> throw new MatchException(null, null);
            case LootMode.LootTier.NORMAL -> "<white>Normal";
            case LootMode.LootTier.BALANCED -> "<aqua>Balanced";
            case LootMode.LootTier.OP -> "<gold>OP";
        };
        return ItemBuilder.of(icon).name("<green>\u2714 " + name).lore("", "<red>Click to clear.").glow().hideFlags().bedrockSafe().build();
    }

    private ItemStack panelFreq(FreqBucket v) {
        if (v == null) {
            return this.emptyPanel("<light_purple>\u23f1 Drop Speed", "No vote \u2014 pick Fast/Medium/Slow");
        }
        Material icon = switch (v.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> Material.BLAZE_POWDER;
            case 1 -> Material.CLOCK;
            case 2 -> Material.COBWEB;
        };
        String name = switch (v.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "<red>\u26a1 Fast (10s)";
            case 1 -> "<yellow>\u25c6 Medium (20s)";
            case 2 -> "<gray>\ud83d\udc22 Slow (30s)";
        };
        return ItemBuilder.of(icon).name("<green>\u2714 " + name).lore("", "<red>Click to clear.").glow().bedrockSafe().build();
    }

    private ItemStack panelResult(LootDraft draft, LootMode loot) {
        if (loot == null) {
            return ItemBuilder.of(Material.GRAY_DYE).name("<gray>Loot Result").lore(new String[]{"", draft.style == null ? "<red>  \u2717 Drop Style" : "<green>  \u2714 Style: " + (draft.style == LootMode.DropStyle.MULTI ? "Multi" : "Single"), draft.tier == null ? "<red>  \u2717 Loot Tier" : "<green>  \u2714 Tier: " + draft.tier.name(), draft.freq == null ? "<red>  \u2717 Drop Speed" : "<green>  \u2714 Speed: " + draft.freq.name(), "", "<dark_gray>Pick all three to complete!"}).bedrockSafe().build();
        }
        return ItemBuilder.of(Material.EMERALD).name("<green><bold>\u2714 " + loot.getColoredDisplayName()).lore("", "<gray>" + loot.getDescription(), "", "<yellow>Interval: <white>" + loot.getMinIntervalSeconds() + "s", "<yellow>Items/drop: <white>" + loot.getMinItems() + "\u2013" + loot.getMaxItems()).glow().bedrockSafe().build();
    }

    private ItemStack emptyPanel(String name, String hint) {
        return ItemBuilder.of(Material.GRAY_DYE).name(name).lore("", "<dark_gray>" + hint).bedrockSafe().build();
    }

    private void clickGame(GameModeType mode) {
        if (!this.guardVoting()) {
            return;
        }
        GameModeType cur = this.safeGameVote();
        if (cur == mode) {
            this.plugin.getVoteManager().removeVote(this.player, this.arena);
            this.msg("<yellow>Game mode vote removed.");
        } else {
            try {
                this.plugin.getVoteManager().castVote(this.player, this.arena, mode);
                this.msg("<green>\u2714 Game mode: <gold>" + mode.getDisplayName());
            }
            catch (Exception e) {
                this.msg("<red>Error voting. Try /tof vote " + mode.name().toLowerCase());
                return;
            }
        }
        this.sfx(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        this.reopen();
    }

    private void clickStyle(LootMode.DropStyle style) {
        if (!this.guardVoting()) {
            return;
        }
        LootDraft draft = this.getDraft();
        if (draft.style == style) {
            draft.style = null;
            this.msg("<yellow>Drop style removed.");
        } else {
            draft.style = style;
            this.msg("<green>\u2714 Style: <aqua>" + (style == LootMode.DropStyle.MULTI ? "Multi-Item" : "Single-Item"));
        }
        this.syncLootVote();
        this.sfx(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        this.reopen();
    }

    private void clickTier(LootMode.LootTier tier) {
        if (!this.guardVoting()) {
            return;
        }
        LootDraft draft = this.getDraft();
        if (draft.tier == tier) {
            draft.tier = null;
            this.msg("<yellow>Loot tier removed.");
        } else {
            draft.tier = tier;
            String n = switch (tier) {
                default -> throw new MatchException(null, null);
                case LootMode.LootTier.NORMAL -> "<white>Normal";
                case LootMode.LootTier.BALANCED -> "<aqua>Balanced";
                case LootMode.LootTier.OP -> "<gold>OP";
            };
            this.msg("<green>\u2714 Tier: " + n);
        }
        this.syncLootVote();
        this.sfx(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        this.reopen();
    }

    private void clickFreq(FreqBucket freq) {
        if (!this.guardVoting()) {
            return;
        }
        LootDraft draft = this.getDraft();
        if (draft.freq == freq) {
            draft.freq = null;
            this.msg("<yellow>Drop speed removed.");
        } else {
            draft.freq = freq;
            String n = switch (freq.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> "<red>Fast (10s)";
                case 1 -> "<yellow>Medium (20s)";
                case 2 -> "<gray>Slow (30s)";
            };
            this.msg("<green>\u2714 Speed: " + n);
        }
        this.syncLootVote();
        this.sfx(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        this.reopen();
    }

    private void doClearAll() {
        this.plugin.getVoteManager().removeVote(this.player, this.arena);
        DRAFTS.put(this.player.getUniqueId(), new LootDraft());
        this.syncLootVote();
        this.msg("<aqua>All votes cleared!");
        this.sfx(Sound.ENTITY_ITEM_PICKUP);
        this.reopen();
    }

    private void doRandom() {
        if (!this.guardVoting()) {
            return;
        }
        try {
            GameModeType[] allModes = GameModeType.values();
            this.plugin.getVoteManager().castVote(this.player, this.arena, allModes[(int)(Math.random() * (double)allModes.length)]);
            LootDraft draft = this.getDraft();
            LootMode.DropStyle[] styles = LootMode.DropStyle.values();
            LootMode.LootTier[] tiers = LootMode.LootTier.values();
            FreqBucket[] freqs = FreqBucket.values();
            draft.style = styles[(int)(Math.random() * (double)styles.length)];
            draft.tier = tiers[(int)(Math.random() * (double)tiers.length)];
            draft.freq = freqs[(int)(Math.random() * (double)freqs.length)];
            this.syncLootVote();
            LootMode result = this.resolveFromDraft(draft);
            this.msg("<light_purple>Random votes placed!" + (String)(result != null ? " Loot: " + result.getColoredDisplayName() : ""));
            this.sfx(Sound.ENTITY_ENDERMAN_TELEPORT);
            this.reopen();
        }
        catch (Exception e) {
            this.msg("<red>Error with random vote!");
        }
    }

    private void reopen() {
        this.plugin.getGuiManager().suppressNextClose(this.player);
        this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> new VotingGUI(this.plugin, this.player, this.arena).open(), 1L);
    }

    private void syncLootVote() {
        LootMode resolved;
        try {
            this.plugin.getLootModeVoteManager().removeVote(this.player, this.arena);
        }
        catch (Exception exception) {
            // empty catch block
        }
        LootDraft draft = this.getDraft();
        if (draft.isComplete() && (resolved = this.resolve(draft.style, draft.tier, draft.freq)) != null) {
            try {
                this.plugin.getLootModeVoteManager().castVote(this.player, this.arena, resolved);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    private LootMode resolveFromDraft(LootDraft draft) {
        if (!draft.isComplete()) {
            return null;
        }
        return this.resolve(draft.style, draft.tier, draft.freq);
    }

    private LootMode resolve(LootMode.DropStyle style, LootMode.LootTier tier, FreqBucket freq) {
        String name = style.name() + "_" + tier.name() + "_" + freq.name();
        try {
            return LootMode.valueOf(name);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            for (LootMode m : LootMode.values()) {
                if (m.getStyle() != style || m.getTier() != tier || FreqBucket.fromSeconds(m.getMinIntervalSeconds()) != freq) continue;
                return m;
            }
            return null;
        }
    }

    private LootDraft getDraft() {
        return DRAFTS.computeIfAbsent(this.player.getUniqueId(), k -> new LootDraft());
    }

    private boolean votingAllowed() {
        if (this.arena == null) {
            return false;
        }
        GameState s = this.arena.getState();
        return s == GameState.WAITING || s == GameState.STARTING;
    }

    private boolean guardVoting() {
        if (!this.votingAllowed()) {
            this.msg("<red>Voting is no longer available!");
            this.sfx(Sound.ENTITY_VILLAGER_NO);
            return false;
        }
        return true;
    }

    private GameModeType safeGameVote() {
        try {
            return this.plugin.getVoteManager().getPlayerVote(this.player, this.arena);
        }
        catch (Exception e) {
            return null;
        }
    }

    private LootMode safeLootVote() {
        try {
            return this.plugin.getLootModeVoteManager().getPlayerVote(this.player, this.arena);
        }
        catch (Exception e) {
            return null;
        }
    }

    private Material safeGmIcon(GameModeType mode) {
        try {
            Material m = mode.getIcon();
            if (m != null && m != Material.AIR) {
                return m;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return Material.PAPER;
    }

    private ItemStack glass() {
        return ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").bedrockSafe().build();
    }

    private void msg(String miniMsg) {
        this.player.sendMessage(FortunePillars.parseWithPrefix(miniMsg));
    }

    private void sfx(Sound s) {
        this.player.playSound(this.player.getLocation(), s, 0.5f, 1.0f);
    }

    private void showError(String ... lines) {
        String title = lines[0];
        String[] lore = new String[lines.length - 1];
        System.arraycopy(lines, 1, lore, 0, lore.length);
        this.setItem(22, ItemBuilder.of(Material.BARRIER).name(title).lore(lore).bedrockSafe().build());
        this.setItem(49, ItemBuilder.of(Material.ARROW).name("<yellow>Close").bedrockSafe().build(), e -> this.player.closeInventory());
    }

    public static void clearDraft(UUID playerUUID) {
        DRAFTS.remove(playerUUID);
    }

    public static class LootDraft {
        public LootMode.DropStyle style;
        public LootMode.LootTier tier;
        public FreqBucket freq;

        public LootDraft() {
        }

        public LootDraft(LootMode.DropStyle style, LootMode.LootTier tier, FreqBucket freq) {
            this.style = style;
            this.tier = tier;
            this.freq = freq;
        }

        public boolean isComplete() {
            return this.style != null && this.tier != null && this.freq != null;
        }

        public static LootDraft from(LootMode mode) {
            if (mode == null) {
                return new LootDraft();
            }
            return new LootDraft(mode.getStyle(), mode.getTier(), FreqBucket.fromSeconds(mode.getMinIntervalSeconds()));
        }
    }

    public static enum FreqBucket {
        FAST,
        MEDIUM,
        SLOW;


        public static FreqBucket fromSeconds(int seconds) {
            if (seconds <= 10) {
                return FAST;
            }
            if (seconds >= 30) {
                return SLOW;
            }
            return MEDIUM;
        }
    }
}

