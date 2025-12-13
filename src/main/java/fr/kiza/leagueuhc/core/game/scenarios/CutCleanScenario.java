package fr.kiza.leagueuhc.core.game.scenarios;

import fr.kiza.leagueuhc.core.api.scenario.Scenario;
import fr.kiza.leagueuhc.core.api.scenario.ScenarioEntry;
import fr.kiza.leagueuhc.core.api.scenario.ScenarioType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.Map;

@ScenarioEntry
public class CutCleanScenario implements Scenario {

    private static final Map<Material, ItemStack> ORE_RESULTS = new EnumMap<>(Material.class);
    private static final Map<Material, Integer> ORE_XP = new EnumMap<>(Material.class);

    static {
        // Résultats des minerais
        ORE_RESULTS.put(Material.COAL_ORE, new ItemStack(Material.COAL, 1));
        ORE_RESULTS.put(Material.IRON_ORE, new ItemStack(Material.IRON_INGOT, 1));
        ORE_RESULTS.put(Material.GOLD_ORE, new ItemStack(Material.GOLD_INGOT, 1));
        ORE_RESULTS.put(Material.DIAMOND_ORE, new ItemStack(Material.DIAMOND, 1));
        ORE_RESULTS.put(Material.EMERALD_ORE, new ItemStack(Material.EMERALD, 1));
        ORE_RESULTS.put(Material.REDSTONE_ORE, new ItemStack(Material.REDSTONE, 4));
        ORE_RESULTS.put(Material.LAPIS_ORE, new ItemStack(Material.INK_SACK, 6, (short) 4)); // Lapis dye = 4
        ORE_RESULTS.put(Material.QUARTZ_ORE, new ItemStack(Material.QUARTZ, 1));

        // XP par minerai
        ORE_XP.put(Material.COAL_ORE, 0);
        ORE_XP.put(Material.IRON_ORE, 1);
        ORE_XP.put(Material.GOLD_ORE, 1);
        ORE_XP.put(Material.DIAMOND_ORE, 5);
        ORE_XP.put(Material.EMERALD_ORE, 5);
        ORE_XP.put(Material.REDSTONE_ORE, 2);
        ORE_XP.put(Material.LAPIS_ORE, 3);
        ORE_XP.put(Material.QUARTZ_ORE, 3);
    }

    @Override
    public ScenarioType getType() {
        return ScenarioType.CUTCLEAN;
    }

    @Override
    public Material getIcon() {
        return Material.FURNACE;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Material type = block.getType();

        if (!ORE_RESULTS.containsKey(type)) {
            return;
        }

        event.setCancelled(true);
        block.setType(Material.AIR);

        Player player = event.getPlayer();

        // Drop l'item cuit
        ItemStack result = ORE_RESULTS.get(type);
        if (result != null) {
            ItemStack drop = result.clone();
            player.getInventory().addItem(drop);
        }

        // Spawn XP
        int xp = ORE_XP.getOrDefault(type, 0);
        if (xp > 0) {
            ExperienceOrb orb = block.getWorld().spawn(
                    block.getLocation().add(0.5, 0.5, 0.5),
                    ExperienceOrb.class
            );
            orb.setExperience(xp);
        }

        // Durabilité de l'outil
        this.applyToolDurability(player);
    }

    private void applyToolDurability(Player player) {
        ItemStack hand = player.getItemInHand();
        if (hand == null || hand.getType() == Material.AIR) {
            return;
        }

        short maxDurability = hand.getType().getMaxDurability();
        if (maxDurability <= 0) {
            return;
        }

        short currentDurability = hand.getDurability();
        currentDurability++;

        if (currentDurability >= maxDurability) {
            player.setItemInHand(null);
            player.playSound(player.getLocation(), org.bukkit.Sound.ITEM_BREAK, 1.0f, 1.0f);
        } else {
            hand.setDurability(currentDurability);
        }
    }
}