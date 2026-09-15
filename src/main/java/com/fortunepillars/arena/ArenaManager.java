/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 */
package com.fortunepillars.arena;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.arena.GameState;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;

public class ArenaManager {
    private final FortunePillars plugin;
    private final Map<String, Arena> arenas;
    private final Map<UUID, Arena> playerArenaMap;

    public ArenaManager(FortunePillars plugin) {
        this.plugin = plugin;
        this.arenas = new ConcurrentHashMap<String, Arena>();
        this.playerArenaMap = new ConcurrentHashMap<UUID, Arena>();
    }

    public Arena createArena(String name) {
        if (this.arenas.containsKey(name.toLowerCase())) {
            return null;
        }
        Arena arena = new Arena(this.plugin, name.toLowerCase());
        this.arenas.put(name.toLowerCase(), arena);
        arena.save();
        return arena;
    }

    public boolean deleteArena(String name) {
        Arena arena = this.arenas.remove(name.toLowerCase());
        if (arena == null) {
            return false;
        }
        arena.delete();
        return true;
    }

    public Arena getArena(String name) {
        return this.arenas.get(name.toLowerCase());
    }

    public Arena getPlayerArena(Player player) {
        return this.playerArenaMap.get(player.getUniqueId());
    }

    public Arena getPlayerArena(UUID uuid) {
        return this.playerArenaMap.get(uuid);
    }

    public boolean isInArena(Player player) {
        return this.playerArenaMap.containsKey(player.getUniqueId());
    }

    public void setPlayerArena(Player player, Arena arena) {
        if (arena == null) {
            this.playerArenaMap.remove(player.getUniqueId());
        } else {
            this.playerArenaMap.put(player.getUniqueId(), arena);
        }
    }

    public boolean joinArena(Player player, String arenaName) {
        if (this.isInArena(player)) {
            player.sendMessage(this.plugin.getMessages().getWithPrefix("already-in-game"));
            return false;
        }
        Arena arena = this.getArena(arenaName);
        if (arena == null) {
            player.sendMessage(this.plugin.getMessages().getWithPrefix("arena-not-found", "arena", arenaName));
            return false;
        }
        if (arena.addPlayer(player)) {
            this.setPlayerArena(player, arena);
            return true;
        }
        return false;
    }

    public void leaveArena(Player player) {
        Arena arena = this.getPlayerArena(player);
        if (arena == null) {
            player.sendMessage(this.plugin.getMessages().getWithPrefix("not-in-game"));
            return;
        }
        this.setPlayerArena(player, null);
        arena.removePlayer(player, true);
    }

    public void forceRemovePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        Arena tracked = this.playerArenaMap.remove(uuid);
        if (tracked != null) {
            tracked.removePlayer(player, false);
        }
        for (Arena arena : this.getAllArenas()) {
            if (!arena.hasPlayer(uuid)) continue;
            arena.removePlayer(player, false);
        }
    }

    public Arena findBestArena() {
        return this.arenas.values().stream().filter(arena -> arena.getState().isJoinable()).filter(arena -> arena.getPlayerCount() < arena.getMaxPlayers()).filter(Arena::isSetupComplete).max(Comparator.comparingInt(Arena::getPlayerCount)).orElse(null);
    }

    public boolean quickJoin(Player player) {
        if (this.isInArena(player)) {
            player.sendMessage(this.plugin.getMessages().getWithPrefix("already-in-game"));
            return false;
        }
        Arena arena = this.findBestArena();
        if (arena == null) {
            player.sendMessage(this.plugin.getMessages().getWithPrefix("no-arenas-available"));
            return false;
        }
        return this.joinArena(player, arena.getName());
    }

    public Collection<Arena> getAllArenas() {
        return this.arenas.values();
    }

    public List<Arena> getJoinableArenas() {
        return this.arenas.values().stream().filter(arena -> arena.getState().isJoinable()).filter(Arena::isSetupComplete).collect(Collectors.toList());
    }

    public List<String> getArenaNames() {
        return new ArrayList<String>(this.arenas.keySet());
    }

    public void loadAllArenas() {
        File arenasFolder = new File(this.plugin.getDataFolder(), "arenas");
        if (!arenasFolder.exists()) {
            arenasFolder.mkdirs();
            return;
        }
        File[] files = arenasFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return;
        }
        for (File file : files) {
            String arenaName = file.getName().replace(".yml", "");
            Arena arena = new Arena(this.plugin, arenaName);
            arena.load();
            this.arenas.put(arenaName.toLowerCase(), arena);
        }
        this.plugin.getLogger().info("Loaded " + this.arenas.size() + " arena(s)");
    }

    public void saveAllArenas() {
        for (Arena arena : this.arenas.values()) {
            arena.save();
        }
    }

    public void handlePlayerQuit(Player player) {
        Arena arena = this.getPlayerArena(player);
        if (arena != null) {
            this.setPlayerArena(player, null);
            arena.removePlayer(player, false);
        }
    }

    public int getTotalPlayersInArenas() {
        return this.arenas.values().stream().mapToInt(Arena::getPlayerCount).sum();
    }

    public int getActiveGameCount() {
        return (int)this.arenas.values().stream().filter(arena -> arena.getState() == GameState.IN_GAME).count();
    }
}

