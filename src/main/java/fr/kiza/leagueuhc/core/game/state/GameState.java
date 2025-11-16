package fr.kiza.leagueuhc.core.game.state;

public enum GameState {
    IDLE("IDLE"),
    STARTING("STARTING"),
    PLAYING("PLAYING"),
    ENDING("ENDING"),
    FINISHED("FINISHED");

    private final String name;

    GameState(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}