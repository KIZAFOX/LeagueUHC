package fr.kiza.leagueuhc.core.api.gadget;

import fr.kiza.leagueuhc.LeagueUHC;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RainbowWalk {

    private static final Map<Location, BlockData> changedBlocks = new HashMap<>();
    private static final Random random = new Random();
    private static final int DELAY = 40;

    public static void init(final PlayerMoveEvent event) {
        final Player player = event.getPlayer();

        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        final Block blockUnder = player.getLocation().subtract(0, 1, 0).getBlock();

        if (blockUnder.getType() == Material.AIR) return;

        final Location location = blockUnder.getLocation();

        if (changedBlocks.containsKey(location)) return;

        final BlockData originalBlock = new BlockData(blockUnder.getType(), blockUnder.getData());
        changedBlocks.put(location, originalBlock);

        byte glassColor = (byte) random.nextInt(16);
        blockUnder.setType(Material.STAINED_GLASS);
        blockUnder.setData(glassColor);

        new BukkitRunnable() {
            /**
             * When an object implementing interface <code>Runnable</code> is used
             * to create a thread, starting the thread causes the object's
             * <code>run</code> method to be called in that separately executing
             * thread.
             * <p>
             * The general contract of the method <code>run</code> is that it may
             * take any action whatsoever.
             *
             * @see Thread#run()
             */
            @Override
            public void run() {
                if (changedBlocks.containsKey(location)) {
                    final BlockData data = changedBlocks.get(location);
                    blockUnder.setType(data.material);
                    blockUnder.setData(data.data);
                    changedBlocks.remove(location);
                }
            }
        }.runTaskLater(LeagueUHC.getInstance(), DELAY);
    }

    public static void onDisable() {
        for (Map.Entry<Location, BlockData> entries : changedBlocks.entrySet()) {
            final Location location = entries.getKey();
            final BlockData blockData = entries.getValue();
            location.getBlock().setType(blockData.material);
            location.getBlock().setData(blockData.data);
        }
        changedBlocks.clear();
    }

    static class BlockData {

        Material material;
        byte data;

        BlockData(Material material, byte data) {
            this.material = material;
            this.data = data;
        }
    }
}
