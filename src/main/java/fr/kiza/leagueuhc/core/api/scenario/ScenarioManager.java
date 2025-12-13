package fr.kiza.leagueuhc.core.api.scenario;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.game.context.GameContext;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScenarioManager {

    private final LeagueUHC instance;
    private final Map<String, Scenario> scenarios = new HashMap<>();

    public ScenarioManager(LeagueUHC instance) {
        this.instance = instance;
    }

    private GameContext getContext() {
        return this.instance.getGameEngine().getContext();
    }

    public void register(Scenario scenario) {
        String id = scenario.getId().toLowerCase();
        this.scenarios.put(id, scenario);
        Bukkit.getLogger().info("[LeagueUHC] Registered scenario: " + scenario.getName() + " [" + id + "]");
    }

    public Scenario get(String id) {
        return this.scenarios.get(id.toLowerCase());
    }

    public Scenario get(ScenarioType type) {
        return this.get(type.getId());
    }

    public boolean isActive(String id) {
        return getContext().isScenarioActive(id);
    }

    public boolean isActive(ScenarioType type) {
        return this.isActive(type.getId());
    }

    public void enable(String id) {
        Scenario scenario = this.get(id);
        if (scenario == null) {
            Bukkit.getLogger().warning("[LeagueUHC] Cannot enable unknown scenario: " + id);
            return;
        }

        if (this.isActive(id)) {
            return;
        }

        getContext().addScenario(id);
        scenario.onEnable();
        Bukkit.getPluginManager().registerEvents(scenario, this.instance);
        Bukkit.getLogger().info("[LeagueUHC] Enabled scenario: " + scenario.getName());
    }

    public void enable(ScenarioType type) {
        this.enable(type.getId());
    }

    public void disable(String id) {
        Scenario scenario = this.get(id);
        if (scenario == null) {
            return;
        }

        if (!this.isActive(id)) {
            return;
        }

        getContext().removeScenario(id);
        scenario.onDisable();
        HandlerList.unregisterAll(scenario);
        Bukkit.getLogger().info("[LeagueUHC] Disabled scenario: " + scenario.getName());
    }

    public void disable(ScenarioType type) {
        this.disable(type.getId());
    }

    public void toggle(String id) {
        if (this.isActive(id)) {
            this.disable(id);
        } else {
            this.enable(id);
        }
    }

    public void toggle(ScenarioType type) {
        this.toggle(type.getId());
    }

    public void disableAll() {
        for (String id : new HashSet<>(getContext().getActiveScenarioIds())) {
            this.disable(id);
        }
    }

    public Collection<Scenario> getAll() {
        return this.scenarios.values();
    }

    public Set<String> getActiveIds() {
        return getContext().getActiveScenarioIds();
    }

    public int getPercentage(String id) {
        return getContext().getScenarioPercentage(id);
    }

    public int getPercentage(ScenarioType type) {
        return getPercentage(type.getId());
    }

    public void setPercentage(String id, int value) {
        getContext().setScenarioPercentage(id, value);
    }

    public void setPercentage(ScenarioType type, int value) {
        setPercentage(type.getId(), value);
    }
}