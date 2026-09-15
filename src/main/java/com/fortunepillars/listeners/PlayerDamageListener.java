/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.Projectile
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.entity.EntityDamageEvent
 */
package com.fortunepillars.listeners;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.arena.GameState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerDamageListener
implements Listener {
    private final FortunePillars plugin;

    public PlayerDamageListener(FortunePillars plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player)entity;
        Arena arena = this.plugin.getArenaManager().getPlayerArena(player);
        if (arena == null) {
            return;
        }
        if (!this.plugin.getGameManager().canPlayerTakeDamage(player)) {
            event.setCancelled(true);
            return;
        }
        if (arena.isSpectator(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (!arena.getState().allowsPlayerDamage()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player victim = (Player)entity;
        Player attacker = this.getAttacker(event);
        if (attacker == null) {
            return;
        }
        Arena victimArena = this.plugin.getArenaManager().getPlayerArena(victim);
        Arena attackerArena = this.plugin.getArenaManager().getPlayerArena(attacker);
        if (victimArena == null && attackerArena == null) {
            return;
        }
        if (victimArena == null || attackerArena == null) {
            event.setCancelled(true);
            return;
        }
        if (!victimArena.getName().equals(attackerArena.getName())) {
            event.setCancelled(true);
            return;
        }
        Arena arena = victimArena;
        if (!this.plugin.getGameManager().canPlayerPvP(attacker, victim)) {
            event.setCancelled(true);
            if (arena.isGracePeriodActive()) {
                attacker.sendMessage(FortunePillars.parseWithPrefix("<red>Grace period is active! No PvP yet."));
            }
            return;
        }
        if (arena.getState() != GameState.IN_GAME) {
            event.setCancelled(true);
            return;
        }
        if (arena.isSpectator(victim.getUniqueId()) || arena.isSpectator(attacker.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (!arena.isPlayerAlive(victim.getUniqueId()) || !arena.isPlayerAlive(attacker.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    private Player getAttacker(EntityDamageByEntityEvent event) {
        Projectile projectile;
        if (event.getDamager() instanceof Player) {
            return (Player)event.getDamager();
        }
        Entity entity = event.getDamager();
        if (entity instanceof Projectile && (projectile = (Projectile)entity).getShooter() instanceof Player) {
            return (Player)projectile.getShooter();
        }
        return null;
    }
}

