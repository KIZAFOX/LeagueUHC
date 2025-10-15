package fr.kiza.leagueuhc.core.state;

public enum GameState {
    IDLE("IDLE"),
    WAITING("WAITING"),
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