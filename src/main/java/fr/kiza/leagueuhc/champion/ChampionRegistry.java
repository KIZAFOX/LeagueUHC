package fr.kiza.leagueuhc.champion;

import fr.kiza.leagueuhc.champion.annotations.ChampionEntry;
import fr.kiza.leagueuhc.utils.ClassScanner;
import org.bukkit.Bukkit;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;

public final class ChampionRegistry {

    private static final Map<String, Champion> REGISTERED = new HashMap<>();

    private ChampionRegistry() {}

    /**
     * Initialise automatiquement tous les champions trouvés dans le package donné.
     * Les classes doivent être annotées avec @ChampionEntry et hériter de Champion.
     */
    public static void initialize(String basePackage) {
        try {
            for (Class<?> clazz : ClassScanner.findClasses(basePackage)) {
                if (Champion.class.isAssignableFrom(clazz)
                        && clazz.isAnnotationPresent(ChampionEntry.class)
                        && !Modifier.isAbstract(clazz.getModifiers())) {
                    Champion champion = (Champion) clazz.getDeclaredConstructor().newInstance();
                    REGISTERED.put(champion.getName().toLowerCase(Locale.ROOT), champion);
                    Bukkit.getLogger().info("Registered champion: " + champion.getName());
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Error while loading champions", e);
        }
    }

    /**
     * Récupère un champion à partir de son nom.
     */
    public static Champion getChampion(final String name) {
        if(name == null) return null;
        return REGISTERED.get(name.toLowerCase(Locale.ROOT));
    }

    /**
     * Renvoie une vue non modifiable de tous les champions enregistrés.
     */
    public static Collection<Champion> getChampions() {
        return Collections.unmodifiableCollection(REGISTERED.values());
    }

    /**
     * Renvoie la liste triée des noms de champions enregistrés.
     * Utile pour l’autocomplétion de commande.
     */
    public static List<String> getRegisteredNames() {
        List<String> names = new ArrayList<>();
        for(Champion champion : REGISTERED.values()) {
            names.add(champion.getName());
        }

        names.sort(String.CASE_INSENSITIVE_ORDER);
        return Collections.unmodifiableList(names);
    }
}
