package fr.kiza.leagueuhc.managers;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.api.gadget.GadgetManager;
import fr.kiza.leagueuhc.core.api.gadget.RainbowWalk;
import fr.kiza.leagueuhc.core.api.scenario.ScenarioManager;
import fr.kiza.leagueuhc.core.game.cycle.DayCycleManager;
import fr.kiza.leagueuhc.managers.listeners.GlobalListeners;
import fr.kiza.leagueuhc.ui.scoreboard.Scoreboard;
import fr.kiza.leagueuhc.ui.tablist.Tablist;

public class Manager {

    protected static LeagueUHC instance;

    protected GadgetManager gadgetManager;
    protected ScenarioManager scenarioManager;
    protected DayCycleManager dayCycleManager;

    public void onEnable(LeagueUHC inst) {
        instance = inst;

        new Scoreboard(instance);
        new Tablist(instance);

        new GlobalListeners(instance);

        this.gadgetManager = new GadgetManager();
        this.scenarioManager = new ScenarioManager(instance);
        this.dayCycleManager = new DayCycleManager(instance);
    }

    public void onDisable() {
        if (this.gadgetManager != null) this.gadgetManager.onDisable();
        RainbowWalk.onDisable();
    }

    public GadgetManager getGadgetManager() {
        return gadgetManager;
    }

    public ScenarioManager getScenarioManager() {
        return scenarioManager;
    }

    public DayCycleManager getDayCycleManager() {
        return dayCycleManager;
    }
}
