/*
 * Decompiled with CFR 0.152.
 */
package com.fortunepillars.game.loot;

import java.util.Random;

public enum LootMode {
    MULTI_NORMAL_FAST("Multi Normal - Fast", "<yellow>\ud83d\udce6\u26a1 Multi Normal (10s)", "1-3 normal items every 10s - Items drop in stacks", 10, 10, 1, 3, LootTier.NORMAL, DropStyle.MULTI),
    MULTI_NORMAL_MEDIUM("Multi Normal - Medium", "<white>\ud83d\udce6 Multi Normal (20s)", "1-3 normal items every 20s - Items drop in stacks", 20, 20, 1, 3, LootTier.NORMAL, DropStyle.MULTI),
    MULTI_NORMAL_SLOW("Multi Normal - Slow", "<gray>\ud83d\udce6\ud83d\udc22 Multi Normal (30s)", "1-2 normal items every 30s - Items drop in stacks", 30, 30, 1, 2, LootTier.NORMAL, DropStyle.MULTI),
    MULTI_BALANCED_FAST("Multi Balanced - Fast", "<aqua>\u2696\u26a1 Multi Balanced (10s)", "1-3 balanced items every 10s - Items drop in stacks", 10, 10, 1, 3, LootTier.BALANCED, DropStyle.MULTI),
    MULTI_BALANCED_MEDIUM("Multi Balanced - Medium", "<blue>\u2696 Multi Balanced (20s)", "1-3 balanced items every 20s - Items drop in stacks", 20, 20, 1, 3, LootTier.BALANCED, DropStyle.MULTI),
    MULTI_BALANCED_SLOW("Multi Balanced - Slow", "<dark_aqua>\u2696\ud83d\udc22 Multi Balanced (30s)", "1-2 balanced items every 30s - Items drop in stacks", 30, 30, 1, 2, LootTier.BALANCED, DropStyle.MULTI),
    MULTI_OP_FAST("Multi OP - Fast", "<gold>\ud83d\udc8e\u26a1 Multi OP (10s)", "1-4 OP items every 10s - Items drop in stacks", 10, 10, 1, 4, LootTier.OP, DropStyle.MULTI),
    MULTI_OP_MEDIUM("Multi OP - Medium", "<yellow>\ud83d\udc8e Multi OP (20s)", "1-3 OP items every 20s - Items drop in stacks", 20, 20, 1, 3, LootTier.OP, DropStyle.MULTI),
    MULTI_OP_SLOW("Multi OP - Slow", "<gold>\ud83d\udc8e\ud83d\udc22 Multi OP (30s)", "1-2 OP items every 30s - Items drop in stacks", 30, 30, 1, 2, LootTier.OP, DropStyle.MULTI),
    SINGLE_NORMAL_FAST("Single Normal - Fast", "<yellow>1\ufe0f\u20e3\ud83d\udce6 Single Normal (10s)", "Exactly 1 normal item every 10s - NO stacks!", 10, 10, 1, 1, LootTier.NORMAL, DropStyle.SINGLE),
    SINGLE_NORMAL_MEDIUM("Single Normal - Medium", "<white>1\ufe0f\u20e3\ud83d\udce6 Single Normal (20s)", "Exactly 1 normal item every 20s - NO stacks!", 20, 20, 1, 1, LootTier.NORMAL, DropStyle.SINGLE),
    SINGLE_NORMAL_SLOW("Single Normal - Slow", "<gray>1\ufe0f\u20e3\ud83d\udce6 Single Normal (30s)", "Exactly 1 normal item every 30s - NO stacks!", 30, 30, 1, 1, LootTier.NORMAL, DropStyle.SINGLE),
    SINGLE_BALANCED_FAST("Single Balanced - Fast", "<aqua>1\ufe0f\u20e3\u2696 Single Balanced (10s)", "Exactly 1 balanced item every 10s - NO stacks!", 10, 10, 1, 1, LootTier.BALANCED, DropStyle.SINGLE),
    SINGLE_BALANCED_MEDIUM("Single Balanced - Medium", "<blue>1\ufe0f\u20e3\u2696 Single Balanced (20s)", "Exactly 1 balanced item every 20s - NO stacks!", 20, 20, 1, 1, LootTier.BALANCED, DropStyle.SINGLE),
    SINGLE_BALANCED_SLOW("Single Balanced - Slow", "<dark_aqua>1\ufe0f\u20e3\u2696 Single Balanced (30s)", "Exactly 1 balanced item every 30s - NO stacks!", 30, 30, 1, 1, LootTier.BALANCED, DropStyle.SINGLE),
    SINGLE_OP_FAST("Single OP - Fast", "<gold>1\ufe0f\u20e3\ud83d\udc8e Single OP (10s)", "Exactly 1 OP item every 10s - NO stacks!", 10, 10, 1, 1, LootTier.OP, DropStyle.SINGLE),
    SINGLE_OP_MEDIUM("Single OP - Medium", "<yellow>1\ufe0f\u20e3\ud83d\udc8e Single OP (20s)", "Exactly 1 OP item every 20s - NO stacks!", 20, 20, 1, 1, LootTier.OP, DropStyle.SINGLE),
    SINGLE_OP_SLOW("Single OP - Slow", "<gold>1\ufe0f\u20e3\ud83d\udc8e Single OP (30s)", "Exactly 1 OP item every 30s - NO stacks!", 30, 30, 1, 1, LootTier.OP, DropStyle.SINGLE);

