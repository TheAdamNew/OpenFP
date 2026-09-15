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
import com.fortunepillars.gui.CageColorGUI;
import com.fortunepillars.gui.GUI;
import com.fortunepillars.gui.MainMenuGUI;
import com.fortunepillars.gui.TrailGUI;
import com.fortunepillars.gui.WinEffectGUI;
import com.fortunepillars.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class CosmeticsGUI
extends GUI {
    public CosmeticsGUI(FortunePillars plugin, Player player) {
        super(plugin, player, "<gradient:#FF69B4:#DA70D6>Cosmetics</gradient>", 45);
    }

    @Override
    public void setup() {
        this.fillBorder(Material.MAGENTA_STAINED_GLASS_PANE);
        this.setItem(4, ItemBuilder.of(Material.NETHER_STAR).name("<gold><bold>Cosmetics Menu</bold></gold>").lore("", "<gray>Customize your appearance!", "", "<yellow>Click the items below").glow().bedrockSafe().build());
        this.setItem(11, ItemBuilder.of(Material.GLASS).name("<yellow><bold>Cage Colors</bold></yellow>").lore("", "<gray>Customize your cage!", "", "<gray>Current: <white>" + this.getCurrentCage(), "", "<yellow>Click to change!").glow().bedrockSafe().build(), event -> {
            this.player.playSound(this.player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            new CageColorGUI(this.plugin, this.player).open();
        });
        this.setItem(13, ItemBuilder.of(Material.FIREWORK_ROCKET).name("<gold><bold>Win Effects</bold></gold>").lore("", "<gray>Effects when you win!", "", "<gray>Current: <white>" + this.getCurrentWinEffect(), "", "<yellow>Click to change!").bedrockSafe().build(), event -> {
            this.player.playSound(this.player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            new WinEffectGUI(this.plugin, this.player).open();
        });
        this.setItem(15, ItemBuilder.of(Material.BLAZE_POWDER).name("<aqua><bold>Trails</bold></aqua>").lore("", "<gray>Particle trail behind you!", "", "<gray>Current: <white>" + this.getCurrentTrail(), "", "<yellow>Click to change!").bedrockSafe().build(), event -> {
            this.player.playSound(this.player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            new TrailGUI(this.plugin, this.player).open();
        });
        this.setItem(20, ItemBuilder.of(Material.REDSTONE).name("<red><bold>Kill Effects</bold></red>").lore("", "<gray>Effects when you get a kill!", "", "<gray>Current: <white>" + this.getCurrentKillEffect(), "", "<yellow>Coming soon!").bedrockSafe().build(), event -> {
            this.player.playSound(this.player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            this.player.sendMessage(FortunePillars.parseWithPrefix("<yellow>Kill effects coming soon!"));
        });
        this.setItem(24, ItemBuilder.of(Material.NOTE_BLOCK).name("<light_purple><bold>Death Cry</bold></light_purple>").lore("", "<gray>Sound when you die!", "", "<gray>Current: <white>None", "", "<yellow>Coming soon!").bedrockSafe().build(), event -> {
            this.player.playSound(this.player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            this.player.sendMessage(FortunePillars.parseWithPrefix("<yellow>Death cry coming soon!"));
        });
        this.setItem(36, ItemBuilder.of(Material.ARROW).name("<yellow><bold>\u2190 Back</bold></yellow>").lore("", "<gray>Return to main menu").bedrockSafe().build(), event -> {
            this.player.playSound(this.player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            new MainMenuGUI(this.plugin, this.player).open();
        });
        this.setItem(44, ItemBuilder.of(Material.BARRIER).name("<red>Close").bedrockSafe().build(), event -> this.player.closeInventory());
    }

    private String getCurrentCage() {
        String cage = this.plugin.getCosmeticsManager().getSelectedCosmetic(this.player, CosmeticType.CAGE);
        return cage != null && !cage.isEmpty() ? this.formatName(cage) : "Default";
    }

    private String getCurrentWinEffect() {
        String effect = this.plugin.getCosmeticsManager().getSelectedCosmetic(this.player, CosmeticType.WIN_EFFECT);
        return effect != null && !effect.isEmpty() ? this.formatName(effect) : "None";
    }

    private String getCurrentTrail() {
        String trail = this.plugin.getCosmeticsManager().getSelectedCosmetic(this.player, CosmeticType.TRAIL);
        return trail != null && !trail.isEmpty() ? this.formatName(trail) : "None";
    }

    private String getCurrentKillEffect() {
        String effect = this.plugin.getCosmeticsManager().getSelectedCosmetic(this.player, CosmeticType.KILL_EFFECT);
        return effect != null && !effect.isEmpty() ? this.formatName(effect) : "None";
    }

    private String formatName(String name) {
        if (name == null) {
            return "None";
        }
        String[] parts = name.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
        }
        return result.toString().trim();
    }
}

