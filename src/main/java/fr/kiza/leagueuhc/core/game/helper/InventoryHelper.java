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

    public static final Map<UUID, InventoryHelper> SAVED_INVENTORY = new HashMap<>();
    public static InventoryHelper START_INVENTORY = null;

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
}