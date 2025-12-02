package fr.kiza.leagueuhc.core.game.state.states;

import fr.kiza.leagueuhc.core.game.context.GameContext;
import fr.kiza.leagueuhc.core.game.event.bus.GameEventBus;
import fr.kiza.leagueuhc.core.game.event.MovementFreezeEvent;
import fr.kiza.leagueuhc.core.game.input.GameInput;
import fr.kiza.leagueuhc.core.game.state.BaseGameState;
import fr.kiza.leagueuhc.core.game.state.GameState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class StartingState extends BaseGameState {

    private long elapsedTime = 0;

    public StartingState() {
        super(GameState.STARTING.getName());
    }

    @Override
    public void onEnter(GameContext context) {
        this.elapsedTime = 0;

        this.broadcast(ChatColor.GOLD + "" + ChatColor.BOLD + "=== DÉMARRAGE DE LA PARTIE ===");
        this.broadcast(ChatColor.YELLOW + "La partie commence dans " + ChatColor.RED + context.getCountdown() + ChatColor.YELLOW + " secondes !");

        Bukkit.getOnlinePlayers().forEach(players -> {
            players.getInventory().clear();
            players.playSound(players.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
        });
    }

    @Override
    public void onExit(GameContext context) { }

    @Override
    public void update(GameContext context, long deltaTime) {
        this.elapsedTime += deltaTime;

        if (this.elapsedTime >= 1000) {
            this.elapsedTime = 0;
            context.decrementCountdown();

            final int countdown = context.getCountdown();

            if (countdown > 0) {
                if (countdown <= 5) {
                    GameEventBus.getInstance().publish(new MovementFreezeEvent(true));

                    Bukkit.getOnlinePlayers().forEach(players -> {
                        players.playSound(players.getLocation(), Sound.NOTE_PLING, 1.0f, 1.5f);
                        players.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + countdown + "...");
                    });
                } else if (countdown == 10) {
                    this.broadcast(ChatColor.YELLOW + "Démarrage dans" + ChatColor.RED + " 10 " + ChatColor.YELLOW + "secondes !");
                }
            }
        }
    }

    @Override
    public void handleInput(GameContext context, GameInput input) {
        switch (input.getType()) {
            case PLAYER_JOIN:
                final Player player = input.getPlayer();

                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "La partie est en cours de lancement...");
                break;
            case PLAYER_LEAVE:
                context.removePlayer(input.getPlayer().getUniqueId());
                break;
            default:
                break;
        }
    }
}
