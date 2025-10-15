package fr.kiza.leagueuhc.core.state.transition;

import fr.kiza.leagueuhc.core.context.GameContext;

public interface IStateTransition {
    String getFromState();
    String getToState();
    boolean canTransition(GameContext context);
}
