package fr.kiza.leagueuhc.core.game.helper.pregen;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.api.packets.builder.ActionBarBuilder;
import fr.kiza.leagueuhc.core.game.cycle.DayCycleManager;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PregenManager {

    private final LeagueUHC plugin;
    private final String worldName = "uhc_world";

    private World world;
    private boolean running = false;
    private boolean paused = false;
    private Player initiator;
    private BukkitRunnable task;

    private final int currentRadius = 500;
    private final int chunksPerTickDefault = 6;

    public PregenManager(LeagueUHC plugin) {
        this.plugin = plugin;
    }

    public World createWorld() {
        if (Bukkit.getWorld(worldName) != null) {
            world = Bukkit.getWorld(worldName);
            plugin.getLogger().info("[Pregen] Monde existant trouvé : " + worldName);

            // Forcer le jour même sur un monde existant
            DayCycleManager.forceDay(world);

            return world;
        }

        plugin.getLogger().info("[Pregen] Création du monde " + worldName + "...");

        WorldCreator creator = new WorldCreator(worldName);
        creator.environment(World.Environment.NORMAL);
        creator.generateStructures(true);
        creator.seed(new Random().nextLong());

        World created = creator.createWorld();
        if (created != null) {
            created.setDifficulty(Difficulty.HARD);
            created.setSpawnFlags(true, true);
            created.setPVP(true);
            created.setAutoSave(true);
            created.setSpawnLocation(0, 70, 0);

            DayCycleManager.forceDay(created);

            WorldBorder border = created.getWorldBorder();
            border.setCenter(0, 0);
            border.setSize(currentRadius * 2);
            border.setWarningDistance(10);
            border.setWarningTime(15);
            border.setDamageAmount(0.2);
            border.setDamageBuffer(5);

            plugin.getLogger().info("[Pregen] ✓ Monde VANILLA créé (seed: " + created.getSeed() + ")");
            plugin.getLogger().info("[Pregen]   Bordure : " + (currentRadius * 2) + "x" + (currentRadius * 2) + " blocs");
        }

        world = created;
        return created;
    }

    public void deleteWorldFolder() {
        File base = Bukkit.getWorldContainer();
        File target = new File(base, worldName);
        if (!target.exists()) {
            plugin.getLogger().info("[Pregen] Aucun monde à supprimer");
            return;
        }

        plugin.getLogger().info("[Pregen] Suppression COMPLÈTE du monde " + worldName + "...");

        try {
            Path root = target.toPath();
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            plugin.getLogger().info("[Pregen] ✓ Monde supprimé complètement");
        } catch (IOException e) {
            plugin.getLogger().severe("[Pregen] ✗ Erreur lors de la suppression du monde !");
            e.printStackTrace();
        }
    }

    public void unloadAndDeleteWorld() {
        World w = Bukkit.getWorld(worldName);
        if (w != null) {
            plugin.getLogger().info("[Pregen] Déchargement du monde...");
            for (Player p : w.getPlayers()) {
                p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                p.sendMessage("§e§l[Pregen] §7Vous avez été téléporté car le monde est supprimé");
            }
            Bukkit.unloadWorld(w, false);
        }

        Bukkit.getScheduler().runTaskLater(plugin, this::deleteWorldFolder, 20L);

        world = null;
    }

    public synchronized void startPregen(Player player) {
        if (running) {
            player.sendMessage("§c§l[Pregen] §7Une pré-génération est déjà en cours !");
            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        if (Bukkit.getWorld(worldName) != null) {
            player.sendMessage("§e§l[Pregen] §7Suppression de l'ancienne map...");
            unloadAndDeleteWorld();

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                continuePregen(player, currentRadius);
            }, 40L);
            return;
        }

        continuePregen(player, currentRadius);
    }

    private void continuePregen(Player player, int radius) {
        world = createWorld();
        if (world == null) {
            player.sendMessage("§c§l[Pregen] §7Erreur: Impossible de créer le monde UHC !");
            player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0f, 0.5f);
            return;
        }

        running = true;
        paused = false;
        initiator = player;

        final int chunkRadius = radius >> 4;
        final int size = chunkRadius * 2 + 1;
        final int totalChunks = size * size;
        final AtomicInteger doneCounter = new AtomicInteger(0);

        player.sendMessage("§a§l========================================");
        player.sendMessage("§6§l    PRÉ-GÉNÉRATION DU MONDE UHC");
        player.sendMessage("");
        player.sendMessage("§7Taille : §e" + (radius * 2) + "x" + (radius * 2) + " blocs");
        player.sendMessage("§7Bordure : §e" + (radius * 2) + " blocs");
        player.sendMessage("§7Chunks : §e" + totalChunks);
        player.sendMessage("§7Seed : §e" + world.getSeed());
        player.sendMessage("");
        player.sendMessage("§2Génération : Monde vanilla + Dark Forest au centre");
        player.sendMessage("");
        player.sendMessage("§7Cela peut prendre plusieurs minutes...");
        player.sendMessage("§a§l========================================");
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.5f);

        final TPSMonitor tpsMonitor = new TPSMonitor();

        task = new BukkitRunnable() {
            int x = -chunkRadius;
            int z = -chunkRadius;

            @Override
            public void run() {
                if (!running) {
                    cancel();
                    return;
                }
                if (paused) return;

                if (world == null) {
                    if (initiator != null) {
                        initiator.sendMessage("§c§l[Pregen] §7Erreur: Le monde UHC n'existe plus !");
                    }
                    running = false;
                    cancel();
                    return;
                }

                tpsMonitor.pulse();
                double tps = tpsMonitor.getTps();
                int chunksThisTick = determineChunksThisTick(tps);

                for (int i = 0; i < chunksThisTick; i++) {
                    if (x > chunkRadius) {
                        finishPregen();
                        return;
                    }

                    try {
                        world.loadChunk(x, z, true);
                    } catch (Exception ignored) {
                    }

                    int done = doneCounter.incrementAndGet();

                    if (initiator != null && initiator.isOnline()) {
                        int percent = (int) ((done / (double) totalChunks) * 100.0);
                        ActionBarBuilder.create()
                                .message("§6⚡ PREGEN §8» §e" + percent + "% §8[" + createCompactBar(percent) + "§8] §7" + done + "/" + totalChunks)
                                .send(Bukkit.getOnlinePlayers().stream().filter(ServerOperator::isOp).collect(Collectors.toList()));
                    }

                    z++;
                    if (z > chunkRadius) {
                        z = -chunkRadius;
                        x++;
                    }
                }
            }
        };

        task.runTaskTimer(plugin, 1L, 1L);
    }

    private void finishPregen() {
        running = false;
        paused = false;

        if (world != null && initiator != null) {
            initiator.sendMessage("§6§l[Pregen] §7Changement du biome en Dark Forest...");

            ActionBarBuilder.create()
                    .message("§6⚡ PREGEN §8» §eChangement du biome en cours...")
                    .send(initiator);

            plugin.getLogger().info("[Pregen] Changement du biome au centre...");

            int forestRadius = 350;
            int forestChunkRadius = forestRadius >> 4;

            for (int cx = -forestChunkRadius; cx <= forestChunkRadius; cx++) {
                for (int cz = -forestChunkRadius; cz <= forestChunkRadius; cz++) {
                    try {
                        org.bukkit.Chunk chunk = world.getChunkAt(cx, cz);

                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                int worldX = cx * 16 + x;
                                int worldZ = cz * 16 + z;
                                double distance = Math.sqrt(worldX * worldX + worldZ * worldZ);

                                if (distance <= forestRadius) {
                                    world.setBiome(worldX, worldZ, Biome.ROOFED_FOREST);
                                }
                            }
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("[Pregen] Erreur changement biome en " + cx + "," + cz);
                    }
                }
            }

            plugin.getLogger().info("[Pregen] ✓ Biome changé en ROOFED_FOREST");

            initiator.sendMessage("§6§l[Pregen] §7Génération de la Dark Forest...");

            ActionBarBuilder.create()
                    .message("§6⚡ PREGEN §8» §eGénération de la Dark Forest...")
                    .send(initiator);

            plugin.getLogger().info("[Pregen] Génération de la Dark Forest...");

            DarkForestPopulator populator = new DarkForestPopulator(forestRadius);

            for (int x = -forestChunkRadius; x <= forestChunkRadius; x++) {
                for (int z = -forestChunkRadius; z <= forestChunkRadius; z++) {
                    try {
                        org.bukkit.Chunk chunk = world.getChunkAt(x, z);
                        populator.populate(world, new java.util.Random(), chunk);
                    } catch (Exception e) {
                        plugin.getLogger().warning("[Pregen] Erreur génération arbres en " + x + "," + z);
                    }
                }
            }

            plugin.getLogger().info("[Pregen] ✓ Dark Forest générée !");
        }

        if (initiator != null && initiator.isOnline()) {
            ActionBarBuilder.create()
                    .message("§a✔ PREGEN TERMINÉE §8» §7Monde prêt !")
                    .send(initiator);

            initiator.sendMessage("§a§l========================================");
            initiator.sendMessage("§6§l    PRÉ-GÉNÉRATION TERMINÉE !");
            initiator.sendMessage("");
            initiator.sendMessage("§7Le monde §euhc_world §7est prêt");
            initiator.sendMessage("§7Taille : §e" + (currentRadius * 2) + "x" + (currentRadius * 2));
            initiator.sendMessage("§7Bordure : §e" + (currentRadius * 2) + " blocs");
            initiator.sendMessage("§2Dark Forest au centre (700x700)");
            initiator.sendMessage("");
            initiator.sendMessage("§7Utilisez §e/tppregen §7pour voir le résultat");
            initiator.sendMessage("§aVous pouvez maintenant lancer la partie !");
            initiator.sendMessage("§a§l========================================");
            initiator.playSound(initiator.getLocation(), Sound.LEVEL_UP, 1.0f, 2.0f);
        }

        Bukkit.getOnlinePlayers().forEach(p -> {
            if (initiator == null || !p.equals(initiator)) {
                p.sendMessage("§a§l[Pregen] §7La pré-génération UHC est terminée !");
            }
        });

        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public synchronized void stopPregen(Player player) {
        if (!running) {
            player.sendMessage("§c§l[Pregen] §7Aucune pré-génération en cours !");
            return;
        }

        if (task != null) {
            task.cancel();
            task = null;
        }
        running = false;
        paused = false;
        initiator = null;

        player.sendMessage("§c§l[Pregen] §7Pré-génération annulée !");
        player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0f, 1.0f);

        ActionBarBuilder.create()
                .message("§c✘ PREGEN ANNULÉE")
                .send(player);
    }

    private String createCompactBar(int percent) {
        int totalBars = 20;
        int filled = (int) ((percent / 100.0) * totalBars);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < totalBars; i++) {
            if (i < filled) sb.append("§a▌");
            else sb.append("§8▌");
        }
        return sb.toString();
    }

    private int determineChunksThisTick(double tps) {
        if (tps <= 0) return 1;
        if (tps >= 19.5) return chunksPerTickDefault;
        if (tps >= 18.5) return Math.max(1, chunksPerTickDefault - 2);
        if (tps >= 17.0) return Math.max(1, chunksPerTickDefault - 3);
        return 1;
    }

    public boolean isRunning() {
        return running;
    }

    public World getWorld() {
        return world;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getCurrentRadius() {
        return currentRadius;
    }

    private static class TPSMonitor {
        private long lastSampleMillis = System.currentTimeMillis();
        private int tickCount = 0;
        private double lastTps = 20.0;

        synchronized void pulse() {
            tickCount++;
            long now = System.currentTimeMillis();
            long delta = now - lastSampleMillis;
            if (delta >= 1000L) {
                double seconds = delta / 1000.0;
                lastTps = tickCount / seconds;
                tickCount = 0;
                lastSampleMillis = now;
            }
        }

        synchronized double getTps() {
            return lastTps;
        }
    }
}