    private final String displayName;
    private final String coloredDisplayName;
    private final String description;
    private final int minIntervalSeconds;
    private final int maxIntervalSeconds;
    private final int minItems;
    private final int maxItems;
    private final LootTier tier;
    private final DropStyle style;
    private final Random random;

    private LootMode(String displayName, String coloredDisplayName, String description, int minIntervalSeconds, int maxIntervalSeconds, int minItems, int maxItems, LootTier tier, DropStyle style) {
        this.displayName = displayName;
        this.coloredDisplayName = coloredDisplayName;
        this.description = description;
        this.minIntervalSeconds = minIntervalSeconds;
        this.maxIntervalSeconds = maxIntervalSeconds;
        this.minItems = minItems;
        this.maxItems = maxItems;
        this.tier = tier;
        this.style = style;
        this.random = new Random();
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getColoredDisplayName() {
        return this.coloredDisplayName;
    }

    public String getDescription() {
        return this.description;
    }

    public int getMinIntervalSeconds() {
        return this.minIntervalSeconds;
    }

    public int getMaxIntervalSeconds() {
        return this.maxIntervalSeconds;
    }

    public int getMinItems() {
        return this.minItems;
    }

    public int getMaxItems() {
        return this.maxItems;
    }

    public LootTier getTier() {
        return this.tier;
    }

    public DropStyle getStyle() {
        return this.style;
    }

    public boolean isSingleMode() {
        return this.style == DropStyle.SINGLE;
    }

    public boolean isMultiMode() {
        return this.style == DropStyle.MULTI;
    }

    public int getRandomIntervalTicks() {
        if (this.minIntervalSeconds == this.maxIntervalSeconds) {
            return this.minIntervalSeconds * 20;
        }
        int seconds = this.minIntervalSeconds + this.random.nextInt(this.maxIntervalSeconds - this.minIntervalSeconds + 1);
        return seconds * 20;
    }

    public int getRandomItemCount() {
        if (this.minItems == this.maxItems) {
            return this.minItems;
        }
        return this.minItems + this.random.nextInt(this.maxItems - this.minItems + 1);
    }

    public String getTierColor() {
        return switch (this.tier.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "<white>";
            case 1 -> "<aqua>";
            case 2 -> "<gold>";
        };
    }

    public String getStyleEmoji() {
        return switch (this.style.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "\ud83d\udce6";
            case 1 -> "1\ufe0f\u20e3";
        };
    }

    public static LootMode fromString(String name) {
        for (LootMode mode : LootMode.values()) {
            if (!mode.name().equalsIgnoreCase(name)) continue;
            return mode;
        }
        return MULTI_NORMAL_MEDIUM;
    }

    public static enum LootTier {
        NORMAL,
        BALANCED,
        OP;

    }

    public static enum DropStyle {
        MULTI,
        SINGLE;

    }
}

