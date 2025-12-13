package fr.kiza.leagueuhc.core.api.scenario;

import fr.kiza.leagueuhc.utils.ClassScanner;
import org.bukkit.Bukkit;

import java.lang.reflect.Modifier;
import java.util.logging.Level;

public class ScenarioLoader {

    private static boolean initialized = false;

    public static void initialize(String basePackage, ScenarioManager manager) {
        if (initialized) {
            Bukkit.getLogger().warning("[LeagueUHC] ScenarioLoader already initialized!");
            return;
        }

        Bukkit.getLogger().info("[LeagueUHC] Initializing ScenarioLoader from: " + basePackage);
        int count = 0;

        try {
            for (Class<?> clazz : ClassScanner.findClasses(basePackage)) {
                if (!Scenario.class.isAssignableFrom(clazz)) continue;
                if (!clazz.isAnnotationPresent(ScenarioEntry.class)) continue;
                if (Modifier.isAbstract(clazz.getModifiers())) continue;
                if (clazz.isInterface()) continue;

                try {
                    Scenario scenario = (Scenario) clazz.getDeclaredConstructor().newInstance();
                    manager.register(scenario);
                    count++;
                } catch (Exception e) {
                    Bukkit.getLogger().log(Level.WARNING,
                            "[LeagueUHC] Failed to instantiate scenario: " + clazz.getName(), e);
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE,
                    "[LeagueUHC] Error scanning for scenarios in " + basePackage, e);
        }

        initialized = true;
        Bukkit.getLogger().info("[LeagueUHC] ScenarioLoader initialized with " + count + " scenario(s).");
    }

    public static void reset() {
        initialized = false;
    }
}