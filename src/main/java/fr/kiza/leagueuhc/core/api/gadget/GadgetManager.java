package fr.kiza.leagueuhc.core.api.gadget;

import fr.kiza.leagueuhc.LeagueUHC;

public class GadgetManager {

    protected static LeagueUHC instance;

    public CrownManager crownManager;

    public void onEnable(LeagueUHC inst) {
        instance = inst;
        crownManager = new CrownManager(instance);
    }

    public void onDisable() {
        if (crownManager != null) crownManager.disableAll();
    }
}
