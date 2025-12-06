package fr.kiza.leagueuhc.core.game.helper.pregen;

import fr.kiza.leagueuhc.core.api.packets.builder.ActionBarBuilder;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

public class PregenManager {

    private final JavaPlugin plugin;
    private final String worldName = "uhc-world";

    private World world;
    private boolean running = false;
    private boolean paused = false;
    private Player initiator;
    private BukkitRunnable task;

    private final int radius;
    private final int chunksPerTickDefault;
    private final double tpsThreshold = 17.5;

    public PregenManager(JavaPlugin plugin){
        this(plugin,500,6);
    }

    public PregenManager(JavaPlugin plugin,int radius,int chunksPerTickDefault){
        this.plugin = plugin;
        this.radius = radius;
        this.chunksPerTickDefault = Math.max(1,chunksPerTickDefault);
    }

    public World createWorld(){
        if(Bukkit.getWorld(worldName) != null){
            world = Bukkit.getWorld(worldName);
            return world;
        }

        WorldCreator creator = new WorldCreator(worldName);
        creator.environment(World.Environment.NORMAL);
        creator.generateStructures(true);
        creator.generator(new CustomBiome());

        World created = creator.createWorld();
        if(created != null){
            created.setDifficulty(Difficulty.HARD);
            created.setSpawnFlags(true,true);
            created.setPVP(true);
            created.setStorm(false);
            created.setThundering(false);
            created.setWeatherDuration(Integer.MAX_VALUE);
            created.setAutoSave(true);

            WorldBorder border = created.getWorldBorder();
            border.setCenter(0,0);
            border.setSize(radius * 2);
        }
        world = created;
        return created;
    }

