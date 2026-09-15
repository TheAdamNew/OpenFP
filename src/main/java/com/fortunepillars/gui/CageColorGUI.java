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
import com.fortunepillars.cosmetics.CosmeticType;
import com.fortunepillars.gui.CosmeticsGUI;
import com.fortunepillars.gui.GUI;
import com.fortunepillars.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CageColorGUI
extends GUI {
    public CageColorGUI(FortunePillars plugin, Player player) {
        super(plugin, player, "<gradient:#FFD700:#FFA500>Cage Colors</gradient>", 45);
    }

    @Override
    public void setup() {
        this.fillBorder(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        String currentCage = this.plugin.getCosmeticsManager().getSelectedCosmetic(this.player, CosmeticType.CAGE);
        if (currentCage == null) {
            currentCage = "WHITE";
        }
        this.setCageItem(10, "WHITE", Material.WHITE_STAINED_GLASS, "White", currentCage, null);
        this.setCageItem(11, "ORANGE", Material.ORANGE_STAINED_GLASS, "Orange", currentCage, null);
        this.setCageItem(12, "MAGENTA", Material.MAGENTA_STAINED_GLASS, "Magenta", currentCage, null);
        this.setCageItem(13, "LIGHT_BLUE", Material.LIGHT_BLUE_STAINED_GLASS, "Light Blue", currentCage, null);
        this.setCageItem(14, "YELLOW", Material.YELLOW_STAINED_GLASS, "Yellow", currentCage, null);
        this.setCageItem(15, "LIME", Material.LIME_STAINED_GLASS, "Lime", currentCage, null);
        this.setCageItem(16, "PINK", Material.PINK_STAINED_GLASS, "Pink", currentCage, null);
        this.setCageItem(19, "GRAY", Material.GRAY_STAINED_GLASS, "Gray", currentCage, null);
        this.setCageItem(20, "LIGHT_GRAY", Material.LIGHT_GRAY_STAINED_GLASS, "Light Gray", currentCage, null);
        this.setCageItem(21, "CYAN", Material.CYAN_STAINED_GLASS, "Cyan", currentCage, null);
        this.setCageItem(22, "PURPLE", Material.PURPLE_STAINED_GLASS, "Purple", currentCage, null);
        this.setCageItem(23, "BLUE", Material.BLUE_STAINED_GLASS, "Blue", currentCage, null);
        this.setCageItem(24, "BROWN", Material.BROWN_STAINED_GLASS, "Brown", currentCage, null);
        this.setCageItem(25, "GREEN", Material.GREEN_STAINED_GLASS, "Green", currentCage, null);
        this.setCageItem(28, "RED", Material.RED_STAINED_GLASS, "Red", currentCage, null);
        this.setCageItem(29, "BLACK", Material.BLACK_STAINED_GLASS, "Black", currentCage, null);
        this.setCageItem(30, "CLEAR", Material.GLASS, "Clear Glass", currentCage, "fortunepillars.cosmetic.cage.clear");
        this.setCageItem(31, "TINTED", Material.TINTED_GLASS, "Tinted Glass", currentCage, "fortunepillars.cosmetic.cage.tinted");
        this.setCageItem(32, "RAINBOW", Material.BEACON, "Rainbow", currentCage, "fortunepillars.cosmetic.cage.rainbow");
        this.setItem(36, ItemBuilder.of(Material.ARROW).name("<yellow><bold>\u2190 Back</bold></yellow>").lore("", "<gray>Return to cosmetics").bedrockSafe().build(), event -> {
            this.player.playSound(this.player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            new CosmeticsGUI(this.plugin, this.player).open();
        });
        this.setItem(44, ItemBuilder.of(Material.BARRIER).name("<red>Close").bedrockSafe().build(), event -> this.player.closeInventory());
    }

    private void setCageItem(int slot, String id, Material material, String displayName, String currentCage, String permission) {
        boolean isSelected = id.equalsIgnoreCase(currentCage);
        boolean hasPermission = permission == null || this.player.hasPermission(permission);
        String prefix = isSelected ? "<green>\u2713 " : "";
        String lockIcon = hasPermission ? "" : "<red>\ud83d\udd12 ";
        ItemBuilder builder = ItemBuilder.of(material).name(lockIcon + prefix + "<yellow>" + displayName + "</yellow>").lore("", isSelected ? "<green>Currently equipped!" : (hasPermission ? "<yellow>Click to select!" : "<red>Locked!"), hasPermission ? "" : "<dark_gray>Requires permission");
        if (isSelected) {
            builder.glow();
        }
        ItemStack item = builder.bedrockSafe().build();
        this.setItem(slot, item, event -> {
            if (!hasPermission) {
                this.player.sendMessage(FortunePillars.parseWithPrefix("<red>You don't have permission for this cage!"));
                this.player.playSound(this.player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                return;
            }
            this.selectCage(id, displayName);
        });
    }

    private void selectCage(String id, String displayName) {
        this.plugin.getCosmeticsManager().setSelectedCosmetic(this.player, CosmeticType.CAGE, id);
        this.player.sendMessage(FortunePillars.parseWithPrefix("<green>Selected cage color: <yellow>" + displayName));
        this.player.playSound(this.player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        this.refresh();
    }
}

