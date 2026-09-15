/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.event.ClickEvent
 *  net.kyori.adventure.text.event.HoverEvent
 *  net.kyori.adventure.text.event.HoverEventSource
 *  org.bukkit.Sound
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package com.fortunepillars.commands.subcommands;

import com.fortunepillars.FortunePillars;
import com.fortunepillars.arena.Arena;
import com.fortunepillars.arena.GameState;
import com.fortunepillars.commands.subcommands.BaseSubCommand;
import com.fortunepillars.game.GameModeType;
import com.fortunepillars.gui.VotingGUI;
import com.fortunepillars.utils.BedrockCompat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VoteCommand
extends BaseSubCommand {
    public VoteCommand(FortunePillars plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "vote";
    }

    @Override
    public String getDescription() {
        return "Vote for a game mode";
    }

    @Override
    public String getUsage() {
        return "/tof vote [mode]";
    }

    @Override
    public String getPermission() {
        return "fortunepillars.player";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public int getMinArgs() {
        return 0;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String arg;
        Player player = (Player)sender;
        Arena arena = this.plugin.getArenaManager().getPlayerArena(player);
        if (arena == null) {
            player.sendMessage(this.plugin.getMessages().getWithPrefix("not-in-game"));
            return;
        }
        if (!this.isVotingAllowed(arena)) {
            player.sendMessage(FortunePillars.parseWithPrefix("<red>You can only vote during the waiting phase!"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }
        boolean isBedrockPlayer = BedrockCompat.isBedrockPlayer(player);
        if (args.length < 1) {
            if (isBedrockPlayer) {
                this.showTextVoteMenu(player, arena);
            } else {
                this.openVoteMenu(player, arena);
            }
            return;
        }
        switch (arg = args[0].toLowerCase()) {
            case "gui": 
            case "menu": {
                if (isBedrockPlayer) {
                    player.sendMessage(FortunePillars.parseWithPrefix("<yellow>Bedrock players use text voting. Showing options..."));
                    this.showTextVoteMenu(player, arena);
                } else {
                    new VotingGUI(this.plugin, player, arena).open();
                }
                return;
            }
            case "clear": 
            case "remove": {
                this.clearVote(player, arena);
                return;
            }
            case "random": {
                this.voteRandom(player, arena);
                return;
            }
            case "info": 
            case "status": {
                this.showVoteStatus(player, arena);
                return;
            }
        }
        GameModeType mode = GameModeType.fromString(arg);
        if (mode == null) {
            player.sendMessage(FortunePillars.parseWithPrefix("<red>Invalid game mode! <gray>Use <yellow>/tof vote</yellow> to see options."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            String suggestion = this.findSimilarMode(arg);
            if (suggestion != null) {
                player.sendMessage(FortunePillars.parseWithPrefix("<gray>Did you mean: <yellow>" + suggestion + "</yellow>?"));
            }
            return;
        }
        GameModeType currentVote = this.plugin.getVoteManager().getPlayerVote(player, arena);
        if (currentVote == mode) {
            player.sendMessage(FortunePillars.parseWithPrefix("<green>\u2713 Already voted for <gold>" + mode.getDisplayName()));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.0f);
        } else {
            this.plugin.getVoteManager().castVote(player, arena, mode);
            if (currentVote != null) {
                player.sendMessage(FortunePillars.parseWithPrefix("<green>\u2713 Changed vote to <gold>" + mode.getDisplayName()));
            } else {
                player.sendMessage(FortunePillars.parseWithPrefix("<green>\u2713 Voted for <gold>" + mode.getDisplayName()));
            }
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
        }
    }

    private void openVoteMenu(Player player, Arena arena) {
        new VotingGUI(this.plugin, player, arena).open();
    }

    private void showTextVoteMenu(Player player, Arena arena) {
        GameModeType currentVote = this.plugin.getVoteManager().getPlayerVote(player, arena);
        boolean isBedrockPlayer = BedrockCompat.isBedrockPlayer(player);
        player.sendMessage((Component)Component.empty());
        player.sendMessage(FortunePillars.parse("<gold><bold>\u2550\u2550\u2550\u2550\u2550\u2550\u2550 Vote for Game Mode \u2550\u2550\u2550\u2550\u2550\u2550\u2550</bold></gold>"));
        player.sendMessage((Component)Component.empty());
        if (currentVote != null) {
            player.sendMessage(FortunePillars.parse("<gray>Your current vote: <yellow>" + currentVote.getDisplayName() + "</yellow>"));
            player.sendMessage((Component)Component.empty());
        }
        for (GameModeType mode : GameModeType.values()) {
            int votes = this.plugin.getVoteManager().getVoteCount(arena, mode);
            boolean isSelected = mode == currentVote;
            String prefix = isSelected ? "<green>\u2713 " : "<gold>\u25b8 ";
            String voteText = "<dark_gray>[" + votes + " vote" + (votes != 1 ? "s" : "") + "]";
            if (isBedrockPlayer) {
                player.sendMessage(FortunePillars.parse(prefix + "<yellow>" + mode.getDisplayName() + "</yellow> " + voteText));
                player.sendMessage(FortunePillars.parse("  <gray>" + mode.getDescription().replace("<gray>", "")));
                player.sendMessage(FortunePillars.parse("  <aqua>Command: <white>/tof vote " + mode.name().toLowerCase()));
                continue;
            }
            Component modeComponent = FortunePillars.parse(prefix + "<yellow>" + mode.getDisplayName() + "</yellow> " + voteText).clickEvent(ClickEvent.runCommand((String)("/tof vote " + mode.name().toLowerCase()))).hoverEvent((HoverEventSource)HoverEvent.showText((Component)FortunePillars.parse(mode.getDescription() + "\n\n<yellow>Click to vote!")));
            player.sendMessage(modeComponent);
            player.sendMessage(FortunePillars.parse("  <gray>" + mode.getDescription().replace("<gray>", "")));
        }
        player.sendMessage((Component)Component.empty());
        if (!isBedrockPlayer) {
            Component guiButton = FortunePillars.parse("<green>[Open GUI]").clickEvent(ClickEvent.runCommand((String)"/tof vote gui")).hoverEvent((HoverEventSource)HoverEvent.showText((Component)FortunePillars.parse("<gray>Click to open voting GUI")));
            Component randomButton = FortunePillars.parse("<light_purple>[Random]").clickEvent(ClickEvent.runCommand((String)"/tof vote random")).hoverEvent((HoverEventSource)HoverEvent.showText((Component)FortunePillars.parse("<gray>Click to vote randomly")));
            Component clearButton = FortunePillars.parse("<red>[Clear Vote]").clickEvent(ClickEvent.runCommand((String)"/tof vote clear")).hoverEvent((HoverEventSource)HoverEvent.showText((Component)FortunePillars.parse("<gray>Click to remove your vote")));
            player.sendMessage(guiButton.append(FortunePillars.parse(" ")).append(randomButton).append(FortunePillars.parse(" ")).append(clearButton));
        } else {
            player.sendMessage(FortunePillars.parse("<gray>Commands: <white>/tof vote random <dark_gray>| <white>/tof vote clear"));
        }
        player.sendMessage((Component)Component.empty());
    }

    private void clearVote(Player player, Arena arena) {
        GameModeType currentVote = this.plugin.getVoteManager().getPlayerVote(player, arena);
        if (currentVote == null) {
            player.sendMessage(FortunePillars.parseWithPrefix("<yellow>You haven't voted yet!"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 0.8f);
            return;
        }
        this.plugin.getVoteManager().removeVote(player, arena);
        player.sendMessage(FortunePillars.parseWithPrefix("<aqua>Your vote for <white>" + currentVote.getDisplayName() + "</white> has been cleared!"));
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);
    }

    private void voteRandom(Player player, Arena arena) {
        GameModeType[] modes = GameModeType.values();
        GameModeType randomMode = modes[(int)(Math.random() * (double)modes.length)];
        this.plugin.getVoteManager().castVote(player, arena, randomMode);
        player.sendMessage(FortunePillars.parseWithPrefix("<light_purple>\ud83c\udfb2 Randomly voted for <yellow>" + randomMode.getDisplayName() + "</yellow>!"));
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.0f);
    }

    private void showVoteStatus(Player player, Arena arena) {
        int totalVotes = this.plugin.getVoteManager().getTotalVotes(arena);
        GameModeType leading = this.plugin.getVoteManager().getLeadingMode(arena);
        boolean isTied = this.plugin.getVoteManager().isTied(arena);
        player.sendMessage((Component)Component.empty());
        player.sendMessage(FortunePillars.parse("<gold><bold>\u2550\u2550\u2550\u2550\u2550\u2550\u2550 Vote Status \u2550\u2550\u2550\u2550\u2550\u2550\u2550</bold></gold>"));
        player.sendMessage((Component)Component.empty());
        player.sendMessage(FortunePillars.parse("<gray>Total votes: <white>" + totalVotes));
        if (leading != null) {
            String tieText = isTied ? " <red>(Tied!)" : "";
            player.sendMessage(FortunePillars.parse("<gray>Leading: <yellow>" + leading.getDisplayName() + tieText));
        } else {
            player.sendMessage(FortunePillars.parse("<gray>Leading: <dark_gray>No votes yet"));
        }
        player.sendMessage((Component)Component.empty());
        player.sendMessage(FortunePillars.parse("<gray>Vote breakdown:"));
        for (GameModeType mode : GameModeType.values()) {
            int votes = this.plugin.getVoteManager().getVoteCount(arena, mode);
            if (votes <= 0) continue;
            int percentage = totalVotes > 0 ? votes * 100 / totalVotes : 0;
            String bar = this.createVoteBar(votes, totalVotes);
            player.sendMessage(FortunePillars.parse("  <yellow>" + mode.getDisplayName() + "</yellow>: <white>" + votes + " <gray>(" + percentage + "%) " + bar));
        }
        GameModeType playerVote = this.plugin.getVoteManager().getPlayerVote(player, arena);
        player.sendMessage((Component)Component.empty());
        if (playerVote != null) {
            player.sendMessage(FortunePillars.parse("<gray>Your vote: <green>" + playerVote.getDisplayName()));
        } else {
            player.sendMessage(FortunePillars.parse("<gray>Your vote: <red>None <dark_gray>- Use /tof vote to vote!"));
        }
        player.sendMessage((Component)Component.empty());
    }

    private String createVoteBar(int votes, int totalVotes) {
        int barLength = 10;
        int filled = totalVotes > 0 ? (int)Math.round((double)votes / (double)totalVotes * (double)barLength) : 0;
        StringBuilder bar = new StringBuilder("<dark_gray>[");
        for (int i = 0; i < barLength; ++i) {
            if (i < filled) {
                bar.append("<green>|");
                continue;
            }
            bar.append("<dark_gray>|");
        }
        bar.append("<dark_gray>]");
        return bar.toString();
    }

    private boolean isVotingAllowed(Arena arena) {
        GameState state = arena.getState();
        return state == GameState.WAITING || state == GameState.STARTING;
    }

    private String findSimilarMode(String input) {
        input = input.toLowerCase();
        for (GameModeType mode : GameModeType.values()) {
            String modeName = mode.name().toLowerCase();
            String displayName = mode.getDisplayName().toLowerCase();
            if (!modeName.contains(input) && !displayName.contains(input) && !input.contains(modeName.substring(0, Math.min(3, modeName.length())))) continue;
            return mode.name().toLowerCase();
        }
        return null;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> completions = Arrays.stream(GameModeType.values()).map(mode -> mode.name().toLowerCase()).collect(Collectors.toList());
            completions.add("gui");
            completions.add("menu");
            completions.add("clear");
            completions.add("random");
            completions.add("info");
            return completions.stream().filter((String name) -> name.startsWith(args[0].toLowerCase())).sorted().collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

