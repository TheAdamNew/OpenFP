/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 */
package com.fortunepillars.voting;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.game.loot.LootMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;

public class LootModeVoteManager {
    private final FortunePillars plugin;
    private final Map<String, Map<UUID, LootMode>> lootVotes;

    public LootModeVoteManager(FortunePillars plugin) {
        this.plugin = plugin;
        this.lootVotes = new ConcurrentHashMap<String, Map<UUID, LootMode>>();
    }

    public LootMode getDefaultLootMode() {
        String configValue = null;
        configValue = this.plugin.getConfigManager().getConfig().getString("loot.default-loot-mode");
        if (configValue == null || configValue.isEmpty()) {
            configValue = this.plugin.getConfigManager().getConfig().getString("loot.default-mode");
        }
        if (configValue == null || configValue.isEmpty()) {
            configValue = this.plugin.getConfigManager().getConfig().getString("game.default-loot-mode");
        }
        if (configValue == null || configValue.isEmpty()) {
            configValue = this.plugin.getConfigManager().getConfig().getString("default-loot-mode");
        }
        if (configValue == null || configValue.isEmpty()) {
            return LootMode.MULTI_NORMAL_MEDIUM;
        }
        configValue = configValue.toUpperCase().trim().replace("-", "_").replace(" ", "_");
        try {
            return LootMode.valueOf(configValue);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            LootMode mode;
            int n;
            int n2;
            LootMode[] lootModeArray;
            String remainder;
            if (configValue.equals("SINGLE")) {
                return LootMode.SINGLE_NORMAL_MEDIUM;
            }
            if (configValue.equals("MULTI")) {
                return LootMode.MULTI_NORMAL_MEDIUM;
            }
            if (configValue.equals("NORMAL")) {
                return LootMode.MULTI_NORMAL_MEDIUM;
            }
            if (configValue.equals("BALANCED")) {
                return LootMode.MULTI_BALANCED_MEDIUM;
            }
            if (configValue.equals("OP")) {
                return LootMode.MULTI_OP_MEDIUM;
            }
            if (configValue.startsWith("SINGLE_")) {
                remainder = configValue.substring(7);
                if (remainder.equals("NORMAL") || remainder.equals("NORMAL_MEDIUM")) {
                    return LootMode.SINGLE_NORMAL_MEDIUM;
                }
                if (remainder.equals("BALANCED") || remainder.equals("BALANCED_MEDIUM")) {
                    return LootMode.SINGLE_BALANCED_MEDIUM;
                }
                if (remainder.equals("OP") || remainder.equals("OP_MEDIUM")) {
                    return LootMode.SINGLE_OP_MEDIUM;
                }
                lootModeArray = LootMode.values();
                n2 = lootModeArray.length;
                for (n = 0; n < n2; ++n) {
                    mode = lootModeArray[n];
                    if (mode.getStyle() != LootMode.DropStyle.SINGLE || !mode.name().contains(remainder)) continue;
                    return mode;
                }
            }
            if (configValue.startsWith("MULTI_")) {
                remainder = configValue.substring(6);
                if (remainder.equals("NORMAL") || remainder.equals("NORMAL_MEDIUM")) {
                    return LootMode.MULTI_NORMAL_MEDIUM;
                }
                if (remainder.equals("BALANCED") || remainder.equals("BALANCED_MEDIUM")) {
                    return LootMode.MULTI_BALANCED_MEDIUM;
                }
                if (remainder.equals("OP") || remainder.equals("OP_MEDIUM")) {
                    return LootMode.MULTI_OP_MEDIUM;
                }
                lootModeArray = LootMode.values();
                n2 = lootModeArray.length;
                for (n = 0; n < n2; ++n) {
                    mode = lootModeArray[n];
                    if (mode.getStyle() != LootMode.DropStyle.MULTI || !mode.name().contains(remainder)) continue;
                    return mode;
                }
            }
            for (LootMode mode2 : LootMode.values()) {
                if (!mode2.name().contains(configValue) && !configValue.contains(mode2.name())) continue;
                return mode2;
            }
            return LootMode.MULTI_NORMAL_MEDIUM;
        }
    }

