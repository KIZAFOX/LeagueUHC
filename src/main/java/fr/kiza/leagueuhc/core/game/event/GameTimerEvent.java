package fr.kiza.leagueuhc.core.game.event;

public class GameTimerEvent {
    private final long gameStartTime;
    private final long currentTime;
    private final int elapsedSeconds;
    private final String formattedTime;

    public GameTimerEvent(long gameStartTime, long currentTime) {
        this.gameStartTime = gameStartTime;
        this.currentTime = currentTime;
        this.elapsedSeconds = (int) ((currentTime - gameStartTime) / 1000);
        this.formattedTime = formatTime(elapsedSeconds);
    }

    public long getGameStartTime() {
        return gameStartTime;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    public int getElapsedMinutes() {
        return elapsedSeconds / 60;
    }

    public String getFormattedTime() {
        return formattedTime;
    }

    private String formatTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }
}
