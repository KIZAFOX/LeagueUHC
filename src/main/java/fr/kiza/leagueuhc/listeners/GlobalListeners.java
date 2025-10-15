package fr.kiza.leagueuhc.listeners;

import fr.kiza.leagueuhc.LeagueUHC;

import fr.kiza.leagueuhc.utils.RainbowWalk;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GlobalListeners implements Listener {

    protected final LeagueUHC instance;

    public GlobalListeners(LeagueUHC instance) {
        this.instance = instance;
        this.instance.getServer().getPluginManager().registerEvents(this, instance);

        new SettingsListener(this.instance);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onLogin(final PlayerJoinEvent event) {
        event.setJoinMessage(null);
        event.getPlayer().teleport(new Location(Bukkit.getWorlds().get(0), 0, 100, 0));
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onLogout(final PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onMove(final PlayerMoveEvent event) {
        RainbowWalk.init(event);
    }
}
