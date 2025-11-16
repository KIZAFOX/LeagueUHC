package fr.kiza.leagueuhc.managers;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.api.champion.ability.AbilityTriggerManager;
import fr.kiza.leagueuhc.core.api.gadget.GadgetManager;
import fr.kiza.leagueuhc.core.api.gadget.RainbowWalk;
import fr.kiza.leagueuhc.managers.listeners.GlobalListeners;
import fr.kiza.leagueuhc.ui.scoreboard.Scoreboard;
import fr.kiza.leagueuhc.ui.tablist.Tablist;

public class Manager {

    protected static LeagueUHC instance;

    public static void onEnable(LeagueUHC inst) {
        instance = inst;

        new Scoreboard(instance);
        new Tablist(instance);

        new GlobalListeners(instance);

        new AbilityTriggerManager(instance);

        GadgetManager.onEnable(instance);
    }

    public static void onDisable() {
        GadgetManager.onDisable();
        RainbowWalk.onDisable();
    }
}
