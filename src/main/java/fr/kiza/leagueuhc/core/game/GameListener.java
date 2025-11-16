package fr.kiza.leagueuhc.core.game;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.game.input.GameInput;
import fr.kiza.leagueuhc.core.game.input.InputType;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GameListener implements Listener {

    protected final LeagueUHC instance;

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

//    @EventHandler (priority = EventPriority.MONITOR)
//    public void onDamage(final EntityDamageEvent event) {
//        this.instance.getGameEngine().handleInput(new GameInput(
//                InputType.PLAYER_DAMAGE,
//                (Player) event.getEntity(),
//                event
//        ));
//    }
//
    @EventHandler (priority = EventPriority.MONITOR)
    public void onDeath(final PlayerDeathEvent event) {
        this.instance.getGameEngine().handleInput(new GameInput(
                InputType.PLAYER_DEATH,
                event.getEntity().getPlayer(),
                event
        ));
    }
}
