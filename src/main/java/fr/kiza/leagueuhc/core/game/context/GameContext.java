package fr.kiza.leagueuhc.core.game.context;

import fr.kiza.leagueuhc.core.api.champion.Champion;
import fr.kiza.leagueuhc.core.api.champion.ChampionRegistry;
import fr.kiza.leagueuhc.core.api.scenario.Scenario;
import fr.kiza.leagueuhc.core.api.scenario.ScenarioType;
import fr.kiza.leagueuhc.core.game.timer.GameTimerManager;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class GameContext {

    public static final int PLAYER_MAX = 32, COUNTDOWN = 10;

    private int countdown = COUNTDOWN;
    private boolean isPaused;

    private final Map<UUID, Integer> playerScores;
    private final Set<UUID> alivePlayers;
    private final Set<UUID> allPlayers;
    private final Map<String, Object> gameData;

    private final Set<String> activeScenarios;
    private final Map<String, Integer> scenarioPercentages;

    private final Map<PotionEffectType, Integer> effectPercentages;

    private final Set<Champion> enabledChampions = new HashSet<>();

    public GameContext() {
        this.isPaused = false;

        this.playerScores = new HashMap<>();
        this.alivePlayers = new HashSet<>();
        this.allPlayers = new HashSet<>();
        this.gameData = new HashMap<>();

        this.activeScenarios = new HashSet<>();
        this.scenarioPercentages = new HashMap<>();
        this.effectPercentages = new HashMap<>();

        this.effectPercentages.put(PotionEffectType.INCREASE_DAMAGE, 20);
        this.effectPercentages.put(PotionEffectType.DAMAGE_RESISTANCE, 20);
        this.effectPercentages.put(PotionEffectType.SPEED, 20);
    }

    // ========================
    // Effects
    // ========================

    public void resetEffects() {
        this.effectPercentages.put(PotionEffectType.INCREASE_DAMAGE, 20);
        this.effectPercentages.put(PotionEffectType.DAMAGE_RESISTANCE, 20);
        this.effectPercentages.put(PotionEffectType.SPEED, 20);
    }

    public int getEffectPercentage(PotionEffectType type) {
        return effectPercentages.getOrDefault(type, 20);
    }

    public void setEffectPercentage(PotionEffectType type, int percentage) {
        effectPercentages.put(type, percentage);
    }

    public int getEffectLevel(PotionEffectType type) {
        int percentage = getEffectPercentage(type);
        if (percentage == 0) return -1;
        return Math.max(0, (percentage / 20) - 1);
    }

    public boolean isEffectActive(PotionEffectType type) {
        return getEffectPercentage(type) > 0;
    }

    // ========================
    // Players
    // ========================

    public void addPlayer(UUID playerUuid) {
        this.allPlayers.add(playerUuid);
        this.alivePlayers.add(playerUuid);
        this.playerScores.put(playerUuid, 0);
    }

    public void removePlayer(UUID playerUuid) {
        this.allPlayers.remove(playerUuid);
        this.alivePlayers.remove(playerUuid);
        this.playerScores.remove(playerUuid);
    }

    public boolean hasPlayer(UUID playerUuid) {
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

    public void setPlayerAlive(UUID playerUuid, boolean alive) {
        if (alive) {
            this.alivePlayers.add(playerUuid);
        } else {
            this.alivePlayers.remove(playerUuid);
        }
    }

    // ========================
    // Scores
    // ========================

    public int getScore(UUID playerUuid) {
        return this.playerScores.getOrDefault(playerUuid, 0);
    }

    public void setScore(UUID playerUuid, int score) {
        playerScores.put(playerUuid, score);
    }

    public void addScore(UUID playerUuid, int points) {
        int currentScore = this.getScore(playerUuid);
        setScore(playerUuid, currentScore + points);
    }

    public Map<UUID, Integer> getAllScores() {
        return new HashMap<>(this.playerScores);
    }

    // ========================
    // Game Data
    // ========================

    public void setData(String key, Object value) {
        this.gameData.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getData(String key) {
        return (T) this.gameData.get(key);
    }

    public <T> T getData(String key, T defaultValue) {
        T value = this.getData(key);
        return value != null ? value : defaultValue;
    }

    // ========================
    // Scenarios
    // ========================

    public boolean isScenarioActive(String id) {
        return activeScenarios.contains(id.toLowerCase());
    }

    public boolean isScenarioActive(ScenarioType type) {
        return isScenarioActive(type.getId());
    }

    public void addScenario(String id) {
        activeScenarios.add(id.toLowerCase());
    }

    public void addScenario(ScenarioType type) {
        addScenario(type.getId());
    }

    public void removeScenario(String id) {
        activeScenarios.remove(id.toLowerCase());
    }

    public void removeScenario(ScenarioType type) {
        removeScenario(type.getId());
    }

    public Set<String> getActiveScenarioIds() {
        return new HashSet<>(activeScenarios);
    }

    public int getScenarioPercentage(String id) {
        return scenarioPercentages.getOrDefault(id.toLowerCase(), 100);
    }

    public int getScenarioPercentage(ScenarioType type) {
        return getScenarioPercentage(type.getId());
    }

    public void setScenarioPercentage(String id, int value) {
        scenarioPercentages.put(id.toLowerCase(), Math.max(0, Math.min(100, value)));
    }

    public void setScenarioPercentage(ScenarioType type, int value) {
        setScenarioPercentage(type.getId(), value);
    }

    public void clearScenarios() {
        activeScenarios.clear();
        scenarioPercentages.clear();
    }

    // ========================
    // Champions
    // ========================

    public boolean isChampionEnabled(Champion champion) {
        return enabledChampions.contains(champion);
    }

    public void enableChampion(Champion champion) {
        enabledChampions.add(champion);
    }

    public void disableChampion(Champion champion) {
        enabledChampions.remove(champion);
    }

    public Set<Champion> getEnabledChampions() {
        return Collections.unmodifiableSet(enabledChampions);
    }

    public void enableAllChampions() {
        enabledChampions.addAll(ChampionRegistry.getChampions());
    }

    public void disableAllChampions() {
        enabledChampions.clear();
    }

    // ========================
    // Countdown / Pause
    // ========================

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

    // ========================
    // Reset
    // ========================

    public void reset() {
        this.countdown = COUNTDOWN;
        this.isPaused = false;

        this.playerScores.clear();
        this.alivePlayers.clear();
        this.allPlayers.clear();
        this.gameData.clear();

        this.clearScenarios();
        this.resetEffects();
        this.disableAllChampions();

        GameTimerManager.getInstance().reset();
    }
}