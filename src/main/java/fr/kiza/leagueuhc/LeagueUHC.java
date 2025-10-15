package fr.kiza.leagueuhc;

import fr.kiza.leagueuhc.commands.CommandUHC;
import fr.kiza.leagueuhc.core.GameEngine;

import fr.kiza.leagueuhc.game.GameHelper;
import fr.kiza.leagueuhc.gadget.CrownManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class LeagueUHC extends JavaPlugin {

    private static LeagueUHC instance;

    private GameEngine gameEngine;

    private CrownManager crownManager;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("==== LeagueUHC START ====");

        GameHelper.init(this);

        this.gameEngine = new GameEngine(this);
        this.gameEngine.start();

        this.crownManager = new CrownManager(this);

        final CommandUHC command = new CommandUHC(this);
        this.getCommand("uhc").setExecutor(command);
        this.getCommand("uhc").setTabCompleter(command);

        getLogger().info("==== LeagueUHC READY ====");
    }

    @Override
    public void onDisable() {
        if (this.gameEngine != null) this.gameEngine.stop();
        if (this.crownManager != null) this.crownManager.disableAll();
        GameHelper.terminate();
    }

    public static LeagueUHC getInstance() {
        return instance;
    }

    public GameEngine getGameEngine() {
        return gameEngine;
    }

    public CrownManager getCrownManager() {
        return crownManager;
    }
}
