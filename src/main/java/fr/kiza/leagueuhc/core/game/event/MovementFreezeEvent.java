package fr.kiza.leagueuhc.core.game.event;

public class MovementFreezeEvent {
    private final boolean frozen;

    public MovementFreezeEvent(boolean frozen) {
        this.frozen = frozen;
    }

    public boolean isFrozen() {
        return frozen;
    }
}