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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Scoreboard implements Listener {

    protected final LeagueUHC instance;
    protected final Server server;

    private final Map<UUID, FastBoard> boards = new HashMap<>();

    public Scoreboard(LeagueUHC instance) {
        this.instance = instance;
        this.server = instance.getServer();

        this.server.getPluginManager().registerEvents(this, this.instance);
        this.server.getScheduler().runTaskTimer(this.instance, () -> this.boards.values().forEach(this::updateBoard), 0, 20);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onLogin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final FastBoard board = new FastBoard(player);

        board.updateTitle(ChatColor.GOLD + "" + ChatColor.BOLD + "LeagueUHC - 1.0.0");
        this.boards.put(player.getUniqueId(), board);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onLogout(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final FastBoard board = this.boards.remove(player.getUniqueId());

        if (board != null) board.delete();
    }

    private void updateBoard(final FastBoard board) {
        final Player player = board.getPlayer();

        board.updateLines(
                "",
                ChatColor.YELLOW + "    Hi, " + ChatColor.AQUA + player.getName(),
                ChatColor.YELLOW + "Online: " + ChatColor.LIGHT_PURPLE + this.server.getOnlinePlayers().size() + "/" + this.instance.getGameEngine().getContext().getMaxPlayers(),
                "",
                ChatColor.RED + this.instance.getGameEngine().getCurrentState(),
                ""
        );
    }
}
