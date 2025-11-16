package fr.kiza.leagueuhc.core.game.state;

import fr.kiza.leagueuhc.core.game.context.GameContext;
import fr.kiza.leagueuhc.core.game.input.GameInput;
import fr.kiza.leagueuhc.core.game.observer.IGameObserver;
import fr.kiza.leagueuhc.core.game.state.transition.StateTransition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StateManager {

    private IGameState currentState;
    private final Map<String, IGameState> states;
    private final List<StateTransition> transitions;
    private final List<IGameObserver> observers;

    public StateManager() {
        this.states = new HashMap<>();
        this.transitions = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    public void registerState(IGameState state) {
        states.put(state.getName(), state);
    }

    public void registerTransition(StateTransition transition) {
        transitions.add(transition);
    }

    public void addObserver(IGameObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(IGameObserver observer) {
        observers.remove(observer);
    }

    public boolean changeState(String stateName, GameContext context) {
        IGameState newState = states.get(stateName);
        if (newState == null) {
            System.err.println("[MiniGame] État introuvable: " + stateName);
            return false;
        }

        String oldStateName = currentState != null ? currentState.getName() : "NONE";

        if (currentState != null) {
            currentState.onExit(context);
        }

        currentState = newState;
        currentState.onEnter(context);

        notifyStateChanged(oldStateName, stateName);
        return true;
    }

    public void update(GameContext context, long deltaTime) {
        if (currentState == null) return;

        checkTransitions(context);
        currentState.update(context, deltaTime);
    }

    public void handleInput(GameContext context, GameInput input) {
        if (currentState != null) {
            currentState.handleInput(context, input);
        }
    }

    private void checkTransitions(GameContext context) {
        if (currentState == null) return;

        for (StateTransition transition : transitions) {
            if (transition.getFromState().equals(currentState.getName())) {
                if (transition.canTransition(context)) {
                    changeState(transition.getToState(), context);
                    break;
                }
            }
        }
    }

    private void notifyStateChanged(String oldState, String newState) {
        for (IGameObserver observer : observers) {
            observer.onStateChanged(oldState, newState);
        }
    }

    public String getCurrentStateName() {
        return currentState != null ? currentState.getName() : "NONE";
    }
}