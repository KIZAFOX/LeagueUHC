package fr.kiza.leagueuhc.core.context;

import java.util.*;

public class GameContext {

    private int maxPlayers, countdown;
    private boolean isPaused;

    private final Map<UUID, Integer> playerScores;
    private final Set<UUID> alivePlayers;
    private final Set<UUID> allPlayers;
    private final Map<String, Object> gameData;

    public GameContext() {
        this.maxPlayers = 16;
        this.countdown = 10;
        this.isPaused = false;

        this.playerScores = new HashMap<>();
        this.alivePlayers = new HashSet<>();
        this.allPlayers = new HashSet<>();
        this.gameData = new HashMap<>();
    }

    public void addPlayer(final UUID playerUuid) {
        this.allPlayers.add(playerUuid);
        this.alivePlayers.add(playerUuid);
        this.playerScores.put(playerUuid, 0);
    }

    public void removePlayer(final UUID playerUuid) {
        this.allPlayers.remove(playerUuid);
        this.alivePlayers.remove(playerUuid);
        this.playerScores.remove(playerUuid);
    }

    public boolean hasPlayer(final UUID playerUuid) {
        return this.allPlayers.contains(playerUuid);
    }

    public Set<UUID> getPlayers() {
        return new HashSet<>(this.allPlayers);
    }

    public Set<UUID> getAlivePlayers() {
        return new HashSet<>(this.alivePlayers);
    }

    public int getPlayerCount() {
        return this.allPlayers.size();
    }

    public void setPlayerAlive(final UUID playerUuid, boolean alive) {
        if (alive) {
            this.alivePlayers.add(playerUuid);
        } else {
            this.alivePlayers.remove(playerUuid);
        }
    }

    public int getScore(final UUID playerUuid) {
        return this.playerScores.getOrDefault(playerUuid, 0);
    }

    public void setScore(final UUID playerUuid, final int score) {
        playerScores.put(playerUuid, score);
    }

    public void addScore(final UUID playerUuid, final int points) {
        final int currentScore = this.getScore(playerUuid);
        setScore(playerUuid, currentScore + points);
    }

    public Map<UUID, Integer> getAllScores() {
        return new HashMap<>(this.playerScores);
    }

    public void setData(final String key, final Object value) {
        this.gameData.put(key, value);
    }

    public <T> T getData(final String key) {
        return (T) this.gameData.get(key);
    }

    public <T> T getData(final String key, final T defaultValue) {
        final T value = this.getData(key);
        return value != null ? value : defaultValue;
    }

    public void reset() {
        this.countdown = 10;
        this.maxPlayers = 16;
        this.isPaused = false;

        this.playerScores.clear();
        this.alivePlayers.clear();
        this.allPlayers.clear();
        this.gameData.clear();
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getCountdown() {
        return countdown;
    }

    public void setCountdown(int countdown) {
        this.countdown = countdown;
    }

    public void decrementCountdown() {
        this.countdown--;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }
}
