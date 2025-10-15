package fr.kiza.leagueuhc.core.state.transition;

import fr.kiza.leagueuhc.core.context.GameContext;

import java.util.function.Predicate;

public class StateTransition implements IStateTransition {

    private final String fromState;
    private final String toState;

    private final Predicate<GameContext> condition;

    public StateTransition(String fromState, String toState, Predicate<GameContext> condition) {
        this.fromState = fromState;
        this.toState = toState;
        this.condition = condition;
    }

    @Override
    public String getFromState() {
        return fromState;
    }

    @Override
    public String getToState() {
        return toState;
    }

    @Override
    public boolean canTransition(GameContext context) {
        return condition.test(context);
    }
}
