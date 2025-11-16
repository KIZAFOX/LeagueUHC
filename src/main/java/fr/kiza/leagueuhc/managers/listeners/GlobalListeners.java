package fr.kiza.leagueuhc.managers.listeners;

import fr.kiza.leagueuhc.LeagueUHC;

import fr.kiza.leagueuhc.core.api.gadget.RainbowWalk;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

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

        final Player player = event.getPlayer();

        player.setGameMode(GameMode.ADVENTURE);
        player.setFoodLevel(20);
        player.setWalkSpeed(.20F);
        player.setFlySpeed(.15F);
        player.setAllowFlight(false);
        player.setExp(0);
        player.setLevel(0);
        player.setMaxHealth(20.0D);
        player.setHealth(player.getMaxHealth());

        player.teleport(new Location(Bukkit.getWorlds().get(0), 0, 100, 0));
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onLogout(final PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onBlockBreak(final BlockBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onBlockPlace(final BlockPlaceEvent event) {
        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onFoodChange(final FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onWeatherChange(final WeatherChangeEvent event) {
        if (event.toWeatherState()) {
            event.setCancelled(true);
            event.getWorld().setStorm(false);
            event.getWorld().setWeatherDuration(Integer.MAX_VALUE);
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onThunderChange(final ThunderChangeEvent event) {
        if (event.toThunderState()) {
            event.setCancelled(true);
            event.getWorld().setStorm(false);
            event.getWorld().setWeatherDuration(Integer.MAX_VALUE);
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onMove(final PlayerMoveEvent event) {
        RainbowWalk.init(event);
    }
}
