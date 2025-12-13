package fr.kiza.leagueuhc;

import fr.kiza.leagueuhc.managers.commands.CommandUHC;
import fr.kiza.leagueuhc.core.game.GameEngine;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class LeagueUHC extends JavaPlugin {

    private static LeagueUHC instance;

    private GameEngine gameEngine;

    @Override
    public void onEnable() {
        instance = this;

        this.getLogger().info("==== LeagueUHC START ====");

        Bukkit.setWhitelist(true);

        this.gameEngine = new GameEngine(this);
        this.gameEngine.start();

        final CommandUHC command = new CommandUHC(this);
        this.getCommand("uhc").setExecutor(command);
        this.getCommand("uhc").setTabCompleter(command);

        CommandUHC.pregenManager.unloadAndDeleteWorld();

        this.getLogger().info("==== LeagueUHC READY ====");
    }

    @Override
    public void onDisable() {
        Bukkit.getWhitelistedPlayers().forEach(players -> players.setWhitelisted(false));

        if (this.gameEngine != null) this.gameEngine.stop();
    }

    public static LeagueUHC getInstance() {
        return instance;
    }

    public GameEngine getGameEngine() {
        return gameEngine;
    }

}
