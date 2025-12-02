package fr.kiza.leagueuhc.core.game.mechanics;

import fr.kiza.leagueuhc.core.game.event.GameTimerEvent;
import fr.kiza.leagueuhc.core.game.event.PvPEvent;
import fr.kiza.leagueuhc.core.game.event.bus.GameEventBus;
import fr.kiza.leagueuhc.core.game.timer.GameTimerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class GameMechanicsManager {

    public void init() {
        GameTimerManager.getInstance().start();
        GameEventBus.getInstance().subscribe(GameTimerEvent.class, this::onTimerTick);
    }

    private void onTimerTick(GameTimerEvent event) {
        int minutes = event.getElapsedMinutes();
        int seconds = event.getElapsedSeconds();

        // set pvp to true after 5min (20s for test)
        if (!PvPEvent.PvPHandler.getInstance().isEnabled() && seconds >= 20) {
            PvPEvent.PvPHandler.getInstance().setEnabled(true);
            Bukkit.getOnlinePlayers().forEach(players -> players.sendMessage(ChatColor.RED + "PvP Activé !"));

        }

        // final heal at 10min and 20min
        if (minutes == 10 || minutes == 20) {
            Bukkit.getOnlinePlayers().forEach(players -> players.setHealth(players.getMaxHealth()));
        }

        if (seconds % 10 == 0 && seconds > 0) {
            Bukkit.getOnlinePlayers().forEach(players -> players.sendMessage(ChatColor.GRAY + "⏱ Temps écoulé: " + ChatColor.GOLD + event.getFormattedTime()));
        }
    }
}
