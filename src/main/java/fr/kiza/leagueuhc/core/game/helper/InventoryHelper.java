package fr.kiza.leagueuhc.core.game.helper;

import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryHelper {

    private final ItemStack[] contents;
    private final ItemStack[] armor;
    private final GameMode gameMode;

    private static final UUID START_INVENTORY_KEY = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final Map<UUID, InventoryHelper> SAVED_INVENTORY = new HashMap<>();

    public InventoryHelper(ItemStack[] contents, ItemStack[] armor, GameMode gameMode) {
        this.contents = contents;
        this.armor = armor;
        this.gameMode = gameMode;
    }

    public ItemStack[] getContents() {
        return contents;
    }

    public ItemStack[] getArmor() {
        return armor;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public static Map<UUID, InventoryHelper> getSavedInventory() {
        return SAVED_INVENTORY;
    }

    public static void setStartInventory(InventoryHelper inventory) {
        SAVED_INVENTORY.put(START_INVENTORY_KEY, inventory);
    }

    public static InventoryHelper getStartInventory() {
        return SAVED_INVENTORY.get(START_INVENTORY_KEY);
    }

    public static boolean hasStartInventory() {
        return SAVED_INVENTORY.containsKey(START_INVENTORY_KEY);
    }
}