package fr.kiza.leagueuhc.ui.scoreboard;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.mrmicky.fastboard.FastBoard;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class Scoreboard implements Listener {

    protected final LeagueUHC instance;
    protected final Server server;

    private final Map<UUID, FastBoard> boards = new HashMap<>();
    private int ipCharIndex = 0;
    private int cooldown = 0;

    public Scoreboard(LeagueUHC instance) {
        this.instance = instance;
        this.server = instance.getServer();

        this.server.getPluginManager().registerEvents(this, this.instance);
        this.server.getScheduler().runTaskTimer(this.instance, () -> {
            this.boards.values().forEach(this::updateBoard);
        }, 0, 2);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onLogin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final FastBoard board = new FastBoard(player);

        board.updateTitle(ChatColor.GOLD + "" + ChatColor.BOLD + "LEAGUE" + ChatColor.YELLOW + ChatColor.BOLD + "UHC");
        this.boards.put(player.getUniqueId(), board);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onLogout(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final FastBoard board = this.boards.remove(player.getUniqueId());

        if (board != null) board.delete();
    }

    private void updateBoard(final FastBoard board) {
        final String currentState = this.instance.getGameEngine().getCurrentState();

        switch (currentState) {
            case "PLAYING":
                this.updatePlayingBoard(board);
                break;
            case "FINISHED":
                this.updateFinishedBoard(board);
            default:
                this.updateDefaultBoard(board);
                break;
        }
    }

    private void updatePlayingBoard(final FastBoard board) {
        final int alivePlayers = this.instance.getGameEngine().getContext().getAlivePlayers().size();
        final int totalPlayers = this.instance.getGameEngine().getContext().getPlayers().size();
        final boolean isAlive = this.instance.getGameEngine().getContext().getAlivePlayers().contains(board.getPlayer().getUniqueId());

        List<String> lines = new ArrayList<>();
        lines.add("");

        if (isAlive) {
            lines.add(ChatColor.GREEN + "┃ ⚔ " + ChatColor.BOLD + "EN VIE");
        } else {
            lines.add(ChatColor.RED + "┃ ☠ " + ChatColor.BOLD + "SPECTATEUR");
        }

        lines.add("");
        lines.add(ChatColor.GOLD + "┃ " + ChatColor.YELLOW + "Joueurs: " + ChatColor.WHITE + alivePlayers + ChatColor.DARK_GRAY + "/" + ChatColor.GRAY + totalPlayers);

        lines.add("");
        lines.add(ChatColor.GOLD + "┃ " + ChatColor.YELLOW + "Kills: " + ChatColor.WHITE + "0");

        lines.add("");
        lines.add(getAnimatedIP());

        board.updateLines(lines);
    }

    private void updateFinishedBoard(final FastBoard board) {
        List<String> lines = new ArrayList<>();
        lines.add("");
        lines.add(ChatColor.GOLD + "┃ " + ChatColor.YELLOW + ChatColor.BOLD + "PARTIE TERMINÉE");
        lines.add("");
        lines.add(ChatColor.GRAY + "┃ En attente du prochain");
        lines.add(ChatColor.GRAY + "┃ redémarrage...");
        lines.add("");
        lines.add(ChatColor.GOLD + "┃ " + ChatColor.YELLOW + "Joueurs: " + ChatColor.WHITE + this.server.getOnlinePlayers().size());
        lines.add("");
        lines.add(getAnimatedIP());

        board.updateLines(lines);
    }

    private void updateDefaultBoard(final FastBoard board) {
        List<String> lines = new ArrayList<>();
        lines.add("");
        lines.add(ChatColor.YELLOW + "┃ " + board.getPlayer().getName());
        lines.add(ChatColor.GRAY + "┃ " + this.instance.getGameEngine().getCurrentState());
        lines.add("");
        lines.add(ChatColor.GOLD + "┃ " + ChatColor.YELLOW + "Joueurs: " + ChatColor.WHITE + this.server.getOnlinePlayers().size());
        lines.add("");
        lines.add(getAnimatedIP());

        board.updateLines(lines);
    }

    private String getAnimatedIP() {
        String ip = "play.leagueuhc.fr";

        if (this.cooldown > 0) {
            this.cooldown--;
            return ChatColor.YELLOW + ip;
        }

        StringBuilder formattedIp = new StringBuilder();

        if (this.ipCharIndex > 0) {
            formattedIp.append(ChatColor.YELLOW).append(ip, 0, this.ipCharIndex - 1);
            formattedIp.append(ChatColor.GOLD).append(ip.charAt(this.ipCharIndex - 1));
        } else {
            formattedIp.append(ChatColor.YELLOW).append(ip, 0, this.ipCharIndex);
        }

        formattedIp.append(ChatColor.WHITE).append(ip.charAt(this.ipCharIndex));

        if (this.ipCharIndex + 1 < ip.length()) {
            formattedIp.append(ChatColor.GOLD).append(ip.charAt(this.ipCharIndex + 1));

            if (this.ipCharIndex + 2 < ip.length()) {
                formattedIp.append(ChatColor.YELLOW).append(ip.substring(this.ipCharIndex + 2));
            }

            this.ipCharIndex++;
        } else {
            this.ipCharIndex = 0;
            this.cooldown = 25;
        }

        return formattedIp.toString();
    }
}