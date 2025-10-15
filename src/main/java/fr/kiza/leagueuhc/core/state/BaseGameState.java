package fr.kiza.leagueuhc.core.state;

import fr.kiza.leagueuhc.core.context.GameContext;
import fr.kiza.leagueuhc.core.input.GameInput;
import org.bukkit.Bukkit;

public abstract class BaseGameState implements IGameState {

    protected final String name;

    public BaseGameState(String name) {
        this.name = name;
    }

    protected void broadcast(final String message) {
        Bukkit.getOnlinePlayers().forEach(players -> players.sendMessage(message));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void handleInput(GameContext context, GameInput input) { }
}