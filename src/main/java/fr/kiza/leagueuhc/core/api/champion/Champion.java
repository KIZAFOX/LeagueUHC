package fr.kiza.leagueuhc.core.api.champion;

import fr.kiza.leagueuhc.core.api.champion.ability.Ability;
import fr.kiza.leagueuhc.core.game.GamePlayer;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Classe de base pour tous les champions.
 * Chaque champion doit étendre cette classe et être annoté avec @ChampionEntry.
 */
public abstract class Champion {

    /**
     * Catégories de champions (basées sur League of Legends)
     */
    public enum Category {
        ASSASSIN("Assassin", "§c"),
        MAGE("Mage", "§9"),
        TANK("Tank", "§a"),
        FIGHTER("Combattant", "§6"),
        SUPPORT("Support", "§d"),
        MARKSMAN("Tireur", "§e");

        private final String displayName;
        private final String color;

        Category(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getColor() {
            return color;
        }

        public String getColoredName() {
            return color + displayName;
        }
    }

    /**
     * Régions / Factions de Runeterra + modes de jeu spéciaux
     */
    public enum Region {
        // Régions de Runeterra
        DEMACIA("Demacia", "§6§l", "Le royaume de la justice et de la lumière"),
        NOXUS("Noxus", "§c§l", "L'empire de la force et de la conquête"),
        BILGEWATER("Bilgewater", "§3§l", "Le port des pirates et des contrebandiers"),
        TARGON("Targon", "§b§l", "La montagne des Aspects célestes"),
        FRELJORD("Freljord", "§f§l", "Les terres glacées du nord"),
        VOID("Néant", "§5§l", "La dimension du chaos absolu"),
        IONIA("Ionia", "§d§l", "Le royaume de l'équilibre spirituel"),
        PILTOVER("Piltover", "§e§l", "La cité du progrès"),
        ZAUN("Zaun", "§2§l", "La ville souterraine de la chimie"),
        SHURIMA("Shurima", "§6§l", "L'empire du désert ressuscité"),
        SHADOW_ISLES("Îles Obscures", "§8§l", "Le royaume des morts"),
        BANDLE_CITY("Bandle City", "§a§l", "Le monde caché des Yordles"),
        IXTAL("Ixtal", "§2§l", "La jungle aux secrets élémentaires"),
        RUNETERRA("Runeterra", "§7§l", "Vagabonds de Runeterra"),

        // Modes spéciaux (pour le gameplay UHC)
        SOLITAIRE("Solitaire", "§c§l", "Champion jouant seul"),
        DUO("Duo", "§e§l", "Champion jouant en binôme");

        private final String displayName;
        private final String color;
        private final String description;

        Region(String displayName, String color, String description) {
            this.displayName = displayName;
            this.color = color;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getColor() {
            return color;
        }

        public String getDescription() {
            return description;
        }

        public String getColoredName() {
            return color + displayName;
        }

        /**
         * Vérifie si c'est une vraie région de Runeterra (pas un mode de jeu)
         */
        public boolean isLoreRegion() {
            return this != SOLITAIRE && this != DUO;
        }

        /**
         * Vérifie si c'est un mode de jeu spécial
         */
        public boolean isGameMode() {
            return this == SOLITAIRE || this == DUO;
        }
    }

    private final String name;
    private final String description;
    private final Category category;
    private final Region region;
    private final Material icon;
    private final List<Ability> abilities = new ArrayList<>();

    /**
     * Constructeur principal.
     *
     * @param name        Nom du champion
     * @param description Description courte du champion
     * @param category    Catégorie du champion
     * @param region      Région d'origine du champion
     * @param icon        Icône Material pour les menus
     */
    protected Champion(String name, String description, Category category, Region region, Material icon) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.region = region;
        this.icon = icon;
    }

    /**
     * Constructeur sans région (défaut: RUNETERRA).
     */
    protected Champion(String name, String description, Category category, Material icon) {
        this(name, description, category, Region.RUNETERRA, icon);
    }

    /**
     * Enregistre une ability pour ce champion.
     * À appeler dans le constructeur des classes filles.
     *
     * @param ability L'ability à enregistrer
     */
    protected final void registerAbility(Ability ability) {
        ability.setOwningChampion(this);
        this.abilities.add(ability);
    }

    // ═══════════════════════════════════════════════════════════════
    // HOOKS - À override dans les classes filles si nécessaire
    // ═══════════════════════════════════════════════════════════════

    /**
     * Appelé quand le champion est assigné à un joueur.
     * Utilisé pour donner les items, envoyer des messages, etc.
     */
    public void onAssign(GamePlayer player) {}

    /**
     * Appelé quand le champion est retiré d'un joueur.
     * Utilisé pour nettoyer les effets, retirer les items, etc.
     */
    public void onRevoke(GamePlayer player) {}

    /**
     * Appelé chaque tick du serveur pour ce joueur.
     * Attention aux performances - garder le code léger.
     */
    public void onTick(GamePlayer player) {}

    /**
     * Appelé quand le joueur tue un autre joueur.
     */
    public void onKill(GamePlayer player, Player victim) {}

    /**
     * Appelé quand le joueur meurt.
     *
     * @param killer Le tueur (peut être null si mort naturelle)
     */
    public void onDeath(GamePlayer player, Player killer) {}

    /**
     * Appelé quand le joueur inflige des dégâts à une entité.
     */
    public void onDamageDealt(GamePlayer player, org.bukkit.entity.Entity target, double damage) {}

    /**
     * Appelé quand le joueur reçoit des dégâts.
     */
    public void onDamageTaken(GamePlayer player, double damage, org.bukkit.entity.Entity source) {}

    // ═══════════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════════

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public Region getRegion() {
        return region;
    }

    public Material getIcon() {
        return icon;
    }

    public List<Ability> getAbilities() {
        return Collections.unmodifiableList(abilities);
    }

    /**
     * Récupère une ability par son nom.
     */
    public Optional<Ability> getAbility(String name) {
        return abilities.stream()
                .filter(ability -> ability.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Récupère toutes les abilities d'un type de trigger spécifique.
     */
    public List<Ability> getAbilitiesByTrigger(Ability.Trigger trigger) {
        List<Ability> result = new ArrayList<>();
        for (Ability ability : abilities) {
            if (ability.getTrigger() == trigger) {
                result.add(ability);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "Champion{name='" + name + "', category=" + category + ", region=" + region + ", abilities=" + abilities.size() + "}";
    }
}