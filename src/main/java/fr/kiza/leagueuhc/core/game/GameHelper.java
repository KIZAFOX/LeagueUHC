package fr.kiza.leagueuhc.core.game;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.api.champion.ChampionRegistry;
import fr.kiza.leagueuhc.core.api.gui.manager.GuiManager;
import fr.kiza.leagueuhc.managers.Manager;

public class GameHelper {
	public static void init(final LeagueUHC instance) {
        Manager.onEnable(instance);

        GuiManager.initialize("fr.kiza.leagueuhc.core.game.gui");
        ChampionRegistry.initialize("fr.kiza.leagueuhc.core.api.champion.champions");
	}
	
	public static void onDisable() {
        Manager.onDisable();
    }
}
