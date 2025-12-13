package fr.kiza.leagueuhc.core.api.scenario;

public enum ScenarioType {
    CUTCLEAN("cutclean", "CutClean", "Les minerais minés sont automatiquement cuits"),
    GRAB_ORE("grabore", "GrabOre", "Les minerais minés vont directement dans l'inventaire"),
    NO_LAVA("nolava", "NoLava", "La lave ne fait aucun dégât"),
    BOOST_XP("boostxp", "BoostXP", "Augmente l'XP récupérée", true),
    BOOST_POMME("boostpomme", "BoostPomme", "Augmente le drop de pommes", true),
    BOOST_SILEX("boostsilex", "BoostSilex", "Augmente le drop de silex", true),
    BOOST_PLUME("boostplume", "BoostPlume", "Augmente le drop de plumes", true),
    BOOST_DIAMANT("boostdiamant", "BoostDiamant", "Augmente le nombre de diamants", true),
    BOOST_OR("boostor", "BoostOr", "Augmente le nombre d'or", true),
    BOOST_CAVE("boostcave", "BoostCave", "Augmente le nombre de caves", true),
    OUTIL("outil", "Outil", "Les outils craftés ont Efficacité 3 et Solidité 3"),
    ARBRE("arbre", "Arbre", "Casser une bûche détruit tout l'arbre (jusqu'à 10min)"),
    FINAL_HEALTH("finalhealth", "FinalHealth", "Health tous les joueurs à 10min et 19min");

    private final String id;
    private final String displayName;
    private final String description;
    private final boolean hasPercentage;

    ScenarioType(String id, String displayName, String description) {
        this(id, displayName, description, false);
    }

    ScenarioType(String id, String displayName, String description, boolean hasPercentage) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.hasPercentage = hasPercentage;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean hasPercentage() {
        return hasPercentage;
    }

    public static ScenarioType getById(String id) {
        for (ScenarioType type : values()) {
            if (type.getId().equals(id.toLowerCase())) {
                return type;
            }
        }
        return null;
    }
}