    public LootMode.DropStyle getDefaultDropStyle() {
        String styleValue = this.plugin.getConfigManager().getConfig().getString("loot.default-style");
        if (styleValue == null || styleValue.isEmpty()) {
            styleValue = this.plugin.getConfigManager().getConfig().getString("loot.drop-style");
        }
        if (styleValue == null || styleValue.isEmpty()) {
            return this.getDefaultLootMode().getStyle();
        }
        if ((styleValue = styleValue.toUpperCase().trim()).equals("SINGLE") || styleValue.equals("1") || styleValue.equals("ONE")) {
            return LootMode.DropStyle.SINGLE;
        }
        if (styleValue.equals("MULTI") || styleValue.equals("MULTIPLE") || styleValue.equals("MANY")) {
            return LootMode.DropStyle.MULTI;
        }
        return LootMode.DropStyle.MULTI;
    }

    public LootMode.LootTier getDefaultLootTier() {
        String tierValue = this.plugin.getConfigManager().getConfig().getString("loot.default-tier");
        if (tierValue == null || tierValue.isEmpty()) {
            tierValue = this.plugin.getConfigManager().getConfig().getString("loot.tier");
        }
        if (tierValue == null || tierValue.isEmpty()) {
            return this.getDefaultLootMode().getTier();
        }
        tierValue = tierValue.toUpperCase().trim();
        try {
            return LootMode.LootTier.valueOf(tierValue);
        }
        catch (IllegalArgumentException e) {
            return LootMode.LootTier.NORMAL;
        }
    }

    public void castVote(Player player, Arena arena, LootMode mode) {
        if (arena == null || mode == null) {
            return;
        }
        String arenaName = arena.getName();
        LootMode previousVote = this.getPlayerVote(player, arena);
        this.lootVotes.computeIfAbsent(arenaName, k -> new ConcurrentHashMap()).put(player.getUniqueId(), mode);
        if (previousVote != mode && this.plugin.getConfigManager().isChatNotificationEnabled("vote-cast")) {
            int voteCount = this.getVoteCount(arena, mode);
            String changeText = previousVote != null ? " <dark_gray>(changed from " + previousVote.getDisplayName() + ")" : "";
            arena.broadcastMessage(FortunePillars.parseWithPrefix("<yellow>" + player.getName() + "</yellow> <gray>voted for loot mode</gray> " + mode.getColoredDisplayName() + " <dark_gray>[" + voteCount + " vote" + (voteCount != 1 ? "s" : "") + "]" + changeText));
        }
    }

    public void removeVote(Player player, Arena arena) {
        if (arena == null) {
            return;
        }
        Map<UUID, LootMode> votes = this.lootVotes.get(arena.getName());
        if (votes != null) {
            votes.remove(player.getUniqueId());
        }
    }

    public LootMode getPlayerVote(Player player, Arena arena) {
        if (arena == null) {
            return null;
        }
        Map<UUID, LootMode> votes = this.lootVotes.get(arena.getName());
        return votes != null ? votes.get(player.getUniqueId()) : null;
    }

    public boolean hasVoted(Player player, Arena arena) {
        return this.getPlayerVote(player, arena) != null;
    }

    public int getVoteCount(Arena arena, LootMode mode) {
        if (arena == null || mode == null) {
            return 0;
        }
        Map<UUID, LootMode> votes = this.lootVotes.get(arena.getName());
        if (votes == null) {
            return 0;
        }
        return (int)votes.values().stream().filter(v -> v == mode).count();
    }

    public int getTotalVotes(Arena arena) {
        if (arena == null) {
            return 0;
        }
        Map<UUID, LootMode> votes = this.lootVotes.get(arena.getName());
        return votes != null ? votes.size() : 0;
    }

    public LootMode calculateWinningMode(Arena arena) {
        if (arena == null) {
            return this.getDefaultLootMode();
        }
        Map<UUID, LootMode> votes = this.lootVotes.get(arena.getName());
        if (votes == null || votes.isEmpty()) {
            return this.getDefaultLootMode();
        }
        EnumMap<LootMode, Integer> voteCounts = new EnumMap<LootMode, Integer>(LootMode.class);
        for (LootMode mode : LootMode.values()) {
            voteCounts.put(mode, 0);
        }
        for (LootMode vote : votes.values()) {
            voteCounts.merge(vote, 1, Integer::sum);
        }
        LootMode winner = this.getDefaultLootMode();
        int maxVotes = 0;
        ArrayList<LootMode> tiedModes = new ArrayList<LootMode>();
        for (Map.Entry entry : voteCounts.entrySet()) {
            int count = (Integer)entry.getValue();
            if (count > maxVotes) {
                maxVotes = count;
                winner = (LootMode)((Object)entry.getKey());
                tiedModes.clear();
                tiedModes.add((LootMode)((Object)entry.getKey()));
                continue;
            }
            if (count != maxVotes || maxVotes <= 0) continue;
            tiedModes.add((LootMode)((Object)entry.getKey()));
        }
        if (tiedModes.size() > 1) {
            winner = (LootMode)((Object)tiedModes.get(new Random().nextInt(tiedModes.size())));
        }
        return winner;
    }

