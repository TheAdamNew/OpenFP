/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.World
 *  org.bukkit.block.Block
 *  org.bukkit.block.Sign
 *  org.bukkit.block.sign.Side
 *  org.bukkit.block.sign.SignSide
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.block.BlockBreakEvent
 *  org.bukkit.event.block.SignChangeEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package com.fortunepillars.signs;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.arena.GameState;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class SignManager
implements Listener {
    private final FortunePillars plugin;
    private final Map<Location, String> arenaSigns;
    private final File signsFile;
    private FileConfiguration signsConfig;
    private BukkitTask updateTask;

    public SignManager(FortunePillars plugin) {
        this.plugin = plugin;
        this.arenaSigns = new ConcurrentHashMap<Location, String>();
        this.signsFile = new File(plugin.getDataFolder(), "signs.yml");
        this.loadSigns();
        this.startUpdateTask();
    }

    private void loadSigns() {
        if (!this.signsFile.exists()) {
            try {
                this.signsFile.createNewFile();
            }
            catch (IOException e) {
                this.plugin.getLogger().severe("Could not create signs.yml: " + e.getMessage());
                return;
            }
        }
        this.signsConfig = YamlConfiguration.loadConfiguration((File)this.signsFile);
        if (this.signsConfig.contains("signs")) {
            for (String key : this.signsConfig.getConfigurationSection("signs").getKeys(false)) {
                World world;
                String path = "signs." + key;
                String worldName = this.signsConfig.getString(path + ".world");
                int x = this.signsConfig.getInt(path + ".x");
                int y = this.signsConfig.getInt(path + ".y");
                int z = this.signsConfig.getInt(path + ".z");
                String arenaName = this.signsConfig.getString(path + ".arena");
                if (worldName == null || arenaName == null || (world = Bukkit.getWorld((String)worldName)) == null) continue;
                Location loc = new Location(world, (double)x, (double)y, (double)z);
                this.arenaSigns.put(loc, arenaName);
            }
        }
        this.plugin.getLogger().info("Loaded " + this.arenaSigns.size() + " arena sign(s).");
    }

    private void saveSigns() {
        this.signsConfig = new YamlConfiguration();
        int index = 0;
        for (Map.Entry<Location, String> entry : this.arenaSigns.entrySet()) {
            Location loc = entry.getKey();
            String arena = entry.getValue();
            String path = "signs." + index;
            this.signsConfig.set(path + ".world", (Object)loc.getWorld().getName());
            this.signsConfig.set(path + ".x", (Object)loc.getBlockX());
            this.signsConfig.set(path + ".y", (Object)loc.getBlockY());
            this.signsConfig.set(path + ".z", (Object)loc.getBlockZ());
            this.signsConfig.set(path + ".arena", (Object)arena);
            ++index;
        }
        try {
            this.signsConfig.save(this.signsFile);
        }
        catch (IOException e) {
            this.plugin.getLogger().severe("Could not save signs.yml: " + e.getMessage());
        }
    }

    private void startUpdateTask() {
        this.updateTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, this::updateAllSigns, 0L, 40L);
    }

    private void updateAllSigns() {
        Iterator<Map.Entry<Location, String>> iterator = this.arenaSigns.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Location, String> entry = iterator.next();
            Location loc = entry.getKey();
            String arenaName = entry.getValue();
            Block block = loc.getBlock();
            if (!this.isSign(block)) {
                iterator.remove();
                continue;
            }
            Arena arena = this.plugin.getArenaManager().getArena(arenaName);
            this.updateSign((Sign)block.getState(), arena, arenaName);
        }
        this.saveSigns();
    }

    private void updateSign(Sign sign, Arena arena, String arenaName) {
        SignSide side = sign.getSide(Side.FRONT);
        if (arena == null) {
            side.line(0, FortunePillars.parse("<gold><bold>[FortunePillars]</bold></gold>"));
            side.line(1, FortunePillars.parse("<red>ERROR"));
            side.line(2, FortunePillars.parse("<red>Arena not found"));
            side.line(3, FortunePillars.parse(""));
        } else {
            GameState state = arena.getState();
            int playerCount = arena.getPlayerCount();
            int maxPlayers = arena.getMaxPlayers();
            side.line(0, FortunePillars.parse("<gold><bold>[FortunePillars]</bold></gold>"));
            side.line(1, FortunePillars.parse("<yellow>" + arenaName.toUpperCase()));
            side.line(2, FortunePillars.parse(state.getColoredName()));
            side.line(3, FortunePillars.parse("<gray>" + playerCount + "/" + maxPlayers));
        }
        sign.update();
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        String arenaName;
        String line0;
        Player player = event.getPlayer();
        if (!player.hasPermission("fortunepillars.admin")) {
            return;
        }
        String string = line0 = event.line(0) != null ? PlainTextComponentSerializer.plainText().serialize(event.line(0)).toLowerCase() : "";
        if (!line0.contains("[fortunepillars]") && !line0.contains("[fp]")) {
            return;
        }
        String string2 = arenaName = event.line(1) != null ? PlainTextComponentSerializer.plainText().serialize(event.line(1)).toLowerCase() : "";
        if (arenaName.isEmpty()) {
            player.sendMessage(FortunePillars.parseWithPrefix("<red>Please specify an arena name on line 2!"));
            return;
        }
        Arena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            player.sendMessage(FortunePillars.parseWithPrefix("<yellow>Warning: Arena '" + arenaName + "' not found. Sign created anyway."));
        }
        Location loc = event.getBlock().getLocation();
        this.arenaSigns.put(loc, arenaName);
        this.saveSigns();
        player.sendMessage(FortunePillars.parseWithPrefix("<green>Arena sign created for <yellow>" + arenaName + "</yellow>!"));
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            Block block = event.getBlock();
            if (this.isSign(block)) {
                this.updateSign((Sign)block.getState(), arena, arenaName);
            }
        }, 2L);
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null || !this.isSign(block)) {
            return;
        }
        Location loc = block.getLocation();
        String arenaName = this.arenaSigns.get(loc);
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
            player.sendMessage(FortunePillars.parseWithPrefix("<red>You're already in an arena!"));
            return;
        }
        if (!arena.getState().isJoinable()) {
            player.sendMessage(FortunePillars.parseWithPrefix("<red>This arena is not joinable right now!"));
            return;
        }
        this.plugin.getArenaManager().joinArena(player, arenaName);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!this.isSign(block)) {
            return;
        }
        Location loc = block.getLocation();
        if (!this.arenaSigns.containsKey(loc)) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.hasPermission("fortunepillars.admin")) {
            event.setCancelled(true);
            player.sendMessage(FortunePillars.parseWithPrefix("<red>You cannot break arena signs!"));
            return;
        }
        this.arenaSigns.remove(loc);
        this.saveSigns();
        player.sendMessage(FortunePillars.parseWithPrefix("<green>Arena sign removed!"));
    }

    private boolean isSign(Block block) {
        if (block == null) {
            return false;
        }
        Material type = block.getType();
        return type.name().contains("SIGN");
    }

    public void shutdown() {
        if (this.updateTask != null) {
            this.updateTask.cancel();
        }
        this.saveSigns();
    }

    public Map<Location, String> getArenaSigns() {
        return Collections.unmodifiableMap(this.arenaSigns);
    }

    public int getSignCount() {
        return this.arenaSigns.size();
    }
}