    public void deleteWorldFolder(){
        File base = Bukkit.getWorldContainer();
        File target = new File(base, worldName);
        if(!target.exists()) return;
        try{
            Path root = target.toPath();
            Files.walkFileTree(root, new SimpleFileVisitor<Path>(){
                @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)throws IOException{
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                @Override public FileVisitResult postVisitDirectory(Path dir,IOException exc)throws IOException{
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void unloadAndDeleteWorld(){
        World w = Bukkit.getWorld(worldName);
        if(w != null) Bukkit.unloadWorld(w,false);
        deleteWorldFolder();
        world = null;
    }

    public synchronized void startPregen(Player player){
        if(running){
            player.sendMessage(ChatColor.RED + "✘ Une pré-génération est déjà en cours !");
            return;
        }

        if(world == null) world = createWorld();
        if(world == null){
            player.sendMessage(ChatColor.RED + "✘ Erreur: Impossible de créer le monde UHC !");
            return;
        }

        running = true;
        paused = false;
        initiator = player;

        final int chunkRadius = radius >> 4;
        final int size = chunkRadius * 2 + 1;
        final int totalChunks = size * size;
        final AtomicInteger doneCounter = new AtomicInteger(0);

        player.sendMessage(ChatColor.GREEN + "✔ Pré-génération lancée !");
        player.sendMessage(ChatColor.YELLOW + "  Chunks: " + totalChunks);
        player.sendMessage(ChatColor.GRAY + "  Cela peut prendre plusieurs minutes...");

        final TPSMonitor tpsMonitor = new TPSMonitor();

        task = new BukkitRunnable(){
            int x = -chunkRadius;
            int z = -chunkRadius;

            @Override
            public void run(){
                if(!running){ cancel(); return; }
                if(paused) return;
                if(world == null){
                    if(initiator!=null) initiator.sendMessage(ChatColor.RED + "✘ Erreur: Le monde UHC n'existe plus !");
                    running = false;
                    cancel();
                    return;
                }

                tpsMonitor.pulse();
                double tps = tpsMonitor.getTps();
                int chunksThisTick = determineChunksThisTick(tps);

                for(int i=0;i<chunksThisTick;i++){
                    if(x > chunkRadius){
                        finishPregen();
                        return;
                    }

                    try{
                        world.loadChunk(x,z,true);
                    }catch(Exception ignored){}

                    int done = doneCounter.incrementAndGet();
                    if(done % 10 == 0 || done == totalChunks) sendProgress(done,totalChunks);

                    z++;
                    if(z > chunkRadius){
                        z = -chunkRadius;
                        x++;
                    }
                }
            }
        };

        task.runTaskTimer(plugin,1L,1L);
    }

    private void finishPregen(){
        running = false;
        paused = false;

        if(initiator != null && initiator.isOnline()){
            ActionBarBuilder.create().message(ChatColor.GREEN + "✔ Pré-génération terminée !").send(initiator);
            initiator.sendMessage(ChatColor.GREEN + "✔ Pré-génération terminée !");
        }

        Bukkit.getOnlinePlayers().forEach(p -> {
            if(initiator == null || !p.equals(initiator)){
                p.sendMessage(ChatColor.GREEN + "✔ La pré-génération UHC est terminée !");
            }
        });

        if(task != null){
            task.cancel();
            task = null;
        }
    }

    public synchronized void stopPregen(Player player){
        if(task != null){
            task.cancel();
            task = null;
        }
        running = false;
        paused = false;
        initiator = null;
        player.sendMessage(ChatColor.RED + "✘ Pré-génération annulée !");
    }

    public synchronized void pausePregen(Player player){
        if(!running){
            player.sendMessage(ChatColor.RED + "Aucune pré-génération en cours !");
            return;
        }
        paused = true;
        player.sendMessage(ChatColor.YELLOW + "⏸ Pré-génération mise en pause !");
    }

    public synchronized void resumePregen(Player player){
        if(!running){
            player.sendMessage(ChatColor.RED + "Aucune pré-génération en cours !");
            return;
        }
        if(!paused){
            player.sendMessage(ChatColor.YELLOW + "La pré-génération n'est pas en pause !");
            return;
        }
        paused = false;
        player.sendMessage(ChatColor.GREEN + "▶ Pré-génération reprise !");
    }

    public synchronized void resetPregen(){
        if(task != null){
            task.cancel();
            task = null;
        }
        running = false;
        paused = false;
        initiator = null;

        unloadAndDeleteWorld();
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "✔ Pré-génération réinitialisée !");
    }

    private void sendProgress(int done,int total){
        int percent = (int)((done / (double) total) * 100.0);
        String progressBar = createProgressBar(percent);
        String message = ChatColor.YELLOW + "Pré-génération: " + ChatColor.GOLD + percent + "% " + progressBar;

        if(initiator != null && initiator.isOnline()){
            ActionBarBuilder.create().message(message).send(initiator);
        }
    }

    private String createProgressBar(int percent){
        int totalBars = 20;
        int filled = (int)((percent / 100.0)*totalBars);
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.WHITE).append("[");
        for(int i=0;i<totalBars;i++){
            if(i < filled) sb.append(ChatColor.GREEN).append("█");
            else sb.append(ChatColor.DARK_GRAY).append("█");
        }
        sb.append(ChatColor.WHITE).append("]");
        return sb.toString();
    }

    private int determineChunksThisTick(double tps){
        if(tps <= 0) return 1;
        if(tps >= 19.5) return chunksPerTickDefault;
        if(tps >= 18.5) return Math.max(1,chunksPerTickDefault - 2);
        if(tps >= 17.0) return Math.max(1,chunksPerTickDefault - 3);
        return 1;
    }

    public boolean isRunning(){ return running; }
    public boolean isPaused(){ return paused; }
    public World getWorld(){ return world; }

    private static class TPSMonitor {
        private long lastSampleMillis = System.currentTimeMillis();
        private int tickCount = 0;
        private double lastTps = 20.0;

        synchronized void pulse(){
            tickCount++;
            long now = System.currentTimeMillis();
            long delta = now - lastSampleMillis;
            if(delta >= 1000L){
                double seconds = delta / 1000.0;
                lastTps = tickCount / seconds;
                tickCount = 0;
                lastSampleMillis = now;
            }
        }

        synchronized double getTps(){ return lastTps; }
    }
}