    public List<String> getVoteResults(Arena arena) {
        ArrayList<String> results = new ArrayList<String>();
        if (arena == null) {
            return results;
        }
        int totalVotes = this.getTotalVotes(arena);
        for (LootMode mode : LootMode.values()) {
            int votes = this.getVoteCount(arena, mode);
            if (votes <= 0) continue;
            int percentage = totalVotes > 0 ? votes * 100 / totalVotes : 0;
            results.add(mode.getColoredDisplayName() + ": <white>" + votes + " vote" + (votes != 1 ? "s" : "") + " <gray>(" + percentage + "%)");
        }
        if (results.isEmpty()) {
            results.add("<gray>No votes yet! Default: " + this.getDefaultLootMode().getDisplayName());
        }
        return results;
    }

    public Map<LootMode.DropStyle, List<String>> getVoteResultsByStyle(Arena arena) {
        EnumMap<LootMode.DropStyle, List<String>> results = new EnumMap<LootMode.DropStyle, List<String>>(LootMode.DropStyle.class);
        results.put(LootMode.DropStyle.MULTI, new ArrayList());
        results.put(LootMode.DropStyle.SINGLE, new ArrayList());
        if (arena == null) {
            return results;
        }
        int totalVotes = this.getTotalVotes(arena);
        for (LootMode mode : LootMode.values()) {
            int votes = this.getVoteCount(arena, mode);
            if (votes <= 0) continue;
            int percentage = totalVotes > 0 ? votes * 100 / totalVotes : 0;
            String line = mode.getColoredDisplayName() + ": <white>" + votes + " vote" + (votes != 1 ? "s" : "") + " <gray>(" + percentage + "%)";
            ((List)results.get((Object)mode.getStyle())).add(line);
        }
        return results;
    }

    public Map<LootMode.LootTier, List<String>> getVoteResultsByTier(Arena arena) {
        EnumMap<LootMode.LootTier, List<String>> results = new EnumMap<LootMode.LootTier, List<String>>(LootMode.LootTier.class);
        results.put(LootMode.LootTier.NORMAL, new ArrayList());
        results.put(LootMode.LootTier.BALANCED, new ArrayList());
        results.put(LootMode.LootTier.OP, new ArrayList());
        if (arena == null) {
            return results;
        }
        int totalVotes = this.getTotalVotes(arena);
        for (LootMode mode : LootMode.values()) {
            int votes = this.getVoteCount(arena, mode);
            if (votes <= 0) continue;
            int percentage = totalVotes > 0 ? votes * 100 / totalVotes : 0;
            String line = mode.getColoredDisplayName() + ": <white>" + votes + " vote" + (votes != 1 ? "s" : "") + " <gray>(" + percentage + "%)";
            ((List)results.get((Object)mode.getTier())).add(line);
        }
        return results;
    }

    public int getTierVotes(Arena arena, LootMode.LootTier tier) {
        if (arena == null || tier == null) {
            return 0;
        }
        Map<UUID, LootMode> votes = this.lootVotes.get(arena.getName());
        if (votes == null) {
            return 0;
        }
        return (int)votes.values().stream().filter(mode -> mode.getTier() == tier).count();
    }

    public int getStyleVotes(Arena arena, LootMode.DropStyle style) {
        if (arena == null || style == null) {
            return 0;
        }
        Map<UUID, LootMode> votes = this.lootVotes.get(arena.getName());
        if (votes == null) {
            return 0;
        }
        return (int)votes.values().stream().filter(mode -> mode.getStyle() == style).count();
    }

    public LootMode.LootTier getLeadingTier(Arena arena) {
        if (arena == null) {
            return null;
        }
        LootMode.LootTier leading = null;
        int maxVotes = 0;
        for (LootMode.LootTier tier : LootMode.LootTier.values()) {
            int votes = this.getTierVotes(arena, tier);
            if (votes <= maxVotes) continue;
            maxVotes = votes;
            leading = tier;
        }
        return leading;
    }

    public LootMode.DropStyle getLeadingStyle(Arena arena) {
        if (arena == null) {
            return null;
        }
        LootMode.DropStyle leading = null;
        int maxVotes = 0;
        for (LootMode.DropStyle style : LootMode.DropStyle.values()) {
            int votes = this.getStyleVotes(arena, style);
            if (votes <= maxVotes) continue;
            maxVotes = votes;
            leading = style;
        }
        return leading;
    }

