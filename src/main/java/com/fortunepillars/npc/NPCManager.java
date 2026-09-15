/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.EntityType
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.Villager
 *  org.bukkit.entity.Villager$Profession
 *  org.bukkit.entity.Villager$Type
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerInteractEntityEvent
 *  org.bukkit.plugin.Plugin
 */
package com.fortunepillars.npc;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.Plugin;

public class NPCManager
implements Listener {
    private final FortunePillars plugin;
    private final Map<UUID, String> npcArenaMap;
    private final File npcsFile;

    public NPCManager(FortunePillars plugin) {
        this.plugin = plugin;
        this.npcArenaMap = new HashMap<UUID, String>();
        this.npcsFile = new File(plugin.getDataFolder(), "npcs.yml");
        this.loadNPCs();
        plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)plugin);
    }

    public void createNPC(Location location, String arenaName) {
        Villager villager = (Villager)location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        villager.setCustomName(FortunePillars.parse("<gold><bold>Join " + arenaName.toUpperCase() + "</bold></gold>").toString());
        villager.setCustomNameVisible(true);
        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.setSilent(true);
        villager.setProfession(Villager.Profession.NITWIT);
        villager.setVillagerType(Villager.Type.PLAINS);
        this.npcArenaMap.put(villager.getUniqueId(), arenaName);
        this.saveNPCs();
    }

    public void removeNPC(UUID entityUUID) {
        this.npcArenaMap.remove(entityUUID);
        this.saveNPCs();
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!entity.getUniqueId().equals(entityUUID)) continue;
                entity.remove();
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Villager)) {
            return;
        }
        UUID entityUUID = event.getRightClicked().getUniqueId();
        String arenaName = this.npcArenaMap.get(entityUUID);
        if (arenaName == null) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        Arena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            player.sendMessage(FortunePillars.parseWithPrefix("<red>This arena no longer exists!"));
            return;
        }
        if (this.plugin.getArenaManager().isInArena(player)) {
            player.sendMessage(FortunePillars.parseWithPrefix("<red>You're already in a game!"));
            return;
        }
        this.plugin.getArenaManager().joinArena(player, arenaName);
    }

    private void loadNPCs() {
        if (!this.npcsFile.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration((File)this.npcsFile);
        if (config.contains("npcs")) {
            for (String key : config.getConfigurationSection("npcs").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    String arenaName = config.getString("npcs." + key);
                    this.npcArenaMap.put(uuid, arenaName);
                }
                catch (Exception e) {
                    this.plugin.getLogger().warning("Failed to load NPC: " + key);
                }
            }
        }
        this.plugin.getLogger().info("Loaded " + this.npcArenaMap.size() + " join NPCs.");
    }

    private void saveNPCs() {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, String> entry : this.npcArenaMap.entrySet()) {
            config.set("npcs." + entry.getKey().toString(), (Object)entry.getValue());
        }
        try {
            config.save(this.npcsFile);
        }
        catch (IOException e) {
            this.plugin.getLogger().warning("Failed to save NPCs: " + e.getMessage());
        }
    }

    public Map<UUID, String> getNPCs() {
        return new HashMap<UUID, String>(this.npcArenaMap);
    }
}

