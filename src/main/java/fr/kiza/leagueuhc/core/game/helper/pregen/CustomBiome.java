package fr.kiza.leagueuhc.core.game.helper.pregen;

import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomBiome extends ChunkGenerator {

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData chunk = createChunkData(world);

        int startX = chunkX << 4;
        int startZ = chunkZ << 4;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = startX + x;
                int worldZ = startZ + z;

                // Centre 200x200 = -100 à +100
                if (Math.abs(worldX) <= 100 && Math.abs(worldZ) <= 100) {
                    biome.setBiome(x, z, Biome.ROOFED_FOREST);

                    // Génération simple du sol
                    chunk.setBlock(x, 64, z, Material.GRASS);
                    chunk.setBlock(x, 63, z, Material.DIRT);
                    chunk.setBlock(x, 62, z, Material.DIRT);
                } else {
                    biome.setBiome(x, z, Biome.PLAINS);
                    chunk.setBlock(x, 64, z, Material.GRASS);
                    chunk.setBlock(x, 63, z, Material.DIRT);
                    chunk.setBlock(x, 62, z, Material.DIRT);
                }
            }
        }

        return chunk;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        List<BlockPopulator> populators = new ArrayList<>();
        populators.add(new DarkOakPopulator());
        return populators;
    }
}