    public List<String> getDetailedVoteSummary(Arena arena) {
        int votes;
        ArrayList<String> summary = new ArrayList<String>();
        if (arena == null) {
            return summary;
        }
        int totalVotes = this.getTotalVotes(arena);
        summary.add("<yellow>\ud83d\udcca Total Votes: <white>" + totalVotes);
        summary.add("<gray>Default Mode: " + this.getDefaultLootMode().getDisplayName());
        summary.add("");
        LootMode leadingMode = this.getLeadingMode(arena);
        if (leadingMode != null) {
            summary.add("<gold>\ud83c\udfc6 Leading Mode:");
            summary.add(leadingMode.getColoredDisplayName());
            summary.add("<gray>  " + this.getVoteCount(arena, leadingMode) + " vote(s)");
            summary.add("");
        }
        summary.add("<yellow>\ud83d\udce6 By Style:");
        for (LootMode.DropStyle dropStyle : LootMode.DropStyle.values()) {
            votes = this.getStyleVotes(arena, dropStyle);
            if (votes <= 0) continue;
            String styleIcon = dropStyle == LootMode.DropStyle.MULTI ? "\ud83d\udce6" : "1\ufe0f\u20e3";
            summary.add("  " + styleIcon + " <gray>" + dropStyle.name() + ": <white>" + votes);
        }
        summary.add("");
        summary.add("<yellow>\u2696 By Tier:");
        for (Enum enum_ : LootMode.LootTier.values()) {
            votes = this.getTierVotes(arena, (LootMode.LootTier)enum_);
            if (votes <= 0) continue;
            LootMode.LootTier tier = (LootMode.LootTier) enum_;
            String tierColor = switch (tier) {
                default -> "<white>";
                case NORMAL -> "<white>";
                case BALANCED -> "<aqua>";
                case OP -> "<gold>";
            };
            summary.add("  " + tierColor + enum_.name() + ": <white>" + votes);
        }
        return summary;
    }

    public void clearVotes(Arena arena) {
        if (arena != null) {
            this.lootVotes.remove(arena.getName());
        }
    }

    public void clearPlayerVotes(Player player) {
        for (Map<UUID, LootMode> votes : this.lootVotes.values()) {
            votes.remove(player.getUniqueId());
        }
    }

    public LootMode getLeadingMode(Arena arena) {
        if (arena == null) {
            return null;
        }
        LootMode leading = null;
        int maxVotes = 0;
        for (LootMode mode : LootMode.values()) {
            int votes = this.getVoteCount(arena, mode);
            if (votes <= maxVotes) continue;
            maxVotes = votes;
            leading = mode;
        }
        return leading;
    }

    public boolean isTied(Arena arena) {
        if (arena == null) {
            return false;
        }
        int maxVotes = 0;
        int leadingCount = 0;
        for (LootMode mode : LootMode.values()) {
            int votes = this.getVoteCount(arena, mode);
            if (votes > maxVotes) {
                maxVotes = votes;
                leadingCount = 1;
                continue;
            }
            if (votes != maxVotes || maxVotes <= 0) continue;
            ++leadingCount;
        }
        return leadingCount > 1;
    }

    public List<LootMode> getTiedLeaders(Arena arena) {
        if (arena == null) {
            return Collections.emptyList();
        }
        int maxVotes = 0;
        ArrayList<LootMode> leaders = new ArrayList<LootMode>();
        for (LootMode mode : LootMode.values()) {
            int votes = this.getVoteCount(arena, mode);
            if (votes > maxVotes) {
                maxVotes = votes;
                leaders.clear();
                leaders.add(mode);
                continue;
            }
            if (votes != maxVotes || maxVotes <= 0) continue;
            leaders.add(mode);
        }
        return leaders;
    }

    public List<LootMode> getModesByTier(LootMode.LootTier tier) {
        return Arrays.stream(LootMode.values()).filter(mode -> mode.getTier() == tier).collect(Collectors.toList());
    }

    public List<LootMode> getModesByStyle(LootMode.DropStyle style) {
        return Arrays.stream(LootMode.values()).filter(mode -> mode.getStyle() == style).collect(Collectors.toList());
    }

    public List<LootMode> getModesByTierAndStyle(LootMode.LootTier tier, LootMode.DropStyle style) {
        return Arrays.stream(LootMode.values()).filter(mode -> mode.getTier() == tier && mode.getStyle() == style).collect(Collectors.toList());
    }

