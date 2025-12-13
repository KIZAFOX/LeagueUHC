package fr.kiza.leagueuhc.core.game.helper.pregen;

import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Random;

public class DarkForestPopulator {

    private final int forestRadius;

    public DarkForestPopulator(int forestRadius) {
        this.forestRadius = forestRadius;
    }

    public void populate(World world, Random random, org.bukkit.Chunk chunk) {
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        int centerBlockX = chunkX * 16 + 8;
        int centerBlockZ = chunkZ * 16 + 8;
        double distanceFromCenter = Math.sqrt(centerBlockX * centerBlockX + centerBlockZ * centerBlockZ);

        if (distanceFromCenter > forestRadius) return;

        int treeCount;
        if (distanceFromCenter < forestRadius * 0.3) {
            treeCount = 12 + random.nextInt(5);
        } else if (distanceFromCenter < forestRadius * 0.6) {
            treeCount = 10 + random.nextInt(5);
        } else {
            treeCount = 8 + random.nextInt(5);
        }

        for (int i = 0; i < treeCount; i++) {
            for (int attempt = 0; attempt < 3; attempt++) {
                int x = chunkX * 16 + random.nextInt(16);
                int z = chunkZ * 16 + random.nextInt(16);

                int y = world.getHighestBlockYAt(x, z);

                Block ground = world.getBlockAt(x, y - 1, z);
                Material groundType = ground.getType();

                if (groundType == Material.GRASS || groundType == Material.DIRT) {
                    boolean success = world.generateTree(
                            world.getBlockAt(x, y, z).getLocation(),
                            TreeType.DARK_OAK
                    );

                    if (success) {
                        break;
                    }
                }
            }
        }

        for (int gridX = 0; gridX < 4; gridX++) {
            for (int gridZ = 0; gridZ < 4; gridZ++) {
                if (random.nextInt(10) < 4) {
                    int x = chunkX * 16 + gridX * 4 + random.nextInt(4);
                    int z = chunkZ * 16 + gridZ * 4 + random.nextInt(4);

                    int y = world.getHighestBlockYAt(x, z);
                    Block ground = world.getBlockAt(x, y - 1, z);

                    if (ground.getType() == Material.GRASS || ground.getType() == Material.DIRT) {
                        world.generateTree(
                                world.getBlockAt(x, y, z).getLocation(),
                                TreeType.DARK_OAK
                        );
                    }
                }
            }
        }
    }
}