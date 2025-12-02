package fr.kiza.leagueuhc.core.game.timer;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.game.event.GameTimerEvent;
import fr.kiza.leagueuhc.core.game.event.bus.GameEventBus;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class GameTimerManager {

    private static GameTimerManager instance;

    private long gameStartTime = 0;
    private boolean isRunning = false;
    private BukkitTask timerTask;

    private GameTimerManager() {}

    public static GameTimerManager getInstance() {
        if (instance == null) {
            instance = new GameTimerManager();
        }
        return instance;
    }

    public void start() {
        if (isRunning) return;

        this.gameStartTime = System.currentTimeMillis();
        this.isRunning = true;

        this.timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isRunning) {
                    this.cancel();
                    return;
                }

                long currentTime = System.currentTimeMillis();
                GameTimerEvent event = new GameTimerEvent(gameStartTime, currentTime);
                GameEventBus.getInstance().publish(event);
            }
        }.runTaskTimer(LeagueUHC.getInstance(), 0L, 20L);
    }

    /**
     * Stop le timer
     */
    public void stop() {
        this.isRunning = false;
        if (this.timerTask != null) {
            this.timerTask.cancel();
            this.timerTask = null;
        }
    }

    public void reset() {
        stop();
        this.gameStartTime = 0;
    }

    public int getElapsedSeconds() {
        if (!isRunning || gameStartTime == 0) return 0;
        return (int) ((System.currentTimeMillis() - gameStartTime) / 1000);
    }

    public String getFormattedTime() {
        int totalSeconds = getElapsedSeconds();
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public long getGameStartTime() {
        return gameStartTime;
    }
}