    public void broadcastVoteSummary(Arena arena) {
        if (arena == null) {
            return;
        }
        int totalVotes = this.getTotalVotes(arena);
        if (totalVotes == 0) {
            arena.broadcastMessage(FortunePillars.parseWithPrefix("<gray>No loot mode votes! Using default: <yellow>" + this.getDefaultLootMode().getDisplayName()));
            return;
        }
        arena.broadcastMessage(FortunePillars.parseWithPrefix(""));
        arena.broadcastMessage(FortunePillars.parseWithPrefix("<yellow>\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501"));
        arena.broadcastMessage(FortunePillars.parseWithPrefix("<gold><bold>\ud83d\udcca LOOT MODE VOTES</bold></gold>"));
        arena.broadcastMessage(FortunePillars.parseWithPrefix(""));
        LootMode leading = this.getLeadingMode(arena);
        if (leading != null) {
            int leadingVotes = this.getVoteCount(arena, leading);
            arena.broadcastMessage(FortunePillars.parseWithPrefix("<gold>\ud83c\udfc6 Leading: " + leading.getColoredDisplayName() + " <gray>(" + leadingVotes + " vote" + (leadingVotes != 1 ? "s" : "") + ")"));
            arena.broadcastMessage(FortunePillars.parseWithPrefix(""));
        }
        int multiVotes = this.getStyleVotes(arena, LootMode.DropStyle.MULTI);
        int singleVotes = this.getStyleVotes(arena, LootMode.DropStyle.SINGLE);
        arena.broadcastMessage(FortunePillars.parseWithPrefix("<yellow>By Style:"));
        arena.broadcastMessage(FortunePillars.parseWithPrefix("  <gold>\ud83d\udce6 Multi: <white>" + multiVotes + " <gray>| <aqua>1\ufe0f\u20e3 Single: <white>" + singleVotes));
        arena.broadcastMessage(FortunePillars.parseWithPrefix(""));
        arena.broadcastMessage(FortunePillars.parseWithPrefix("<yellow>By Tier:"));
        int normalVotes = this.getTierVotes(arena, LootMode.LootTier.NORMAL);
        int balancedVotes = this.getTierVotes(arena, LootMode.LootTier.BALANCED);
        int opVotes = this.getTierVotes(arena, LootMode.LootTier.OP);
        arena.broadcastMessage(FortunePillars.parseWithPrefix("  <white>Normal: " + normalVotes + " <gray>| <aqua>Balanced: " + balancedVotes + " <gray>| <gold>OP: " + opVotes));
        arena.broadcastMessage(FortunePillars.parseWithPrefix("<yellow>\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501"));
        arena.broadcastMessage(FortunePillars.parseWithPrefix(""));
    }

    public void shutdown() {
        this.lootVotes.clear();
    }

    public Map<UUID, LootMode> getVotes(Arena arena) {
        if (arena == null) {
            return Collections.emptyMap();
        }
        return new HashMap<UUID, LootMode>(this.lootVotes.getOrDefault(arena.getName(), Collections.emptyMap()));
    }

    public List<String> getDebugInfo(Arena arena) {
        ArrayList<String> info = new ArrayList<String>();
        if (arena == null) {
            info.add("Arena is null!");
            return info;
        }
        info.add("=== Loot Vote Debug Info for " + arena.getName() + " ===");
        info.add("Config Default Mode: " + this.getDefaultLootMode().name());
        info.add("Config Default Style: " + this.getDefaultDropStyle().name());
        info.add("Config Default Tier: " + this.getDefaultLootTier().name());
        info.add("Total Votes: " + this.getTotalVotes(arena));
        info.add("");
        Map<UUID, LootMode> votes = this.getVotes(arena);
        if (votes.isEmpty()) {
            info.add("No votes cast yet.");
        } else {
            info.add("Individual Votes:");
            votes.forEach((uuid, mode) -> info.add("  " + String.valueOf(uuid) + " -> " + mode.name()));
        }
        info.add("");
        info.add("Vote Breakdown:");
        for (LootMode mode2 : LootMode.values()) {
            int count = this.getVoteCount(arena, mode2);
            if (count <= 0) continue;
            info.add("  " + mode2.name() + ": " + count);
        }
        info.add("");
        info.add("Leading Mode: " + (this.getLeadingMode(arena) != null ? this.getLeadingMode(arena).name() : "None"));
        info.add("Is Tied: " + this.isTied(arena));
        info.add("Winning Mode: " + this.calculateWinningMode(arena).name());
        return info;
    }
}

