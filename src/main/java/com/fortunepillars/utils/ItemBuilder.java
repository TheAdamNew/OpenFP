/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
 *  org.bukkit.Bukkit
 *  org.bukkit.Color
 *  org.bukkit.Material
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemFlag
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.inventory.meta.LeatherArmorMeta
 *  org.bukkit.inventory.meta.SkullMeta
 */
package com.fortunepillars.utils;

import com.fortunepillars.FortunePillars;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class ItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;
    private static Boolean floodgatePresent = null;

    private ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = this.item.getItemMeta();
    }

    private ItemBuilder(ItemStack item) {
        this.item = item.clone();
        this.meta = this.item.getItemMeta();
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(material);
    }

    public static ItemBuilder of(ItemStack item) {
        return new ItemBuilder(item);
    }

    public static ItemStack filler() {
        return ItemBuilder.filler(Material.GRAY_STAINED_GLASS_PANE);
    }

    public static ItemStack filler(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName((Component)Component.empty());
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemBuilder name(String name) {
        if (this.meta != null && name != null) {
            this.meta.displayName(FortunePillars.parse(name));
        }
        return this;
    }

    public ItemBuilder name(Component name) {
        if (this.meta != null && name != null) {
            this.meta.displayName(name);
        }
        return this;
    }

    public ItemBuilder lore(String ... lines) {
        if (this.meta != null && lines != null) {
            List lore = Arrays.stream(lines).map(FortunePillars::parse).collect(Collectors.toList());
            this.meta.lore(lore);
        }
        return this;
    }

    public ItemBuilder lore(List<String> lines) {
        if (this.meta != null && lines != null) {
            List lore = lines.stream().map(FortunePillars::parse).collect(Collectors.toList());
            this.meta.lore(lore);
        }
        return this;
    }

    public ItemBuilder addLore(String line) {
        if (this.meta != null && line != null) {
            List<Component> lore = this.meta.lore();
            if (lore == null) {
                lore = new ArrayList<Component>();
            }
            lore.add(FortunePillars.parse(line));
            this.meta.lore(lore);
        }
        return this;
    }

    public ItemBuilder addLore(String ... lines) {
        if (this.meta != null && lines != null) {
            List<Component> lore = this.meta.lore();
            if (lore == null) {
                lore = new ArrayList<Component>();
            }
            for (String line : lines) {
                lore.add(FortunePillars.parse(line));
            }
            this.meta.lore(lore);
        }
        return this;
    }

    public ItemBuilder amount(int amount) {
        this.item.setAmount(Math.max(1, Math.min(64, amount)));
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        if (this.meta != null && enchantment != null) {
            this.meta.addEnchant(enchantment, level, true);
        }
        return this;
    }

    public ItemBuilder glow() {
        return this.glow(true);
    }

    public ItemBuilder glow(boolean glow) {
        if (this.meta != null) {
            this.meta.setEnchantmentGlintOverride(Boolean.valueOf(glow));
        }
        return this;
    }

    public ItemBuilder flags(ItemFlag ... flags) {
        if (this.meta != null && flags != null) {
            this.meta.addItemFlags(flags);
        }
        return this;
    }

    public ItemBuilder hideFlags() {
        if (this.meta != null) {
            this.meta.addItemFlags(ItemFlag.values());
        }
        return this;
    }

    public ItemBuilder hideAttributes() {
        if (this.meta != null) {
            this.meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
        }
        return this;
    }

    public ItemBuilder unbreakable() {
        return this.unbreakable(true);
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        if (this.meta != null) {
            this.meta.setUnbreakable(unbreakable);
        }
        return this;
    }

    public ItemBuilder customModelData(int data) {
        if (this.meta != null) {
            this.meta.setCustomModelData(Integer.valueOf(data));
        }
        return this;
    }

    public ItemBuilder leatherColor(Color color) {
        ItemMeta itemMeta = this.meta;
        if (itemMeta instanceof LeatherArmorMeta) {
            LeatherArmorMeta leatherMeta = (LeatherArmorMeta)itemMeta;
            if (color != null) {
                leatherMeta.setColor(color);
            }
        }
        return this;
    }

    public ItemBuilder skullOwner(String playerName) {
        ItemMeta itemMeta = this.meta;
        if (itemMeta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta)itemMeta;
            if (playerName != null) {
                skullMeta.setOwner(playerName);
            }
        }
        return this;
    }

    public ItemBuilder bedrockSafe() {
        if (this.meta == null) {
            return this;
        }
        LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
        try {
            List<Component> loreParts;
            Component displayName;
            if (this.meta.hasDisplayName() && (displayName = this.meta.displayName()) != null) {
                String legacyName = legacy.serialize(displayName);
                this.meta.setDisplayName(legacyName);
            }
            if (this.meta.hasLore() && (loreParts = this.meta.lore()) != null && !loreParts.isEmpty()) {
                ArrayList<String> legacyLore = new ArrayList<String>();
                for (Component line : loreParts) {
                    if (line != null) {
                        legacyLore.add(legacy.serialize(line));
                        continue;
                    }
                    legacyLore.add("");
                }
                this.meta.setLore(legacyLore);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return this;
    }

    public static boolean isFloodgateInstalled() {
        if (floodgatePresent == null) {
            floodgatePresent = Bukkit.getPluginManager().getPlugin("floodgate") != null;
        }
        return floodgatePresent;
    }

    public static boolean isBedrockPlayer(Player player) {
        if (!ItemBuilder.isFloodgateInstalled()) {
            return false;
        }
        try {
            return player.getUniqueId().getMostSignificantBits() == 0L;
        }
        catch (Exception e) {
            return false;
        }
    }

    public ItemStack build() {
        if (this.meta != null) {
            this.item.setItemMeta(this.meta);
        }
        return this.item;
    }
}

