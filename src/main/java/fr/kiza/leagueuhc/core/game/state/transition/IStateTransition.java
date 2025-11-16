package fr.kiza.leagueuhc.core.game.state.transition;

import fr.kiza.leagueuhc.core.game.context.GameContext;

public interface IStateTransition {
    String getFromState();
    String getToState();
    boolean canTransition(GameContext context);
}
