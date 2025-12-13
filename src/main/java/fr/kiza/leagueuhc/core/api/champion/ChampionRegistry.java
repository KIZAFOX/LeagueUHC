package fr.kiza.leagueuhc.core.api.champion;

import fr.kiza.leagueuhc.core.api.champion.annotations.ChampionEntry;
import fr.kiza.leagueuhc.utils.ClassScanner;

import org.bukkit.Bukkit;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Registre central pour tous les champions.
 * Gère l'enregistrement automatique et manuel des champions.
 */
public final class ChampionRegistry {

    private static final Map<String, Champion> REGISTERED = new HashMap<>();
    private static boolean initialized = false;

    private ChampionRegistry() {}

    /**
     * Initialise automatiquement tous les champions trouvés dans le package donné.
     * Les classes doivent être annotées avec @ChampionEntry et hériter de Champion.
     *
     * @param basePackage Le package racine à scanner
     */
    public static void initialize(String basePackage) {
        if (initialized) {
            Bukkit.getLogger().warning("ChampionRegistry already initialized!");
            return;
        }

        Bukkit.getLogger().info("Initializing ChampionRegistry from package: " + basePackage);
        int count = 0;

        try {
            for (Class<?> clazz : ClassScanner.findClasses(basePackage)) {
                if (!Champion.class.isAssignableFrom(clazz)) continue;
                if (!clazz.isAnnotationPresent(ChampionEntry.class)) continue;
                if (Modifier.isAbstract(clazz.getModifiers())) continue;

                try {
                    Champion champion = (Champion) clazz.getDeclaredConstructor().newInstance();
                    register(champion);
                    count++;
                } catch (Exception e) {
                    Bukkit.getLogger().log(Level.WARNING, "Failed to instantiate champion: " + clazz.getName(), e);
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Error scanning for champions", e);
        }

        initialized = true;
        Bukkit.getLogger().info("ChampionRegistry initialized with " + count + " champions");
    }

    /**
     * Enregistre manuellement un champion.
     */
    public static void register(Champion champion) {
        String key = champion.getName().toLowerCase(Locale.ROOT);

        if (REGISTERED.containsKey(key)) {
            Bukkit.getLogger().warning("Champion already registered: " + champion.getName() + " (overwriting)");
        }

        REGISTERED.put(key, champion);
        Bukkit.getLogger().info("Registered champion: " + champion.getName() + " [" + champion.getCategory().getDisplayName() + "]");
    }

    /**
     * Désenregistre un champion.
     */
    public static void unregister(String name) {
        Champion removed = REGISTERED.remove(name.toLowerCase(Locale.ROOT));
        if (removed != null) {
            Bukkit.getLogger().info("Unregistered champion: " + removed.getName());
        }
    }

    /**
     * Récupère un champion par son nom.
     */
    public static Champion getChampion(String name) {
        if (name == null) return null;
        return REGISTERED.get(name.toLowerCase(Locale.ROOT));
    }

    /**
     * Récupère un champion par nom de classe.
     */
    public static Champion getChampionByClass(Class<? extends Champion> clazz) {
        for (Champion champion : REGISTERED.values()) {
            if (champion.getClass().equals(clazz)) {
                return champion;
            }
        }
        return null;
    }

    /**
     * Vérifie si un champion est enregistré.
     */
    public static boolean isRegistered(String name) {
        return name != null && REGISTERED.containsKey(name.toLowerCase(Locale.ROOT));
    }

    /**
     * Renvoie une vue non modifiable de tous les champions enregistrés.
     */
    public static Collection<Champion> getChampions() {
        return Collections.unmodifiableCollection(REGISTERED.values());
    }

    /**
     * Renvoie les champions d'une catégorie spécifique.
     */
    public static List<Champion> getChampionsByCategory(Champion.Category category) {
        return REGISTERED.values().stream()
                .filter(c -> c.getCategory() == category)
                .collect(Collectors.toList());
    }

    /**
     * Renvoie les champions d'une région spécifique.
     */
    public static List<Champion> getChampionsByRegion(Champion.Region region) {
        return REGISTERED.values().stream()
                .filter(c -> c.getRegion() == region)
                .collect(Collectors.toList());
    }

    /**
     * Renvoie les champions Solitaires (pour le mode solo).
     */
    public static List<Champion> getSoloChampions() {
        return getChampionsByRegion(Champion.Region.SOLITAIRE);
    }

    /**
     * Renvoie les champions Duo (pour le mode duo).
     */
    public static List<Champion> getDuoChampions() {
        return getChampionsByRegion(Champion.Region.DUO);
    }

    /**
     * Renvoie la liste triée des noms de champions enregistrés.
     * Utile pour l'autocomplétion de commande.
     */
    public static List<String> getRegisteredNames() {
        List<String> names = new ArrayList<>();
        for (Champion champion : REGISTERED.values()) {
            names.add(champion.getName());
        }
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return Collections.unmodifiableList(names);
    }

    /**
     * Renvoie les noms filtrés par une chaîne de recherche.
     * Utile pour l'autocomplétion.
     */
    public static List<String> getMatchingNames(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return getRegisteredNames();
        }

        String lowerPrefix = prefix.toLowerCase(Locale.ROOT);
        return REGISTERED.values().stream()
                .map(Champion::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(lowerPrefix))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    /**
     * Renvoie un champion aléatoire.
     */
    public static Champion getRandomChampion() {
        if (REGISTERED.isEmpty()) return null;
        List<Champion> champions = new ArrayList<>(REGISTERED.values());
        return champions.get(new Random().nextInt(champions.size()));
    }

    /**
     * Renvoie des champions aléatoires uniques.
     *
     * @param count Nombre de champions à retourner
     * @return Liste de champions (peut être plus petite si pas assez de champions)
     */
    public static List<Champion> getRandomChampions(int count) {
        List<Champion> champions = new ArrayList<>(REGISTERED.values());
        Collections.shuffle(champions);
        return champions.subList(0, Math.min(count, champions.size()));
    }

    /**
     * Renvoie le nombre de champions enregistrés.
     */
    public static int getCount() {
        return REGISTERED.size();
    }

    /**
     * Nettoie le registre.
     */
    public static void clear() {
        REGISTERED.clear();
        initialized = false;
        Bukkit.getLogger().info("ChampionRegistry cleared");
    }
}