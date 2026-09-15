/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  org.bukkit.Bukkit
 *  org.bukkit.Color
 *  org.bukkit.FireworkEffect
 *  org.bukkit.FireworkEffect$Type
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Particle
 *  org.bukkit.Particle$DustOptions
 *  org.bukkit.Sound
 *  org.bukkit.entity.Firework
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.FireworkMeta
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package com.fortunepillars.cosmetics;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.cosmetics.CosmeticType;
import com.fortunepillars.player.PlayerData;
import com.fortunepillars.utils.SoundUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class CosmeticsManager {
    private final FortunePillars plugin;
    private final Map<String, CageColor> cageColors;
    private final Map<String, WinEffect> winEffects;

    public CosmeticsManager(FortunePillars plugin) {
        this.plugin = plugin;
        this.cageColors = new LinkedHashMap<String, CageColor>();
        this.winEffects = new LinkedHashMap<String, WinEffect>();
        this.initializeCageColors();
        this.initializeWinEffects();
    }

    private void initializeCageColors() {
        this.cageColors.put("WHITE", new CageColor("White", Material.WHITE_STAINED_GLASS, "<white>White Glass", null));
        this.cageColors.put("ORANGE", new CageColor("Orange", Material.ORANGE_STAINED_GLASS, "<gold>Orange Glass", null));
        this.cageColors.put("MAGENTA", new CageColor("Magenta", Material.MAGENTA_STAINED_GLASS, "<light_purple>Magenta Glass", null));
        this.cageColors.put("LIGHT_BLUE", new CageColor("Light Blue", Material.LIGHT_BLUE_STAINED_GLASS, "<aqua>Light Blue Glass", null));
        this.cageColors.put("YELLOW", new CageColor("Yellow", Material.YELLOW_STAINED_GLASS, "<yellow>Yellow Glass", null));
        this.cageColors.put("LIME", new CageColor("Lime", Material.LIME_STAINED_GLASS, "<green>Lime Glass", null));
        this.cageColors.put("PINK", new CageColor("Pink", Material.PINK_STAINED_GLASS, "<light_purple>Pink Glass", null));
        this.cageColors.put("GRAY", new CageColor("Gray", Material.GRAY_STAINED_GLASS, "<dark_gray>Gray Glass", null));
        this.cageColors.put("LIGHT_GRAY", new CageColor("Light Gray", Material.LIGHT_GRAY_STAINED_GLASS, "<gray>Light Gray Glass", null));
        this.cageColors.put("CYAN", new CageColor("Cyan", Material.CYAN_STAINED_GLASS, "<dark_aqua>Cyan Glass", null));
        this.cageColors.put("PURPLE", new CageColor("Purple", Material.PURPLE_STAINED_GLASS, "<dark_purple>Purple Glass", null));
        this.cageColors.put("BLUE", new CageColor("Blue", Material.BLUE_STAINED_GLASS, "<blue>Blue Glass", null));
        this.cageColors.put("BROWN", new CageColor("Brown", Material.BROWN_STAINED_GLASS, "<gold>Brown Glass", null));
        this.cageColors.put("GREEN", new CageColor("Green", Material.GREEN_STAINED_GLASS, "<dark_green>Green Glass", null));
        this.cageColors.put("RED", new CageColor("Red", Material.RED_STAINED_GLASS, "<red>Red Glass", null));
        this.cageColors.put("BLACK", new CageColor("Black", Material.BLACK_STAINED_GLASS, "<dark_gray>Black Glass", null));
        this.cageColors.put("CLEAR", new CageColor("Clear", Material.GLASS, "<white>Clear Glass", "fortunepillars.cosmetic.cage.clear"));
        this.cageColors.put("TINTED", new CageColor("Tinted", Material.TINTED_GLASS, "<dark_gray>Tinted Glass", "fortunepillars.cosmetic.cage.tinted"));
        this.cageColors.put("OBSIDIAN", new CageColor("Obsidian", Material.OBSIDIAN, "<dark_purple>Obsidian Cage", "fortunepillars.cosmetic.cage.obsidian"));
        this.cageColors.put("DIAMOND", new CageColor("Diamond", Material.DIAMOND_BLOCK, "<aqua>Diamond Cage", "fortunepillars.cosmetic.cage.diamond"));
        this.cageColors.put("BEDROCK", new CageColor("Bedrock", Material.BEDROCK, "<dark_gray><bold>Bedrock Cage</bold>", "fortunepillars.cosmetic.cage.bedrock"));
    }

    private void initializeWinEffects() {
        this.winEffects.put("FIREWORK", new WinEffect("Firework", Material.FIREWORK_ROCKET, "<gold>Classic Fireworks", null, this::playFireworkEffect));
        this.winEffects.put("LIGHTNING", new WinEffect("Lightning", Material.LIGHTNING_ROD, "<yellow>Lightning Strike", "fortunepillars.cosmetic.win.lightning", this::playLightningEffect));
        this.winEffects.put("EXPLOSION", new WinEffect("Explosion", Material.TNT, "<red>Explosion", "fortunepillars.cosmetic.win.explosion", this::playExplosionEffect));
        this.winEffects.put("DRAGON", new WinEffect("Dragon", Material.DRAGON_HEAD, "<dark_purple>Dragon Breath", "fortunepillars.cosmetic.win.dragon", this::playDragonEffect));
        this.winEffects.put("RAINBOW", new WinEffect("Rainbow", Material.BEACON, "<gradient:#FF0000:#FF7F00:#FFFF00:#00FF00:#0000FF:#4B0082:#9400D3>Rainbow Spiral</gradient>", "fortunepillars.cosmetic.win.rainbow", this::playRainbowEffect));
        this.winEffects.put("ENDERDRAGON", new WinEffect("Ender Dragon", Material.DRAGON_EGG, "<dark_purple>Summon Ender Dragon", "fortunepillars.cosmetic.win.enderdragon", this::playEnderDragonEffect));
        this.winEffects.put("WITHER", new WinEffect("Wither", Material.WITHER_SKELETON_SKULL, "<dark_gray>Summon Wither", "fortunepillars.cosmetic.win.wither", this::playWitherEffect));
        this.winEffects.put("MEGA_FIREWORKS", new WinEffect("Mega Fireworks", Material.FIREWORK_STAR, "<gradient:#FF0000:#FFA500:#FFFF00>Massive Firework Display</gradient>", "fortunepillars.cosmetic.win.mega", this::playMegaFireworkEffect));
        this.winEffects.put("NONE", new WinEffect("None", Material.BARRIER, "<gray>No Effect", null, (player, arena) -> {}));
    }

    public String getSelectedCosmetic(Player player, CosmeticType type) {
        if (player == null || type == null) {
            return null;
        }
        try {
            return switch (type) {
                default -> throw new MatchException(null, null);
                case CosmeticType.CAGE -> this.plugin.getDatabase().getPlayerCageColor(player.getUniqueId());
                case CosmeticType.WIN_EFFECT -> this.plugin.getDatabase().getPlayerWinEffect(player.getUniqueId());
                case CosmeticType.KILL_EFFECT, CosmeticType.TRAIL, CosmeticType.DEATH_CRY -> this.plugin.getDatabase().getPlayerCosmetic(player.getUniqueId(), type.getConfigKey());
            };
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Failed to get cosmetic for " + player.getName() + ": " + e.getMessage());
            return null;
        }
    }

    public void setSelectedCosmetic(Player player, CosmeticType type, String value) {
        if (player == null || type == null) {
            return;
        }
        try {
            switch (type) {
                case CAGE: {
                    PlayerData pd;
                    Arena arena;
                    this.plugin.getDatabase().setPlayerCageColor(player.getUniqueId(), value);
                    if (this.plugin.getCageManager() != null) {
                        this.plugin.getCageManager().clearCache(player.getUniqueId());
                    }
                    if ((arena = this.plugin.getArenaManager().getPlayerArena(player)) != null && (pd = arena.getPlayerData(player.getUniqueId())) != null) {
                        pd.setCageColor(this.getPlayerCageMaterial(player));
                    }
                    break;
                }
                case WIN_EFFECT: {
                    this.plugin.getDatabase().setPlayerWinEffect(player.getUniqueId(), value);
                    break;
                }
                case KILL_EFFECT: 
                case TRAIL: 
                case DEATH_CRY: {
                    PlayerData pd;
                    Arena arena;
                    this.plugin.getDatabase().setPlayerCosmetic(player.getUniqueId(), type.getConfigKey(), value);
                    if (type != CosmeticType.TRAIL || (arena = this.plugin.getArenaManager().getPlayerArena(player)) == null || (pd = arena.getPlayerData(player.getUniqueId())) == null) break;
                    pd.setTrailEffect(value);
                }
            }
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Failed to set cosmetic for " + player.getName() + ": " + e.getMessage());
        }
    }

    public void openCosmeticsMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, (int)54, (Component)FortunePillars.parse("<gradient:#FFD700:#FFA500>Cosmetics</gradient>"));
        ItemStack background = this.createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ", new String[0]);
        for (int i = 0; i < 54; ++i) {
            gui.setItem(i, background);
        }
        gui.setItem(4, this.createGuiItem(Material.NETHER_STAR, "<gold><bold>Cosmetics Menu</bold></gold>", "<gray>Customize your appearance!"));
        gui.setItem(10, this.createGuiItem(Material.GLASS, "<yellow><bold>Cage Colors</bold></yellow>", "<gray>Change your spawn cage color"));
        gui.setItem(28, this.createGuiItem(Material.FIREWORK_ROCKET, "<yellow><bold>Win Effects</bold></yellow>", "<gray>Change your victory celebration"));
        String currentCage = this.getSelectedCosmetic(player, CosmeticType.CAGE);
        String currentEffect = this.getSelectedCosmetic(player, CosmeticType.WIN_EFFECT);
        int cageSlot = 11;
        int cageCount = 0;
        for (Map.Entry<String, CageColor> entry : this.cageColors.entrySet()) {
            if (cageCount >= 7) break;
            CageColor cage = entry.getValue();
            boolean bl = cage.permission() == null || player.hasPermission(cage.permission());
            boolean isSelected = entry.getKey().equalsIgnoreCase(currentCage);
            ItemStack item = this.createCageColorItem(entry.getKey(), cage, bl, isSelected);
            gui.setItem(cageSlot, item);
            ++cageSlot;
            ++cageCount;
        }
        cageSlot = 19;
        int remaining = this.cageColors.size() - 7;
        int skipped = 0;
        for (Map.Entry entry : this.cageColors.entrySet()) {
            if (skipped < 7) {
                ++skipped;
                continue;
            }
            if (remaining <= 0) break;
            CageColor cage = (CageColor)entry.getValue();
            boolean hasPermission = cage.permission() == null || player.hasPermission(cage.permission());
            boolean isSelected = ((String)entry.getKey()).equalsIgnoreCase(currentCage);
            ItemStack item = this.createCageColorItem((String)entry.getKey(), cage, hasPermission, isSelected);
            gui.setItem(cageSlot, item);
            ++cageSlot;
            --remaining;
        }
        int effectSlot = 29;
        for (Map.Entry<String, WinEffect> entry : this.winEffects.entrySet()) {
            WinEffect effect = entry.getValue();
            boolean hasPermission = effect.permission() == null || player.hasPermission(effect.permission());
            boolean isSelected = entry.getKey().equalsIgnoreCase(currentEffect);
            ItemStack item = this.createWinEffectItem(entry.getKey(), effect, hasPermission, isSelected);
            gui.setItem(effectSlot, item);
            if (++effectSlot <= 35) continue;
            break;
        }
        gui.setItem(49, this.createGuiItem(Material.BARRIER, "<red>Close", "<gray>Click to close menu"));
        player.openInventory(gui);
    }

    private ItemStack createCageColorItem(String key, CageColor cage, boolean hasPermission, boolean isSelected) {
        ItemStack item = new ItemStack(cage.material());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String selectedPrefix = isSelected ? "<green>\u2713 " : "";
            meta.displayName(FortunePillars.parse(selectedPrefix + cage.displayName()));
            ArrayList<Component> lore = new ArrayList<Component>();
            lore.add(FortunePillars.parse(""));
            if (isSelected) {
                lore.add(FortunePillars.parse("<green>Currently Selected!"));
            } else if (hasPermission) {
                lore.add(FortunePillars.parse("<yellow>Click to select!"));
            } else {
                lore.add(FortunePillars.parse("<red>\ud83d\udd12 Locked"));
                lore.add(FortunePillars.parse("<dark_gray>Unlock with a rank!"));
            }
            meta.lore(lore);
            if (isSelected) {
                meta.setEnchantmentGlintOverride(Boolean.valueOf(true));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createWinEffectItem(String key, WinEffect effect, boolean hasPermission, boolean isSelected) {
        ItemStack item = new ItemStack(effect.icon());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String selectedPrefix = isSelected ? "<green>\u2713 " : "";
            meta.displayName(FortunePillars.parse(selectedPrefix + effect.displayName()));
            ArrayList<Component> lore = new ArrayList<Component>();
            lore.add(FortunePillars.parse(""));
            if (isSelected) {
                lore.add(FortunePillars.parse("<green>Currently Selected!"));
            } else if (hasPermission) {
                lore.add(FortunePillars.parse("<yellow>Click to select!"));
            } else {
                lore.add(FortunePillars.parse("<red>\ud83d\udd12 Locked"));
                lore.add(FortunePillars.parse("<dark_gray>Unlock with a rank!"));
            }
            meta.lore(lore);
            if (isSelected) {
                meta.setEnchantmentGlintOverride(Boolean.valueOf(true));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGuiItem(Material material, String name, String ... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(FortunePillars.parse(name));
            if (loreLines.length > 0) {
                ArrayList<Component> lore = new ArrayList<Component>();
                for (String line : loreLines) {
                    lore.add(FortunePillars.parse(line));
                }
                meta.lore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public void handleCosmeticClick(Player player, int slot, ItemStack clicked) {
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }
        if (clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        }
        if (slot == 49) {
            player.closeInventory();
            return;
        }
        if (slot >= 11 && slot <= 17 || slot >= 19 && slot <= 26) {
            this.handleCageColorClick(player, clicked);
            return;
        }
        if (slot >= 29 && slot <= 35) {
            this.handleWinEffectClick(player, clicked);
            return;
        }
    }

    private void handleCageColorClick(Player player, ItemStack clicked) {
        Material clickedMaterial = clicked.getType();
        for (Map.Entry<String, CageColor> entry : this.cageColors.entrySet()) {
            if (entry.getValue().material() != clickedMaterial) continue;
            CageColor cage = entry.getValue();
            if (cage.permission() != null && !player.hasPermission(cage.permission())) {
                player.sendMessage(FortunePillars.parseWithPrefix("<red>You don't have permission to use this cage color!"));
                SoundUtil.playError(player);
                return;
            }
            this.setSelectedCosmetic(player, CosmeticType.CAGE, entry.getKey());
            player.sendMessage(FortunePillars.parseWithPrefix("<green>Cage color set to " + cage.displayName() + "</green>"));
            SoundUtil.playSuccess(player);
            player.closeInventory();
            this.openCosmeticsMenu(player);
            return;
        }
    }

    private void handleWinEffectClick(Player player, ItemStack clicked) {
        Material clickedMaterial = clicked.getType();
        for (Map.Entry<String, WinEffect> entry : this.winEffects.entrySet()) {
            if (entry.getValue().icon() != clickedMaterial) continue;
            WinEffect effect = entry.getValue();
            if (effect.permission() != null && !player.hasPermission(effect.permission())) {
                player.sendMessage(FortunePillars.parseWithPrefix("<red>You don't have permission to use this win effect!"));
                SoundUtil.playError(player);
                return;
            }
            this.setSelectedCosmetic(player, CosmeticType.WIN_EFFECT, entry.getKey());
            player.sendMessage(FortunePillars.parseWithPrefix("<green>Win effect set to " + effect.displayName() + "</green>"));
            SoundUtil.playSuccess(player);
            player.closeInventory();
            this.openCosmeticsMenu(player);
            return;
        }
    }

    public Material getPlayerCageMaterial(Player player) {
        String cageKey = this.getSelectedCosmetic(player, CosmeticType.CAGE);
        CageColor cage = this.cageColors.get(cageKey != null ? cageKey.toUpperCase() : "WHITE");
        return cage != null ? cage.material() : Material.WHITE_STAINED_GLASS;
    }

    public void playWinCelebration(Player winner, Arena arena) {
        if (!this.plugin.getConfigManager().isWinFireworksEnabled()) {
            return;
        }
        String effectKey = this.getSelectedCosmetic(winner, CosmeticType.WIN_EFFECT);
        WinEffect effect = this.winEffects.getOrDefault(effectKey != null ? effectKey.toUpperCase() : "FIREWORK", this.winEffects.get("FIREWORK"));
        if (effect != null && effect.handler() != null) {
            effect.handler().play(winner, arena);
        }
    }

    private void playFireworkEffect(Player player, Arena arena) {
        int count = this.plugin.getConfigManager().getFireworkCount();
        Location baseLoc = player.getLocation();
        for (int i = 0; i < count; ++i) {
            int delay = i * 10;
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                Location spawnLoc = baseLoc.clone().add(ThreadLocalRandom.current().nextDouble(-3.0, 3.0), 0.0, ThreadLocalRandom.current().nextDouble(-3.0, 3.0));
                this.spawnFirework(spawnLoc);
            }, (long)delay);
        }
    }

    private void spawnFirework(Location location) {
        Firework firework = (Firework)location.getWorld().spawn(location, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        Color[] colors = new Color[]{Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.AQUA, Color.BLUE, Color.PURPLE, Color.WHITE, Color.FUCHSIA};
        Color primary = colors[ThreadLocalRandom.current().nextInt(colors.length)];
        Color secondary = colors[ThreadLocalRandom.current().nextInt(colors.length)];
        FireworkEffect.Type[] types = FireworkEffect.Type.values();
        FireworkEffect.Type type = types[ThreadLocalRandom.current().nextInt(types.length)];
        FireworkEffect effect = FireworkEffect.builder().with(type).withColor(primary).withFade(secondary).flicker(ThreadLocalRandom.current().nextBoolean()).trail(ThreadLocalRandom.current().nextBoolean()).build();
        meta.addEffect(effect);
        meta.setPower(ThreadLocalRandom.current().nextInt(1, 3));
        firework.setFireworkMeta(meta);
    }

    private void playLightningEffect(Player player, Arena arena) {
        Location loc = player.getLocation();
        for (int i = 0; i < 5; ++i) {
            int delay = i * 5;
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                Location strikeLoc = loc.clone().add(ThreadLocalRandom.current().nextDouble(-5.0, 5.0), 0.0, ThreadLocalRandom.current().nextDouble(-5.0, 5.0));
                loc.getWorld().strikeLightningEffect(strikeLoc);
            }, (long)delay);
        }
    }

    private void playExplosionEffect(Player player, Arena arena) {
        Location loc = player.getLocation();
        for (int i = 0; i < 3; ++i) {
            int delay = i * 15;
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                Location explosionLoc = loc.clone().add(ThreadLocalRandom.current().nextDouble(-3.0, 3.0), ThreadLocalRandom.current().nextDouble(0.0, 2.0), ThreadLocalRandom.current().nextDouble(-3.0, 3.0));
                loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, explosionLoc, 1);
                loc.getWorld().playSound(explosionLoc, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
            }, (long)delay);
        }
    }

    private void playDragonEffect(Player player, Arena arena) {
        final Location loc = player.getLocation();
        BukkitTask task = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, new Runnable(){
            double angle = 0.0;
            int ticks = 0;

            @Override
            public void run() {
                if (this.ticks >= 60) {
                    return;
                }
                for (int i = 0; i < 4; ++i) {
                    double x = Math.cos(this.angle + (double)i * Math.PI / 2.0) * 3.0;
                    double z = Math.sin(this.angle + (double)i * Math.PI / 2.0) * 3.0;
                    Location particleLoc = loc.clone().add(x, 0.5, z);
                    loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, particleLoc, 5, 0.1, 0.1, 0.1, 0.01);
                }
                this.angle += 0.19634954084936207;
                ++this.ticks;
            }
        }, 0L, 1L);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> ((BukkitTask)task).cancel(), 60L);
    }

    private void playRainbowEffect(Player player, Arena arena) {
        final Location loc = player.getLocation();
        final Color[] rainbow = new Color[]{Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.AQUA, Color.BLUE, Color.PURPLE};
        BukkitTask task = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, new Runnable(){
            double angle = 0.0;
            double height = 0.0;
            int colorIndex = 0;
            int ticks = 0;

            @Override
            public void run() {
                if (this.ticks >= 80) {
                    return;
                }
                double x = Math.cos(this.angle) * 2.0;
                double z = Math.sin(this.angle) * 2.0;
                Location particleLoc = loc.clone().add(x, this.height, z);
                Color color = rainbow[this.colorIndex % rainbow.length];
                Particle.DustOptions dust = new Particle.DustOptions(color, 1.5f);
                loc.getWorld().spawnParticle(Particle.DUST, particleLoc, 3, 0.1, 0.1, 0.1, (Object)dust);
                this.angle += 0.39269908169872414;
                this.height += 0.1;
                if (this.height > 4.0) {
                    this.height = 0.0;
                    ++this.colorIndex;
                }
                ++this.ticks;
            }
        }, 0L, 1L);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> ((BukkitTask)task).cancel(), 80L);
    }

    private void playEnderDragonEffect(Player player, Arena arena) {
        final Location loc = player.getLocation();
        BukkitTask task = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, new Runnable(){
            double angle = 0.0;
            int ticks = 0;

            @Override
            public void run() {
                if (this.ticks >= 100) {
                    return;
                }
                for (int i = 0; i < 8; ++i) {
                    double x = Math.cos(this.angle + (double)i * Math.PI / 4.0) * 5.0;
                    double z = Math.sin(this.angle + (double)i * Math.PI / 4.0) * 5.0;
                    Location particleLoc = loc.clone().add(x, 2.0, z);
                    loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, particleLoc, 20, 0.2, 0.2, 0.2, 0.05);
                    loc.getWorld().spawnParticle(Particle.PORTAL, particleLoc, 10);
                }
                if (this.ticks % 20 == 0) {
                    loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.0f);
                }
                this.angle += 0.19634954084936207;
                ++this.ticks;
            }
        }, 0L, 1L);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> ((BukkitTask)task).cancel(), 100L);
    }

    private void playWitherEffect(Player player, Arena arena) {
        final Location loc = player.getLocation();
        BukkitTask task = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, new Runnable(){
            double angle = 0.0;
            double height = 0.0;
            int ticks = 0;

            @Override
            public void run() {
                if (this.ticks >= 60) {
                    return;
                }
                double x = Math.cos(this.angle) * 3.0;
                double z = Math.sin(this.angle) * 3.0;
                Location particleLoc = loc.clone().add(x, this.height, z);
                loc.getWorld().spawnParticle(Particle.LARGE_SMOKE, particleLoc, 5, 0.1, 0.1, 0.1, 0.01);
                loc.getWorld().spawnParticle(Particle.SOUL, particleLoc, 3);
                if (this.ticks % 10 == 0) {
                    loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_AMBIENT, 0.3f, 0.8f);
                }
                this.angle += 0.39269908169872414;
                this.height += 0.15;
                ++this.ticks;
            }
        }, 0L, 1L);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> ((BukkitTask)task).cancel(), 60L);
    }

    private void playMegaFireworkEffect(Player player, Arena arena) {
        for (int i = 0; i < 20; ++i) {
            int delay = i * 5;
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                for (int j = 0; j < 3; ++j) {
                    Location spawnLoc = player.getLocation().clone().add(ThreadLocalRandom.current().nextDouble(-5.0, 5.0), 0.0, ThreadLocalRandom.current().nextDouble(-5.0, 5.0));
                    this.spawnFirework(spawnLoc);
                }
            }, (long)delay);
        }
    }

    public Map<String, CageColor> getCageColors() {
        return Collections.unmodifiableMap(this.cageColors);
    }

    public Map<String, WinEffect> getWinEffects() {
        return Collections.unmodifiableMap(this.winEffects);
    }

    public CageColor getCageColor(String key) {
        return this.cageColors.get(key != null ? key.toUpperCase() : null);
    }

    public WinEffect getWinEffect(String key) {
        return this.winEffects.get(key != null ? key.toUpperCase() : null);
    }

    public record CageColor(String name, Material material, String displayName, String permission) {
    }

    public record WinEffect(String name, Material icon, String displayName, String permission, WinEffectHandler handler) {
    }

    @FunctionalInterface
    public static interface WinEffectHandler {
        public void play(Player var1, Arena var2);
    }
}

