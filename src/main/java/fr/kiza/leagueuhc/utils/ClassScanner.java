package fr.kiza.leagueuhc.utils;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class ClassScanner {

    private ClassScanner() {}

    public static Set<Class<?>> findClasses(String basePackage) throws IOException, ClassNotFoundException {
        Set<Class<?>> classes = new HashSet<>();
        String path = basePackage.replace('.', '/');

        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(ClassScanner.class);
        ClassLoader classLoader = plugin.getClass().getClassLoader();

        URL resource = classLoader.getResource(path);
        if (resource == null) {
            plugin.getLogger().warning("Aucun chemin trouvé pour le package: " + basePackage);
            return classes;
        }

        if (!"jar".equals(resource.getProtocol())) {
            plugin.getLogger().warning("Le chemin n’est pas un JAR: " + resource);
            return classes;
        }

        JarURLConnection connection = (JarURLConnection) resource.openConnection();
        try (JarFile jarFile = connection.getJarFile()) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (name.startsWith(path) && name.endsWith(".class") && !entry.isDirectory()) {
                    String className = name.replace('/', '.').substring(0, name.length() - 6);
                    Class<?> clazz = Class.forName(className, false, classLoader);
                    classes.add(clazz);
                }
            }
        }
        return classes;
    }
}
