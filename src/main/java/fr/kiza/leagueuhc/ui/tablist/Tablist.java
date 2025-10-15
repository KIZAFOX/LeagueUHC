package fr.kiza.leagueuhc.ui.tablist;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.packets.builder.TablistBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Tablist implements Listener {

    protected final LeagueUHC instance;

    private final Map<UUID, BukkitRunnable> TAB_MAP = new ConcurrentHashMap<>();

    public Tablist(LeagueUHC instance) {
        this.instance = instance;
        this.instance.getServer().getPluginManager().registerEvents(this, instance);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onLogin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        final BukkitRunnable tabTask = new BukkitRunnable() {
            @Override
            public void run() {
                final int size = Bukkit.getOnlinePlayers().size();

                TablistBuilder.create()
                        .header(ChatColor.GOLD + "" + ChatColor.BOLD + "✦ League UHC ✦\n")
                        .footer(ChatColor.YELLOW + "\n" + (size == 1 ? "Joueur" : "Joueurs") + " : " + ChatColor.WHITE + size)
                        .send(player);
            }
        };

        tabTask.runTaskTimer(this.instance, 0L, 20L);
        this.TAB_MAP.put(player.getUniqueId(), tabTask);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onLogout(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final BukkitRunnable tabTask = this.TAB_MAP.remove(player.getUniqueId());
        if (tabTask != null) tabTask.cancel();
    }
}
