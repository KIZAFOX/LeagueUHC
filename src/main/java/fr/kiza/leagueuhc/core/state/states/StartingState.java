package fr.kiza.leagueuhc.core.state.states;

import fr.kiza.leagueuhc.core.context.GameContext;
import fr.kiza.leagueuhc.core.state.BaseGameState;
import fr.kiza.leagueuhc.core.state.GameState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

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

        Bukkit.getOnlinePlayers().forEach(players -> players.playSound(players.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f));
    }

    @Override
    public void onExit(GameContext context) {
        this.broadcast(ChatColor.RED + "" + ChatColor.BOLD + "C'EST PARTI !");
    }

    @Override
    public void update(GameContext context, long deltaTime) {
        this.elapsedTime += deltaTime;

        if (this.elapsedTime >= 1000) {
            this.elapsedTime = 0;
            context.decrementCountdown();

            final int countdown = context.getCountdown();

            if (countdown > 0) {
                if (countdown <= 5) {
                    this.broadcast(ChatColor.RED + "" + ChatColor.BOLD + countdown + "...");

                    Bukkit.getOnlinePlayers().forEach(players -> players.playSound(players.getLocation(), Sound.NOTE_PLING, 1.0f, 1.5f));
                } else if (countdown == 10) {
                    this.broadcast(ChatColor.YELLOW + "Démarrage dans" + ChatColor.RED + " 10 " + ChatColor.YELLOW + "secondes !");
                }
            }
        }
    }
}