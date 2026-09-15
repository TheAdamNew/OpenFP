/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 */
package com.fortunepillars.voting;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.game.GameModeType;
import com.fortunepillars.gui.VotingGUI;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class VoteManager {
    private final FortunePillars plugin;

    public VoteManager(FortunePillars plugin) {
        this.plugin = plugin;
    }

    public void castVote(Player player, Arena arena, GameModeType mode) {
        if (arena == null || mode == null) {
            return;
        }
        GameModeType previousVote = arena.getVotes().get(player.getUniqueId());
        arena.getVotes().put(player.getUniqueId(), mode);
        if (previousVote != mode && this.plugin.getConfigManager().isChatNotificationEnabled("vote-cast")) {
            int voteCount = this.getVoteCount(arena, mode);
            String changeText = previousVote != null ? " <dark_gray>(changed from " + previousVote.getDisplayName() + ")" : "";
            arena.broadcastMessage(FortunePillars.parseWithPrefix("<yellow>" + player.getName() + "</yellow> <gray>voted for</gray> <gold>" + mode.getDisplayName() + "</gold> <dark_gray>[" + voteCount + " vote" + (voteCount != 1 ? "s" : "") + "]" + changeText));
        }
    }

    public void removeVote(Player player, Arena arena) {
        if (arena == null) {
            return;
        }
        arena.getVotes().remove(player.getUniqueId());
    }

    public GameModeType getPlayerVote(Player player, Arena arena) {
        if (arena == null) {
            return null;
        }
        return arena.getVotes().get(player.getUniqueId());
    }

    public boolean hasVoted(Player player, Arena arena) {
        return this.getPlayerVote(player, arena) != null;
    }

    public boolean hasVotedFor(Player player, Arena arena, GameModeType mode) {
        GameModeType vote = this.getPlayerVote(player, arena);
        return vote != null && vote == mode;
    }

    public int getVoteCount(Arena arena, GameModeType mode) {
        if (arena == null || mode == null) {
            return 0;
        }
        return (int)arena.getVotes().values().stream().filter(v -> v == mode).count();
    }

    public int getTotalVotes(Arena arena) {
        if (arena == null) {
            return 0;
        }
        return arena.getVotes().size();
    }

    public GameModeType calculateWinningMode(Arena arena) {
        if (arena == null) {
            return GameModeType.NORMAL;
        }
        EnumMap<GameModeType, Integer> voteCounts = new EnumMap<GameModeType, Integer>(GameModeType.class);
        for (GameModeType mode : GameModeType.values()) {
            voteCounts.put(mode, 0);
        }
        for (GameModeType vote : arena.getVotes().values()) {
            voteCounts.merge(vote, 1, Integer::sum);
        }
        GameModeType winner = GameModeType.NORMAL;
        int maxVotes = 0;
        ArrayList<GameModeType> tiedModes = new ArrayList<GameModeType>();
        for (Map.Entry entry : voteCounts.entrySet()) {
            if ((Integer)entry.getValue() > maxVotes) {
                maxVotes = (Integer)entry.getValue();
                winner = (GameModeType)((Object)entry.getKey());
                tiedModes.clear();
                tiedModes.add((GameModeType)((Object)entry.getKey()));
                continue;
            }
            if ((Integer)entry.getValue() != maxVotes || maxVotes <= 0) continue;
            tiedModes.add((GameModeType)((Object)entry.getKey()));
        }
        if (tiedModes.size() > 1) {
            winner = (GameModeType)((Object)tiedModes.get(new Random().nextInt(tiedModes.size())));
            if (this.plugin.getConfigManager().isChatNotificationEnabled("vote-cast")) {
                arena.broadcastMessage(FortunePillars.parseWithPrefix("<yellow>Vote tie! <gold>" + winner.getDisplayName() + "</gold> was randomly selected!"));
            }
        }
        if (maxVotes == 0) {
            winner = GameModeType.NORMAL;
        }
        return winner;
    }

    public List<String> getVoteResults(Arena arena) {
        ArrayList<String> results = new ArrayList<String>();
        if (arena == null) {
            return results;
        }
        int totalVotes = this.getTotalVotes(arena);
        for (GameModeType mode : GameModeType.values()) {
            int votes = this.getVoteCount(arena, mode);
            if (votes <= 0) continue;
            int percentage = totalVotes > 0 ? votes * 100 / totalVotes : 0;
            results.add("<yellow>" + mode.getDisplayName() + "</yellow>: <white>" + votes + " vote" + (votes != 1 ? "s" : "") + " <gray>(" + percentage + "%)");
        }
        if (results.isEmpty()) {
            results.add("<gray>No votes yet!");
        }
        return results;
    }

    public void announceResults(Arena arena) {
        if (arena == null) {
            return;
        }
        if (!this.plugin.getConfigManager().isChatNotificationEnabled("game-settings")) {
            return;
        }
        GameModeType winner = this.calculateWinningMode(arena);
        arena.broadcastMessage((Component)Component.empty());
        arena.broadcastMessage(FortunePillars.parse("<gold><bold>\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550 Vote Results \u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550</bold></gold>"));
        arena.broadcastMessage((Component)Component.empty());
        for (String result : this.getVoteResults(arena)) {
            arena.broadcastMessage(FortunePillars.parse("  " + result));
        }
        arena.broadcastMessage((Component)Component.empty());
        arena.broadcastMessage(FortunePillars.parse("<green>Winner: <gold><bold>" + winner.getDisplayName() + "</bold></gold>"));
        arena.broadcastMessage(FortunePillars.parse("<gray>" + winner.getDescription()));
        arena.broadcastMessage((Component)Component.empty());
        for (Player p : arena.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.0f);
        }
    }

    public void clearVotes(Arena arena) {
        if (arena != null) {
            arena.getVotes().clear();
        }
    }

    public void openVoteGUI(Player player, Arena arena) {
        new VotingGUI(this.plugin, player, arena).open();
    }

    public void handleVoteClick(Player player, Arena arena, int slot) {
        GameModeType mode = this.getGameModeFromSlot(slot);
        if (mode != null) {
            GameModeType currentVote = this.getPlayerVote(player, arena);
            if (currentVote == mode) {
                this.removeVote(player, arena);
                player.sendMessage(FortunePillars.parseWithPrefix("<yellow>Vote removed!"));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.8f);
            } else {
                this.castVote(player, arena, mode);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
            }
            player.closeInventory();
        }
    }

    private GameModeType getGameModeFromSlot(int slot) {
        return switch (slot) {
            case 1 -> GameModeType.NORMAL;
            case 2 -> GameModeType.BALANCED;
            case 3 -> GameModeType.SWAPPER;
            case 4 -> GameModeType.SHUFFLE;
            case 5 -> GameModeType.LAVA_RISING;
            case 6 -> GameModeType.BORDER_SHRINK;
            case 7 -> GameModeType.SPEED_UHC;
            case 8 -> GameModeType.TNT_RAIN;
            default -> null;
        };
    }

    public void sendVoteReminder(Arena arena) {
        if (arena == null) {
            return;
        }
        if (!this.plugin.getConfigManager().isChatNotificationEnabled("vote-cast")) {
            return;
        }
        for (Player player : arena.getOnlinePlayers()) {
            if (this.hasVoted(player, arena)) continue;
            player.sendMessage(FortunePillars.parseWithPrefix("<yellow>Don't forget to vote! Use <gold>/tof vote</gold> or click the vote item!"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.2f);
        }
    }

    public GameModeType getLeadingMode(Arena arena) {
        if (arena == null) {
            return null;
        }
        GameModeType leading = null;
        int maxVotes = 0;
        for (GameModeType mode : GameModeType.values()) {
            int votes = this.getVoteCount(arena, mode);
            if (votes <= maxVotes) continue;
            maxVotes = votes;
            leading = mode;
        }
        return leading;
    }

    public boolean isTied(Arena arena) {
        if (arena == null) {
            return false;
        }
        int maxVotes = 0;
        int leadingCount = 0;
        for (GameModeType mode : GameModeType.values()) {
            int votes = this.getVoteCount(arena, mode);
            if (votes > maxVotes) {
                maxVotes = votes;
                leadingCount = 1;
                continue;
            }
            if (votes != maxVotes || maxVotes <= 0) continue;
            ++leadingCount;
        }
        return leadingCount > 1;
    }
}

