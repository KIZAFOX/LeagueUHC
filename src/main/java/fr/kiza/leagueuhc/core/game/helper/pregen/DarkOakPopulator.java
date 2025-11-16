package fr.kiza.leagueuhc.core.game.helper.pregen;

import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.TreeType;
import org.bukkit.Chunk;

import java.util.Random;

public class DarkOakPopulator extends BlockPopulator {

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        int startX = chunk.getX() << 4;
        int startZ = chunk.getZ() << 4;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = startX + x;
                int worldZ = startZ + z;

                // Centre 200x200
                if (Math.abs(worldX) <= 100 && Math.abs(worldZ) <= 100) {
                    if (random.nextInt(10) == 0) { // 10% de chance
                        int y = world.getHighestBlockYAt(worldX, worldZ);
                        world.generateTree(new org.bukkit.Location(world, worldX, y, worldZ), TreeType.DARK_OAK);
                    }
                }
            }
        }
    }
}
