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

public class WinEffectGUI
extends GUI {
    public WinEffectGUI(FortunePillars plugin, Player player) {
        super(plugin, player, "<gradient:#FFD700:#FFA500>Win Effects</gradient>", 45);
    }

    @Override
    public void setup() {
        this.fillBorder(Material.YELLOW_STAINED_GLASS_PANE);
        String currentEffect = this.plugin.getCosmeticsManager().getSelectedCosmetic(this.player, CosmeticType.WIN_EFFECT);
        if (currentEffect == null) {
            currentEffect = "FIREWORK";
        }
        this.setEffectItem(10, "FIREWORK", Material.FIREWORK_ROCKET, "Classic Fireworks", currentEffect, null);
        this.setEffectItem(11, "LIGHTNING", Material.LIGHTNING_ROD, "Lightning Strike", currentEffect, "fortunepillars.cosmetic.win.lightning");
        this.setEffectItem(12, "EXPLOSION", Material.TNT, "Explosion", currentEffect, "fortunepillars.cosmetic.win.explosion");
        this.setEffectItem(13, "DRAGON", Material.DRAGON_HEAD, "Dragon Breath", currentEffect, "fortunepillars.cosmetic.win.dragon");
        this.setEffectItem(14, "RAINBOW", Material.BEACON, "Rainbow Spiral", currentEffect, "fortunepillars.cosmetic.win.rainbow");
        this.setEffectItem(19, "ENDERDRAGON", Material.DRAGON_EGG, "Ender Dragon", currentEffect, "fortunepillars.cosmetic.win.enderdragon");
        this.setEffectItem(20, "WITHER", Material.WITHER_SKELETON_SKULL, "Wither Boss", currentEffect, "fortunepillars.cosmetic.win.wither");
        this.setEffectItem(21, "MEGA_FIREWORKS", Material.FIREWORK_STAR, "Mega Fireworks", currentEffect, "fortunepillars.cosmetic.win.mega");
        this.setEffectItem(22, "NONE", Material.BARRIER, "No Effect", currentEffect, null);
        this.setItem(36, ItemBuilder.of(Material.ARROW).name("<yellow><bold>\u2190 Back</bold></yellow>").lore("", "<gray>Return to cosmetics").bedrockSafe().build(), event -> {
            this.player.playSound(this.player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            new CosmeticsGUI(this.plugin, this.player).open();
        });
        this.setItem(44, ItemBuilder.of(Material.BARRIER).name("<red>Close").bedrockSafe().build(), event -> this.player.closeInventory());
    }

    private void setEffectItem(int slot, String id, Material material, String displayName, String currentEffect, String permission) {
        boolean isSelected = id.equalsIgnoreCase(currentEffect);
        boolean hasPermission = permission == null || this.player.hasPermission(permission);
        String prefix = isSelected ? "<green>\u2713 " : "";
        String lockIcon = hasPermission ? "" : "<red>\ud83d\udd12 ";
        ItemBuilder builder = ItemBuilder.of(material).name(lockIcon + prefix + "<yellow>" + displayName + "</yellow>").lore("", isSelected ? "<green>Currently equipped!" : (hasPermission ? "<yellow>Click to select!" : "<red>Locked!"), hasPermission ? "" : "<dark_gray>Requires permission");
        if (isSelected) {
            builder.glow();
        }
        this.setItem(slot, builder.bedrockSafe().build(), event -> {
            if (!hasPermission) {
                this.player.sendMessage(FortunePillars.parseWithPrefix("<red>You don't have permission for this effect!"));
                this.player.playSound(this.player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                return;
            }
            this.selectEffect(id, displayName);
        });
    }

    private void selectEffect(String id, String displayName) {
        this.plugin.getCosmeticsManager().setSelectedCosmetic(this.player, CosmeticType.WIN_EFFECT, id);
        this.player.sendMessage(FortunePillars.parseWithPrefix("<green>Selected win effect: <yellow>" + displayName));
        this.player.playSound(this.player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        this.refresh();
    }
}

