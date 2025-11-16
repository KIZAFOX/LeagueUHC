package fr.kiza.leagueuhc.core.game.scenario;

public enum Scenario {
    CUTCLEAN("CutClean", "Les minerais minés sont automatiquement cuits"),
    GRAB_ORE("GrabOre", "Les minerais minés vont directement dans l'inventaire"),
    NO_LAVA("NoLava", "La lave ne fait aucun dégât"),
    BOOST_XP("BoostXP", "Augmente l'XP récupérée", true),
    BOOST_POMME("BoostPomme", "Augmente le drop de pommes", true),
    BOOST_SILEX("BoostSilex", "Augmente le drop de silex", true),
    BOOST_PLUME("BoostPlume", "Augmente le drop de plumes", true),
    BOOST_DIAMANT("BoostDiamant", "Augmente le nombre de diamants", true),
    BOOST_OR("BoostOr", "Augmente le nombre d'or", true),
    BOOST_CAVE("BoostCave", "Augmente le nombre de caves", true),
    OUTIL("Outil", "Les outils craftés ont Efficacité 3 et Solidité 3"),
    ARBRE("Arbre", "Casser une bûche détruit tout l'arbre (jusqu'à 10min)"),
    FINAL_HEALTH("FinalHealth", "Health tous les joueurs à 10min et 19min");

    private final String name;
    private final String description;
    private final boolean hasPercentage;

    Scenario(String name, String description) {
        this(name, description, false);
    }

    Scenario(String name, String description, boolean hasPercentage) {
        this.name = name;
        this.description = description;
        this.hasPercentage = hasPercentage;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean hasPercentage() {
        return hasPercentage;
    }

    public static Scenario getByName(String name) {
        for (Scenario scenario : values()) {
            if (scenario.getName().equalsIgnoreCase(name)) {
                return scenario;
            }
        }
        return null;
    }
}