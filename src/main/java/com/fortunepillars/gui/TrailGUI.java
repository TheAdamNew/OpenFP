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
import com.fortunepillars.cosmetics.CosmeticType;
import com.fortunepillars.gui.CosmeticsGUI;
import com.fortunepillars.gui.GUI;
import com.fortunepillars.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class TrailGUI
extends GUI {
    public TrailGUI(FortunePillars plugin, Player player) {
        super(plugin, player, "<gradient:#00FFFF:#FF00FF>Trails</gradient>", 45);
    }

    @Override
    public void setup() {
        this.fillBorder(Material.CYAN_STAINED_GLASS_PANE);
        String currentTrail = this.plugin.getCosmeticsManager().getSelectedCosmetic(this.player, CosmeticType.TRAIL);
        if (currentTrail == null) {
            currentTrail = "NONE";
        }
        this.setTrailItem(10, "HEARTS", Material.RED_DYE, "Hearts", currentTrail, null);
        this.setTrailItem(11, "FLAMES", Material.BLAZE_POWDER, "Flames", currentTrail, null);
        this.setTrailItem(12, "WATER", Material.WATER_BUCKET, "Water Drops", currentTrail, null);
        this.setTrailItem(13, "SMOKE", Material.GRAY_DYE, "Smoke", currentTrail, null);
        this.setTrailItem(14, "MAGIC", Material.ENCHANTED_BOOK, "Magic", currentTrail, "fortunepillars.cosmetic.trail.magic");
        this.setTrailItem(19, "RAINBOW", Material.BEACON, "Rainbow", currentTrail, "fortunepillars.cosmetic.trail.rainbow");
        this.setTrailItem(20, "ENDER", Material.ENDER_PEARL, "Ender", currentTrail, "fortunepillars.cosmetic.trail.ender");
        this.setTrailItem(21, "REDSTONE", Material.REDSTONE, "Redstone", currentTrail, "fortunepillars.cosmetic.trail.redstone");
        this.setTrailItem(22, "SLIME", Material.SLIME_BALL, "Slime", currentTrail, "fortunepillars.cosmetic.trail.slime");
        this.setTrailItem(23, "NONE", Material.BARRIER, "No Trail", currentTrail, null);
        this.setItem(36, ItemBuilder.of(Material.ARROW).name("<yellow><bold>\u2190 Back</bold></yellow>").lore("", "<gray>Return to cosmetics").bedrockSafe().build(), event -> {
            this.player.playSound(this.player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            new CosmeticsGUI(this.plugin, this.player).open();
        });
        this.setItem(44, ItemBuilder.of(Material.BARRIER).name("<red>Close").bedrockSafe().build(), event -> this.player.closeInventory());
    }

    private void setTrailItem(int slot, String id, Material material, String displayName, String currentTrail, String permission) {
        boolean isSelected = id.equalsIgnoreCase(currentTrail);
        boolean hasPermission = permission == null || this.player.hasPermission(permission);
        String prefix = isSelected ? "<green>\u2713 " : "";
        String lockIcon = hasPermission ? "" : "<red>\ud83d\udd12 ";
        ItemBuilder builder = ItemBuilder.of(material).name(lockIcon + prefix + "<aqua>" + displayName + "</aqua>").lore("", isSelected ? "<green>Currently equipped!" : (hasPermission ? "<yellow>Click to select!" : "<red>Locked!"), hasPermission ? "" : "<dark_gray>Requires permission");
        if (isSelected) {
            builder.glow();
        }
        this.setItem(slot, builder.bedrockSafe().build(), event -> {
            if (!hasPermission) {
                this.player.sendMessage(FortunePillars.parseWithPrefix("<red>You don't have permission for this trail!"));
                this.player.playSound(this.player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                return;
            }
            this.selectTrail(id, displayName);
        });
    }

    private void selectTrail(String id, String displayName) {
        this.plugin.getCosmeticsManager().setSelectedCosmetic(this.player, CosmeticType.TRAIL, id);
        this.player.sendMessage(FortunePillars.parseWithPrefix("<green>Selected trail: <aqua>" + displayName));
        this.player.playSound(this.player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        this.refresh();
    }
}

