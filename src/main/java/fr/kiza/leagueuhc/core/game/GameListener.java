package fr.kiza.leagueuhc.core.game;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.game.context.GameContext;
import fr.kiza.leagueuhc.core.game.event.PvPEvent;
import fr.kiza.leagueuhc.core.game.input.GameInput;
import fr.kiza.leagueuhc.core.game.input.InputType;

import fr.kiza.leagueuhc.core.game.state.GameState;
import fr.kiza.leagueuhc.core.game.state.StateManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;

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

    @EventHandler (priority = EventPriority.MONITOR)
    public void onDamage(final EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;

        if (!PvPEvent.PvPHandler.getInstance().isEnabled()) event.setCancelled(true);

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
    public void onAsyncChat(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();

        if (this.instance.getGameEngine().getCurrentState().equals(GameState.PLAYING.getName())) {
            event.setCancelled(true);
        } else {
            String prefix;

            if (player.isOp()) {
                prefix = ChatColor.RED + "★ " + ChatColor.DARK_RED;
            } else if (player.hasPermission("host.admin")) {
                prefix = ChatColor.LIGHT_PURPLE + "✦ " + ChatColor.LIGHT_PURPLE;
            } else {
                prefix = ChatColor.GRAY + "● " + ChatColor.GRAY;
            }

            event.setFormat(prefix + player.getName() + ChatColor.DARK_GRAY + " » " + ChatColor.WHITE + "%2$s");
        }
    }
}
