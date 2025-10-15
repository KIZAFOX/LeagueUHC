package fr.kiza.leagueuhc.core.state;

import fr.kiza.leagueuhc.core.context.GameContext;
import fr.kiza.leagueuhc.core.input.GameInput;

public interface IGameState {
    String getName();
    void onEnter(final GameContext context);
    void onExit(final GameContext context);
    void update(final GameContext context, long deltaTime);
    void handleInput(final GameContext context, GameInput input);
}
