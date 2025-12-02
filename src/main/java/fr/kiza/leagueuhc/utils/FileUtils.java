package fr.kiza.leagueuhc.utils;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.game.helper.pregen.PregenManager;
import fr.kiza.leagueuhc.managers.commands.CommandUHC;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public class FileUtils {

    public static void deleteWorld(World world) {
        File worldFolder = world.getWorldFolder();

        new BukkitRunnable() {
            @Override
            public void run() {
                final PregenManager helper = new PregenManager(LeagueUHC.getInstance());
                helper.resetPregen();

                Bukkit.unloadWorld(world, false);

                try {
                    CommandUHC.pregenManager.resetPregen();
                    deleteFolderRecursively(worldFolder);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }.runTaskLater(LeagueUHC.getInstance(), 20 * 2);
    }

    public static void deleteFolderRecursively(File folder) {
        if(folder == null || !folder.exists()) return;

        File[] files = folder.listFiles();
        if(files != null) {
            for(File file : files) {
                if(file.isDirectory()) {
                    deleteFolderRecursively(file);
                } else {
                    file.delete();
                }
            }
        }
        folder.delete();
    }
}
