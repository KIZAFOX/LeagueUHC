package fr.kiza.leagueuhc.core.api.gui.helper;

import fr.kiza.leagueuhc.core.api.gui.core.AbstractGui;
import fr.kiza.leagueuhc.core.api.gui.core.ButtonAction;
import fr.kiza.leagueuhc.core.api.gui.core.GuiButton;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class GuiBuilder {

    private final Plugin plugin;
    private String title = "Menu";
    private int size = 9;

    private final Map<Integer, GuiButton> buttons = new HashMap<>();

    public GuiBuilder(Plugin plugin){
        this.plugin = plugin;
    }

    public GuiBuilder title(String t){
        this.title = t; return this;
    }

    public GuiBuilder size(int s){
        this.size = s; return this;
    }

    public GuiBuilder button(int slot, ItemStack item, ButtonAction action){
        buttons.put(slot, new GuiButton(item, action)); return this;
    }

    public AbstractGui build(){
        return new AbstractGui(plugin) {
            {
                this.setTitle(GuiBuilder.this.title);
                this.setSize(GuiBuilder.this.size);

                for(Map.Entry<Integer, GuiButton> entry : GuiBuilder.this.buttons.entrySet()){
                    this.setButton(entry.getKey(), entry.getValue());
                }

                this.createInventory();
            }
        };
    }
}