package fr.kiza.leagueuhc.core.game.helper.pregen;

import fr.kiza.leagueuhc.core.api.packets.builder.ActionBarBuilder;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public class PregenManager {

    private final String worldName = "uhc-world";
    private final JavaPlugin plugin;

    public static World world;
    public static boolean running = false;
    private boolean paused = false;

    private BukkitRunnable task;
    private Player initiator;

    // Radius en blocs (500 = 1000x1000, 750 = 1500x1500, 1000 = 2000x2000)
    public static final int radius = 150; // Change cette valeur selon la taille voulue

    public PregenManager(JavaPlugin plugin){
        this.plugin = plugin;
    }

    public void deleteWorld(){
        World w = Bukkit.getWorld(worldName);
        if(w != null){
            Bukkit.unloadWorld(w, false);
        }
        deleteFolder(new File(worldName));
        world = null;
    }

    public World createWorld(){
        WorldCreator creator = new WorldCreator(worldName);
        creator.environment(World.Environment.NORMAL);
        creator.generateStructures(true);
        creator.generator(new CustomBiome());

        World createdWorld = creator.createWorld();

        if(createdWorld != null) {
            createdWorld.setDifficulty(Difficulty.HARD);
            createdWorld.setSpawnFlags(true, true);
            createdWorld.setPVP(true);
            createdWorld.setStorm(false);
            createdWorld.setThundering(false);
            createdWorld.setWeatherDuration(Integer.MAX_VALUE);
            createdWorld.setAutoSave(true);

            WorldBorder border = createdWorld.getWorldBorder();
            border.setCenter(0, 0);
            border.setSize(radius * 2);

            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "✔ Monde UHC créé avec succès !");
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "  Taille: " + (radius * 2) + "x" + (radius * 2) + " blocs");
        }

        return createdWorld;
    }

    public void startPregen(Player player){
        if(world == null) {
            world = createWorld();
        }

        if(world == null) {
            player.sendMessage(ChatColor.RED + "✘ Erreur: Impossible de créer le monde UHC !");
            return;
        }

        running = true;
        paused = false;
        initiator = player;

        int chunkRadius = radius >> 4; // Convertit les blocs en chunks (divise par 16)

        final World pregenWorld = world;

        player.sendMessage(ChatColor.GREEN + "✔ Pré-génération lancée !");
        player.sendMessage(ChatColor.YELLOW + "  Taille: " + (radius * 2) + "x" + (radius * 2) + " blocs");
        player.sendMessage(ChatColor.YELLOW + "  Chunks: " + ((chunkRadius * 2 + 1) * (chunkRadius * 2 + 1)) + " chunks à générer");
        player.sendMessage(ChatColor.GRAY + "  Cela peut prendre plusieurs minutes...");

        task = new BukkitRunnable(){
            int x = -chunkRadius;
            int z = -chunkRadius;

            @Override
            public void run(){
                if(!running){
                    cancel();
                    return;
                }

                if(paused) return;

                if(x > chunkRadius){
                    Bukkit.getOnlinePlayers().forEach(players -> {
                        ActionBarBuilder.create().message(ChatColor.GREEN + "✔ Pré-génération terminée !").send(players);
                        players.sendMessage(ChatColor.GREEN + "✔ Pré-génération terminée !");
                        players.sendMessage(ChatColor.YELLOW + "  Map de " + (radius * 2) + "x" + (radius * 2) + " prête !");
                    });
                    running = false;
                    paused = false;
                    cancel();
                    return;
                }

                if(pregenWorld == null) {
                    initiator.sendMessage(ChatColor.RED + "✘ Erreur: Le monde UHC n'existe plus !");
                    running = false;
                    cancel();
                    return;
                }

                pregenWorld.loadChunk(x, z, true);

                // Affiche la progression tous les 10 chunks
                if(z % 10 == 0){
                    int done = (x + chunkRadius) * (chunkRadius*2+1) + (z + chunkRadius);
                    int total = (chunkRadius*2+1) * (chunkRadius*2+1);
                    sendProgress(done, total);
                }

                z++;

                if(z > chunkRadius){
                    z = -chunkRadius;
                    x++;
                }
            }
        };

        task.runTaskTimer(plugin, 1L, 1L);
    }

    public void stopPregen(Player player){
        if(task != null) task.cancel();
        running = false;
        paused = false;
        initiator = null;
        player.sendMessage(ChatColor.RED + "✘ Pré-génération annulée !");
    }

    public void pausePregen(Player player){
        if(!running) return;
        paused = true;
        player.sendMessage(ChatColor.YELLOW + "⏸ Pré-génération mise en pause !");
    }

    public void resumePregen(Player player){
        if(!running || !paused) return;
        paused = false;
        player.sendMessage(ChatColor.GREEN + "▶ Pré-génération reprise !");
    }

    public void resetPregen() {
        if(task != null) {
            task.cancel();
            task = null;
        }

        running = false;
        paused = false;
        initiator = null;

        deleteWorld();

        world = null;

        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "✔ Pré-génération réinitialisée !");
    }

    private void sendProgress(int done, int total){
        int percent = (int)((done / (double) total) * 100);

        final String
                progressBar = createProgressBar(percent),
                message = ChatColor.YELLOW + "Pré-génération: " + ChatColor.GOLD + percent + "% " + progressBar;

        if(initiator != null && initiator.isOnline()){
            ActionBarBuilder.create().message(message).send(initiator);
        }

        Bukkit.getOnlinePlayers().forEach(players -> ActionBarBuilder.create().message(message).send(players));
    }

    private String createProgressBar(int percent) {
        int totalBars = 20;
        int filledBars = (int) ((percent / 100.0) * totalBars);

        StringBuilder bar = new StringBuilder();
        bar.append(ChatColor.WHITE).append("[");

        for (int i = 0; i < totalBars; i++) {
            if (i < filledBars) {
                bar.append(ChatColor.GREEN).append("█");
            } else {
                bar.append(ChatColor.DARK_GRAY).append("█");
            }
        }

        bar.append(ChatColor.WHITE).append("]");
        return bar.toString();
    }

    private void deleteFolder(File file){
        if(!file.exists()) return;
        File[] files = file.listFiles();
        if(files != null){
            for(File f : files){
                if(f.isDirectory()) deleteFolder(f);
                else f.delete();
            }
        }
        file.delete();
    }

    public boolean isRunning(){ return running; }

    public boolean isPaused(){ return paused; }

    public World getWorld(){ return world; }
}