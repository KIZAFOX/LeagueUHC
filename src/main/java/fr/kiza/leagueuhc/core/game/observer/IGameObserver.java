package fr.kiza.leagueuhc.core.game.observer;

import fr.kiza.leagueuhc.core.game.event.GameEvent;

import java.util.UUID;

public interface IGameObserver {
    void onStateChanged(final String oldState, final String newState);
    void onScoreChanged(final UUID playerId, final int newScore);
    void onGameEvent(final GameEvent event);
}
