package fr.kiza.leagueuhc.core.game;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.game.context.GameContext;
import fr.kiza.leagueuhc.core.game.event.GameTimerEvent;
import fr.kiza.leagueuhc.core.game.event.PlayerFreezeEvent;
import fr.kiza.leagueuhc.core.game.event.PvPEvent;
import fr.kiza.leagueuhc.core.game.input.GameInput;
import fr.kiza.leagueuhc.core.game.input.InputType;

import fr.kiza.leagueuhc.core.game.state.GameState;
import fr.kiza.leagueuhc.core.game.state.StateManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class GameListener implements Listener {

    protected final LeagueUHC instance;

    private boolean
            pvpEnabled = false,
            firstHealDone = false,
            secondHealDone = false;

    private final Map<UUID, Location> frozenPlayers = new HashMap<>();;

    public GameListener(LeagueUHC instance) {
        this.instance = instance;
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onLogin(final PlayerJoinEvent event) {
        this.instance.getGameEngine().handleInput(new GameInput(
                InputType.PLAYER_JOIN,
                event.getPlayer(),
                event
        ));
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onLogout(final PlayerQuitEvent event) {
        this.instance.getGameEngine().handleInput(new GameInput(
                InputType.PLAYER_LEAVE,
                event.getPlayer(),
                event
        ));
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onInteract(final PlayerInteractEvent event) {
        this.instance.getGameEngine().handleInput(new GameInput(
                InputType.PLAYER_INTERACT,
                event.getPlayer(),
                event
        ));
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onDamage(final EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;

        this.instance.getGameEngine().handleInput(new GameInput(
                InputType.PLAYER_DAMAGE,
                (Player) event.getDamager(),
                event
        ));
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onDeath(final PlayerDeathEvent event) {
        final GameInput input = new GameInput(InputType.PLAYER_DEATH, event.getEntity().getPlayer(), event);

        input.setDeathDrops(new ArrayList<>(event.getDrops()));
        event.getDrops().clear();

        event.setDeathMessage(null);

        this.instance.getGameEngine().handleInput(input);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerFreeze(final PlayerFreezeEvent event) {
        final Player player = event.getPlayer();

        if (event.isFrozen()) {
            Location freezeLocation = event.getFreezeLocation() != null
                    ? event.getFreezeLocation()
                    : player.getLocation().clone();

            frozenPlayers.put(player.getUniqueId(), freezeLocation);

            player.setWalkSpeed(0);
            player.setFlySpeed(0);
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1, false, false));
        } else {
            frozenPlayers.remove(player.getUniqueId());

            player.setWalkSpeed(0.2f);
            player.setFlySpeed(0.1f);
            player.removePotionEffect(PotionEffectType.JUMP);
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if (frozenPlayers.containsKey(player.getUniqueId())) {
            frozenPlayers.put(player.getUniqueId(), event.getTo().clone());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final Location freezeLocation = frozenPlayers.get(player.getUniqueId());

        if (freezeLocation != null) {
            final Location from = event.getFrom();
            final Location to = event.getTo();

            if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
                final Location newLocation = freezeLocation.clone();
                newLocation.setYaw(to.getYaw());
                newLocation.setPitch(to.getPitch());
                event.setTo(newLocation);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPvPToggle(final PvPEvent event) {
        this.pvpEnabled = event.isEnabled();

        if (event.isEnabled()) {
            Bukkit.getOnlinePlayers().forEach(players -> {
                players.sendMessage(ChatColor.RED + "" + ChatColor.BOLD +"⚔ LE PVP EST MAINTENANT ACTIVÉ!");
                players.playSound(players.getLocation(), Sound.WITHER_SPAWN, 1.0f, 1.0f);
            });
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            if (!this.pvpEnabled) {
                event.setCancelled(true);

                Player damager = (Player) event.getDamager();
                damager.sendMessage("§cLe PvP n'est pas encore activé!");
            }
        }
    }

    @EventHandler
    public void onTimerTick(final GameTimerEvent event){
        final int seconds = event.getElapsedSeconds();
        final int minutes = event.getElapsedMinutes();

        if (!this.pvpEnabled && seconds >= 20) {
            this.pvpEnabled = true;
            Bukkit.getPluginManager().callEvent(new PvPEvent(true));
        }

        if (!this.firstHealDone && minutes >= 10) {
            this.firstHealDone = true;
            Bukkit.getOnlinePlayers().forEach(players -> {
                players.setHealth(players.getMaxHealth());
                players.setFoodLevel(20);
                players.playSound(players.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
                players.sendMessage(ChatColor.GREEN + "✚ Heal de l'épisode 1 !");
            });
        }

        if (!this.secondHealDone && minutes >= 20) {
            this.secondHealDone = true;
            Bukkit.getOnlinePlayers().forEach(players -> {
                players.setHealth(players.getMaxHealth());
                players.setFoodLevel(20);
                players.playSound(players.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
                players.sendMessage(ChatColor.GREEN + "✚ Heal de l'épisode 2 !");
            });
        }

        if (seconds % 10 == 0 && seconds > 0) {
            Bukkit.getOnlinePlayers().forEach(player ->
                    player.sendMessage(ChatColor.GRAY + "⏱ Temps écoulé: " + ChatColor.GOLD + event.getFormattedTime())
            );
        }
    }
}
