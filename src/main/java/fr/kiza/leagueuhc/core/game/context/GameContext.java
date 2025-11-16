package fr.kiza.leagueuhc.core.game.context;

import fr.kiza.leagueuhc.core.game.scenario.Scenario;
import fr.kiza.leagueuhc.managers.commands.CommandUHC;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class GameContext {

    private int maxPlayers, countdown;
    private boolean isPaused;

    private final Map<UUID, Integer> playerScores;
    private final Set<UUID> alivePlayers;
    private final Set<UUID> allPlayers;
    private final Map<String, Object> gameData;

    private final Set<Scenario> activeScenarios;
    private final Map<Scenario, Integer> scenarioPercentages;
    private final Map<PotionEffectType, Integer> effectPercentages;

    public GameContext() {
        this.maxPlayers = 16;
        this.countdown = 10;
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

        for (Scenario scenario : Scenario.values()) {
            if (scenario.hasPercentage()) {
                scenarioPercentages.put(scenario, 100);
            }
        }
    }

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
        if (percentage == 0) return -1; // Désactivé
        return Math.max(0, (percentage / 20) - 1);
    }

    public boolean isEffectActive(PotionEffectType type) {
        return getEffectPercentage(type) > 0;
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

    public boolean isScenarioActive(Scenario scenario) {
        return activeScenarios.contains(scenario);
    }

    public void addScenario(Scenario scenario) {
        activeScenarios.add(scenario);
    }

    public void removeScenario(Scenario scenario) {
        activeScenarios.remove(scenario);
    }

    public Set<Scenario> getActiveScenarios() {
        return new HashSet<>(activeScenarios);
    }

    public int getScenarioPercentage(Scenario scenario) {
        return scenarioPercentages.getOrDefault(scenario, 100);
    }

    public void setScenarioPercentage(Scenario scenario, int percentage) {
        scenarioPercentages.put(scenario, percentage);
    }

    public double getScenarioMultiplier(Scenario scenario) {
        if (!isScenarioActive(scenario)) {
            return 1.0;
        }
        return getScenarioPercentage(scenario) / 100.0;
    }

    public void clearScenarios() {
        activeScenarios.clear();
        scenarioPercentages.clear();
    }

    public void loadScenarios(Map<String, Object> config) {
        clearScenarios();

        if (config.containsKey("scenarios")) {
            Map<String, Object> scenariosData = (Map<String, Object>) config.get("scenarios");

            for (String scenarioName : scenariosData.keySet()) {
                Scenario scenario = Scenario.getByName(scenarioName);
                if (scenario != null) {
                    addScenario(scenario);

                    if (scenario.hasPercentage()) {
                        Object percentageObj = scenariosData.get(scenarioName);
                        if (percentageObj instanceof Integer) {
                            setScenarioPercentage(scenario, (Integer) percentageObj);
                        }
                    }
                }
            }
        }
    }

    /**
     * Sauvegarde les scénarios dans une configuration
     */
    public Map<String, Object> saveScenarios() {
        Map<String, Object> scenariosData = new HashMap<>();

        for (Scenario scenario : activeScenarios) {
            if (scenario.hasPercentage()) {
                scenariosData.put(scenario.getName(), getScenarioPercentage(scenario));
            } else {
                scenariosData.put(scenario.getName(), true);
            }
        }

        Map<String, Object> config = new HashMap<>();
        config.put("scenarios", scenariosData);
        return config;
    }

    public void reset() {
        this.countdown = 10;
        this.maxPlayers = 16;
        this.isPaused = false;

        this.playerScores.clear();
        this.alivePlayers.clear();
        this.allPlayers.clear();
        this.gameData.clear();

        this.clearScenarios();
        this.resetEffects();

        if (CommandUHC.pregenManager != null) CommandUHC.pregenManager.resetPregen();
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