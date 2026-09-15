/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.persistence.PersistentDataContainer
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.potion.PotionEffect
 */
package com.fortunepillars.player;

import com.fortunepillars.arena.Arena;
import com.fortunepillars.cosmetics.CosmeticType;
import com.fortunepillars.cosmetics.CosmeticsManager;
import java.util.Collection;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;

public class PlayerData {
    public static final NamespacedKey KEY_IN_GAME = new NamespacedKey("fortunepillars", "in_game");
    public static final NamespacedKey KEY_ARENA = new NamespacedKey("fortunepillars", "arena");
    public static final NamespacedKey KEY_SPECTATOR = new NamespacedKey("fortunepillars", "spectator");
    private final UUID uuid;
    private final Arena arena;
    private ItemStack[] savedInventory;
    private ItemStack[] savedArmor;
    private ItemStack[] savedExtraContents;
    private ItemStack savedOffhand;
    private Location savedLocation;
    private GameMode savedGameMode;
    private double savedHealth;
    private int savedFoodLevel;
    private float savedSaturation;
    private int savedLevel;
    private float savedExp;
    private Collection<PotionEffect> savedPotionEffects;
    private int savedFireTicks;
    private int cageNumber;
    private boolean isSpectator;
    private int kills;
    private Material cageColor;
    private String trailEffect;

    public PlayerData(UUID uuid, Arena arena) {
        CosmeticsManager cosmetics;
        this.uuid = uuid;
        this.arena = arena;
        this.isSpectator = false;
        this.kills = 0;
        this.cageColor = Material.WHITE_STAINED_GLASS;
        this.trailEffect = "NONE";
        Player player = Bukkit.getPlayer((UUID)uuid);
        if (player != null && arena.getPlugin() != null && (cosmetics = arena.getPlugin().getCosmeticsManager()) != null) {
            this.cageColor = cosmetics.getPlayerCageMaterial(player);
            String equippedTrail = cosmetics.getSelectedCosmetic(player, CosmeticType.TRAIL);
            if (equippedTrail != null) {
                this.trailEffect = equippedTrail;
            }
        }
    }

    public void savePlayerState(Player player) {
        this.savedInventory = (ItemStack[])player.getInventory().getContents().clone();
        this.savedArmor = (ItemStack[])player.getInventory().getArmorContents().clone();
        this.savedExtraContents = (ItemStack[])player.getInventory().getExtraContents().clone();
        this.savedOffhand = player.getInventory().getItemInOffHand().clone();
        this.savedLocation = player.getLocation().clone();
        this.savedGameMode = player.getGameMode();
        this.savedHealth = player.getHealth();
        this.savedFoodLevel = player.getFoodLevel();
        this.savedSaturation = player.getSaturation();
        this.savedLevel = player.getLevel();
        this.savedExp = player.getExp();
        this.savedPotionEffects = player.getActivePotionEffects();
        this.savedFireTicks = player.getFireTicks();
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.set(KEY_IN_GAME, PersistentDataType.BYTE, (byte) 1);
        pdc.set(KEY_ARENA, PersistentDataType.STRING, this.arena.getName());
        pdc.set(KEY_SPECTATOR, PersistentDataType.BYTE, (byte) 0);
    }

    public void restorePlayerState(Player player) {
        player.getInventory().clear();
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        if (this.savedInventory != null) {
            player.getInventory().setContents(this.savedInventory);
        }
        if (this.savedArmor != null) {
            player.getInventory().setArmorContents(this.savedArmor);
        }
        if (this.savedExtraContents != null) {
            player.getInventory().setExtraContents(this.savedExtraContents);
        }
        if (this.savedOffhand != null) {
            player.getInventory().setItemInOffHand(this.savedOffhand);
        }
        player.setGameMode(this.savedGameMode != null ? this.savedGameMode : GameMode.SURVIVAL);
        player.setHealth(Math.min(this.savedHealth, player.getMaxHealth()));
        player.setFoodLevel(this.savedFoodLevel);
        player.setSaturation(this.savedSaturation);
        player.setLevel(this.savedLevel);
        player.setExp(this.savedExp);
        player.setFireTicks(this.savedFireTicks);
        if (this.savedPotionEffects != null) {
            for (PotionEffect effect2 : this.savedPotionEffects) {
                player.addPotionEffect(effect2);
            }
        }
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.remove(KEY_IN_GAME);
        pdc.remove(KEY_ARENA);
        pdc.remove(KEY_SPECTATOR);
        this.arena.getPlugin().getLogger().info("Cleared persistent data for " + player.getName());
    }

    public void setAsSpectator(Player player) {
        this.isSpectator = true;
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.set(KEY_SPECTATOR, PersistentDataType.BYTE, (byte) 1);
    }

    public static void clearSpectatorData(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.remove(KEY_IN_GAME);
        pdc.remove(KEY_ARENA);
        pdc.remove(KEY_SPECTATOR);
        if (player.getGameMode() == GameMode.SPECTATOR) {
            player.setGameMode(GameMode.SURVIVAL);
        }
        player.setInvisible(false);
        player.setInvulnerable(false);
    }

    public static boolean isInGame(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        return pdc.has(KEY_IN_GAME, PersistentDataType.BYTE);
    }

    public static boolean isSpectator(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        Byte value = (Byte)pdc.get(KEY_SPECTATOR, PersistentDataType.BYTE);
        return value != null && value == 1;
    }

    public static String getArenaName(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        String name = (String)pdc.get(KEY_ARENA, PersistentDataType.STRING);
        return name != null ? name : "None";
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public Arena getArena() {
        return this.arena;
    }

    public int getCageNumber() {
        return this.cageNumber;
    }

    public void setCageNumber(int cageNumber) {
        this.cageNumber = cageNumber;
    }

    public boolean isSpectator() {
        return this.isSpectator;
    }

    public int getKills() {
        return this.kills;
    }

    public void addKill() {
        ++this.kills;
    }

    public Material getCageColor() {
        return this.cageColor;
    }

    public void setCageColor(Material cageColor) {
        this.cageColor = cageColor;
    }

    public String getTrailEffect() {
        return this.trailEffect;
    }

    public void setTrailEffect(String trailEffect) {
        this.trailEffect = trailEffect;
    }

    public Location getSavedLocation() {
        return this.savedLocation;
    }
}

