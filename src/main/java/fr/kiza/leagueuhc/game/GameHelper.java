package fr.kiza.leagueuhc.game;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.champion.ChampionRegistry;
import fr.kiza.leagueuhc.champion.ability.AbilityTriggerManager;
import fr.kiza.leagueuhc.gui.manager.GuiManager;
import fr.kiza.leagueuhc.listeners.GlobalListeners;
import fr.kiza.leagueuhc.ui.scoreboard.Scoreboard;
import fr.kiza.leagueuhc.ui.tablist.Tablist;
import fr.kiza.leagueuhc.utils.RainbowWalk;

public class GameHelper {
	public static void init(final LeagueUHC instance) {
        new Scoreboard(instance);
        new Tablist(instance);

        new GlobalListeners(instance);

        new AbilityTriggerManager(instance);
        
        GuiManager.initialize("fr.kiza.leagueuhc.gui.example");
        ChampionRegistry.initialize("fr.kiza.leagueuhc.champion.champions");
	}
	
	public static void terminate() {
		RainbowWalk.onDisable();
	}
}
