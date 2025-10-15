package fr.kiza.leagueuhc.gui.example;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.gui.annotation.GuiInfo;
import fr.kiza.leagueuhc.gui.core.PaginateGui;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

@GuiInfo(title = "§6Example Paginated", size = 27)
public class ExampleGui extends PaginateGui {
    public ExampleGui(LeagueUHC instance) {
        super(instance);
        for (int i = 0; i < 40; i++) {
            ItemStack item = new ItemStack(org.bukkit.Material.STONE);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "Item #" + (i + 1));
            List<String> lore = new ArrayList<>(); lore.add(ChatColor.YELLOW + "This is sample item " + (i + 1));
            meta.setLore(lore);
            item.setItemMeta(meta);
            addElement(item);
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        super.handleClick(event);
        int slot = event.getRawSlot();
        int rows = size / 9;
        int start = 0;
        int end = (rows - 1) * 9 - 1;
        if(slot >= start && slot <= end){
            int indexInPage = slot - start;
            int globalIndex = page * ((end - start) + 1) + indexInPage;
            if(globalIndex >= 0 && globalIndex < elements.size()){
                final Player player = (Player) event.getWhoClicked();
                event.setCancelled(true);
                player.sendMessage(ChatColor.GREEN+"You clicked item #"+(globalIndex+1));
            }
        }
    }
}
