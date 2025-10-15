package fr.kiza.leagueuhc.core.state.states;

import fr.kiza.leagueuhc.core.context.GameContext;
import fr.kiza.leagueuhc.core.input.GameInput;
import fr.kiza.leagueuhc.core.state.BaseGameState;
import fr.kiza.leagueuhc.core.state.GameState;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayingState extends BaseGameState {

    public PlayingState() {
        super(GameState.PLAYING.getName());
    }

    @Override
    public void onEnter(GameContext context) {
        context.getPlayers().forEach(players -> context.setPlayerAlive(players, true));

        this.broadcast(ChatColor.GOLD + "" + ChatColor.BOLD + "=============================");
        this.broadcast(ChatColor.YELLOW + "" + ChatColor.BOLD + "   LA PARTIE COMMENCE !");
        this.broadcast(ChatColor.GOLD + "" + ChatColor.BOLD + "=============================");
        this.broadcast("");

        Bukkit.getOnlinePlayers().forEach(players -> players.playSound(players.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0f, 1.0f));
    }

    @Override
    public void onExit(GameContext context) { }

    @Override
    public void update(GameContext context, long deltaTime) {
        if (context.isPaused()) return;
    }

    @Override
    public void handleInput(GameContext context, GameInput input) {
        switch (input.getType()) {
            case PLAYER_JOIN:
                final Player player = input.getPlayer();

                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "La partie a déjà commencé...");
                break;
            case PLAYER_DEATH:
                final UUID deadPlayer = input.getPlayer().getUniqueId();

                context.setPlayerAlive(deadPlayer, false);

                this.broadcast(ChatColor.RED + "☠ " + input.getPlayer().getName() + ChatColor.GRAY + " est mort !");

                Bukkit.getOnlinePlayers().forEach(players -> players.playSound(players.getLocation(), Sound.WITHER_SPAWN, 0.5f, 1.0f));
                break;
            case PLAYER_LEAVE:
                final UUID leavingPlayer = input.getPlayer().getUniqueId();

                if (context.getAlivePlayers().contains(leavingPlayer)) {
                    context.setPlayerAlive(leavingPlayer, false);
                    this.broadcast(ChatColor.RED + input.getPlayer().getName() + " a quitté la partie (éliminé) !");
                }

                context.removePlayer(leavingPlayer);
                break;

            default:
                break;
        }
    }
}