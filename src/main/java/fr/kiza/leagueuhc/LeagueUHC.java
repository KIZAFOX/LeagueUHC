package fr.kiza.leagueuhc;

import fr.kiza.leagueuhc.managers.commands.CommandUHC;
import fr.kiza.leagueuhc.core.game.GameEngine;

import fr.kiza.leagueuhc.core.game.GameHelper;
import org.bukkit.plugin.java.JavaPlugin;

public final class LeagueUHC extends JavaPlugin {

    private static LeagueUHC instance;

    private GameEngine gameEngine;

    @Override
    public void onEnable() {
        instance = this;

        this.getLogger().info("==== LeagueUHC START ====");

        this.getServer().getWorlds().forEach(worlds -> {
            worlds.setStorm(false);
            worlds.setThundering(false);
            worlds.setWeatherDuration(Integer.MAX_VALUE);
        });

        GameHelper.init(this);

        this.gameEngine = new GameEngine(this);
        this.gameEngine.start();

        final CommandUHC command = new CommandUHC(this);
        this.getCommand("uhc").setExecutor(command);
        this.getCommand("uhc").setTabCompleter(command);

        this.getLogger().info("==== LeagueUHC READY ====");
    }

    @Override
    public void onDisable() {
        if (this.gameEngine != null) {
            this.gameEngine.stop();
            GameHelper.onDisable();
        };
    }

    public static LeagueUHC getInstance() {
        return instance;
    }

    public GameEngine getGameEngine() {
        return gameEngine;
    }

}
