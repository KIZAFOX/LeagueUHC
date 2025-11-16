package fr.kiza.leagueuhc.core.api.gui.core;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

@FunctionalInterface
public interface ButtonAction {
    void execute(final Player player, final Inventory inventory);
}
