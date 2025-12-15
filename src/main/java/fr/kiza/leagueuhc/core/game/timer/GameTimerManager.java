package fr.kiza.leagueuhc.core.game.timer;

import fr.kiza.leagueuhc.core.game.event.GameTimerEvent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class GameTimerManager {

    private static final GameTimerManager INSTANCE = new GameTimerManager();

    private long gameStartTime;
    private BukkitTask timerTask;

    private GameTimerManager() { }

    public void start(Plugin plugin) {
        this.gameStartTime = System.currentTimeMillis();

        this.timerTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            Bukkit.getPluginManager().callEvent(new GameTimerEvent(gameStartTime, currentTime));
        }, 0L, 20L);
    }

    public void stop() {
        if (this.timerTask != null) {
            this.timerTask.cancel();
            this.timerTask = null;
        }
    }

    public String getFormattedTime() {
        if (gameStartTime == 0) return "";
        int totalSeconds = (int) ((System.currentTimeMillis() - gameStartTime) / 1000);
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    public static GameTimerManager getInstance() {
        return INSTANCE;
    }
}