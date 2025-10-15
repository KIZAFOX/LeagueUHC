package fr.kiza.leagueuhc.gui.core;

import org.bukkit.inventory.ItemStack;

public class GuiButton {

    private final ItemStack item;
    private final ButtonAction action;
    private final boolean cancelClick;

    public GuiButton(ItemStack item, ButtonAction action, boolean cancelClick) {
        this.item = item;
        this.action = action;
        this.cancelClick = cancelClick;
    }

    public GuiButton(ItemStack item, ButtonAction action) {
        this(item, action, true);
    }

    public ItemStack getItem() {
        return item;
    }

    public ButtonAction getAction() {
        return action;
    }

    public boolean shouldCancelClick() {
        return cancelClick;
    }
}
