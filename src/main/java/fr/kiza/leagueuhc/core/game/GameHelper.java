package fr.kiza.leagueuhc.core.game;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.api.champion.ChampionManager;
import fr.kiza.leagueuhc.core.api.champion.ChampionRegistry;
import fr.kiza.leagueuhc.core.api.gui.manager.GuiManager;
import fr.kiza.leagueuhc.core.api.scenario.ScenarioLoader;
import fr.kiza.leagueuhc.managers.Manager;

public class GameHelper {

    protected LeagueUHC instance;

    private Manager manager;

    public void init(final LeagueUHC instance) {
        this.instance = instance;

        this.manager = new Manager();
        this.manager.onEnable(instance);

        GuiManager.initialize("fr.kiza.leagueuhc.core.game.gui");
        ChampionRegistry.initialize("fr.kiza.leagueuhc.core.game.champions");
        ScenarioLoader.initialize("fr.kiza.leagueuhc.core.game.scenarios", this.manager.getScenarioManager());

        new ChampionManager(instance);
	}
	
	public void onDisable() {
        if (manager != null) manager.onDisable();
    }

    public Manager getManager() {
        return manager;
    }
}
