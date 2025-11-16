package fr.kiza.leagueuhc.core.api.camp;

public enum Camp {

    DEMACIA("Demacia"),
    NOXUS("Noxus"),
    SOLITAIRE("Solitaire"),
    DUO("Duo"),
    BILGEWATER("Bilgewater"),
    FRELJORD("Freljord"),
    TARGON("Targon"),
    VOID("Void");

    private final String name;

    Camp(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
