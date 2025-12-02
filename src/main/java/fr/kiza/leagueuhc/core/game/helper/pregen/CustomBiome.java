package fr.kiza.leagueuhc.core.game.helper.pregen;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.List;
import java.util.Random;

public class CustomBiome extends ChunkGenerator {

    private final int flatRadius = 50; // Rayon de la zone plate (50 = 100x100 blocs au centre)

    @Override
    public byte[] generate(World world, Random random, int chunkX, int chunkZ) {
        byte[] result = new byte[32768]; // Always create the array

        // Calculer la distance du chunk par rapport au centre
        int centerBlockX = chunkX * 16 + 8;
        int centerBlockZ = chunkZ * 16 + 8;
        double distanceFromCenter = Math.sqrt(centerBlockX * centerBlockX + centerBlockZ * centerBlockZ);

        // Si on est dans la zone centrale plate UNIQUEMENT
        if (distanceFromCenter <= flatRadius) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    // Bedrock (y=0)
                    setBlock(result, x, 0, z, (byte) Material.BEDROCK.getId());

                    // Pierre (y=1 à y=59)
                    for (int y = 1; y < 60; y++) {
                        setBlock(result, x, y, z, (byte) Material.STONE.getId());
                    }

                    // Terre (y=60 à y=62)
                    for (int y = 60; y < 63; y++) {
                        setBlock(result, x, y, z, (byte) Material.DIRT.getId());
                    }

                    // Herbe (y=63)
                    setBlock(result, x, 63, z, (byte) Material.GRASS.getId());
                }
            }
        }
        // EN DEHORS : retourner un tableau vide, pas null
        // Minecraft générera le terrain vanilla via les populators

        return result;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return java.util.Arrays.asList(new DarkOakForestPopulator());
    }

    private void setBlock(byte[] result, int x, int y, int z, byte blkid) {
        if (result.length > 0) {
            result[(x * 16 + z) * 128 + y] = blkid;
        }
    }

    @Override
    public boolean canSpawn(World world, int x, int z) {
        return true;
    }

    // Populator pour la forêt UNIQUEMENT au centre
    private class DarkOakForestPopulator extends BlockPopulator {
        @Override
        public void populate(World world, Random random, org.bukkit.Chunk chunk) {
            int chunkX = chunk.getX();
            int chunkZ = chunk.getZ();

            // Vérifier si on est dans la zone plate
            int centerBlockX = chunkX * 16 + 8;
            int centerBlockZ = chunkZ * 16 + 8;
            double distanceFromCenter = Math.sqrt(centerBlockX * centerBlockX + centerBlockZ * centerBlockZ);

            // Arbres UNIQUEMENT dans la zone plate
            if (distanceFromCenter > flatRadius) return;

            // Générer 2-4 arbres par chunk pour une forêt dense
            int treeCount = 2 + random.nextInt(3);

            for (int i = 0; i < treeCount; i++) {
                int x = random.nextInt(16);
                int z = random.nextInt(16);
                int y = 64;

                generateDarkOakTree(world, random, chunkX * 16 + x, y, chunkZ * 16 + z);
            }
        }

        private void generateDarkOakTree(World world, Random random, int x, int y, int z) {
            int height = 8 + random.nextInt(5);

            // Tronc 2x2 en dark oak
            for (int i = 0; i < height; i++) {
                world.getBlockAt(x, y + i, z).setTypeIdAndData(Material.LOG_2.getId(), (byte) 1, false);
                world.getBlockAt(x + 1, y + i, z).setTypeIdAndData(Material.LOG_2.getId(), (byte) 1, false);
                world.getBlockAt(x, y + i, z + 1).setTypeIdAndData(Material.LOG_2.getId(), (byte) 1, false);
                world.getBlockAt(x + 1, y + i, z + 1).setTypeIdAndData(Material.LOG_2.getId(), (byte) 1, false);
            }

            int leafStart = height - 3;

            // Feuillage large
            for (int dx = -2; dx <= 3; dx++) {
                for (int dz = -2; dz <= 3; dz++) {
                    for (int dy = leafStart; dy < leafStart + 2; dy++) {
                        if ((dx == 0 || dx == 1) && (dz == 0 || dz == 1)) continue;
                        if (Math.abs(dx) == 2 && Math.abs(dz) == 2 && random.nextBoolean()) continue;

                        world.getBlockAt(x + dx, y + dy, z + dz)
                                .setTypeIdAndData(Material.LEAVES_2.getId(), (byte) 1, false);
                    }
                }
            }

            // Feuillage haut
            for (int dx = -1; dx <= 2; dx++) {
                for (int dz = -1; dz <= 2; dz++) {
                    for (int dy = leafStart + 2; dy < height + 2; dy++) {
                        if ((dx == 0 || dx == 1) && (dz == 0 || dz == 1) && dy < height) continue;

                        world.getBlockAt(x + dx, y + dy, z + dz)
                                .setTypeIdAndData(Material.LEAVES_2.getId(), (byte) 1, false);
                    }
                }
            }

            // Sommet
            world.getBlockAt(x, y + height + 2, z).setTypeIdAndData(Material.LEAVES_2.getId(), (byte) 1, false);
            world.getBlockAt(x + 1, y + height + 2, z).setTypeIdAndData(Material.LEAVES_2.getId(), (byte) 1, false);
            world.getBlockAt(x, y + height + 2, z + 1).setTypeIdAndData(Material.LEAVES_2.getId(), (byte) 1, false);
            world.getBlockAt(x + 1, y + height + 2, z + 1).setTypeIdAndData(Material.LEAVES_2.getId(), (byte) 1, false);
        }
    }
}