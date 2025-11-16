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
    private Player initiator; // Le joueur qui a lancé la pregen

    public PregenManager(JavaPlugin plugin){
        this.plugin = plugin;
    }

    public boolean isRunning(){ return running; }
    public boolean isPaused(){ return paused; }
    public World getWorld(){ return world; }

    public void deleteWorld(){
        World w = Bukkit.getWorld(worldName);
        if(w != null){
            Bukkit.unloadWorld(w, false);
        }
        deleteFolder(new File(worldName));
        world = null;
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

    public World createWorld(){
        WorldCreator creator = new WorldCreator(worldName);
        creator.environment(World.Environment.NORMAL);
        creator.generateStructures(true); // Active les structures (villages, temples, etc.)
        creator.generator(new CustomBiome()); // Applique notre générateur de biome custom

        World createdWorld = creator.createWorld();

        if(createdWorld != null) {
            // Configure la bordure du monde
            WorldBorder border = createdWorld.getWorldBorder();
            border.setCenter(0, 0);
            border.setSize(1000); // 1000 blocs de diamètre (500 de rayon)

            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "✔ Monde UHC créé avec succès !");
        }

        return createdWorld;
    }

    public void startPregen(Player player){
        // Crée ou récupère le monde AVANT de démarrer la task
        if(world == null) {
            world = createWorld();
        }

        // Vérifie que le monde a bien été créé
        if(world == null) {
            player.sendMessage(ChatColor.RED + "✘ Erreur: Impossible de créer le monde UHC !");
            return;
        }

        running = true;
        paused = false;
        initiator = player; // Garde une référence du joueur

        // 1000x1000
        int radius = 500;
        int chunkRadius = radius >> 4; // block → chunk

        final World pregenWorld = world; // Référence finale pour la task

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
                    sendAll(ChatColor.GREEN + "✔ Pré-génération terminée !");
                    sendMessage(initiator, ChatColor.GREEN + "✔ Pré-génération terminée !");
                    running = false;
                    paused = false;
                    cancel();
                    return;
                }

                // Sécurité: vérifie que le monde existe toujours
                if(pregenWorld == null) {
                    sendMessage(initiator, ChatColor.RED + "✘ Erreur: Le monde UHC n'existe plus !");
                    running = false;
                    cancel();
                    return;
                }

                pregenWorld.loadChunk(x, z, true);

                // Affiche la progression toutes les 10 chunks
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
        player.sendMessage(ChatColor.GREEN + "✔ Pré-génération lancée !");
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

        // Crée une barre de progression visuelle
        String progressBar = createProgressBar(percent);

        String message = ChatColor.YELLOW + "Pré-génération: " +
                ChatColor.GOLD + percent + "% " +
                progressBar;

        // Envoie au joueur qui a lancé la pregen
        if(initiator != null && initiator.isOnline()){
            ActionBarBuilder.create().message(message).send(initiator);
        }

        // Envoie aussi à tous les admins en ligne
        sendAll(message);
    }

    /**
     * Crée une barre de progression visuelle
     */
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

    private void sendAll(String message){
        for(Player p : Bukkit.getOnlinePlayers()){
            // Envoie seulement aux OPs ou joueurs avec permission
            if(p.isOp() || p.hasPermission("uhc.admin")){
                ActionBarBuilder.create().message(message).send(p);
            }
        }
    }

    /**
     * Envoie un message normal à un joueur s'il est en ligne
     */
    private void sendMessage(Player player, String message){
        if(player != null && player.isOnline()){
            player.sendMessage(message);
        }
    }
}