package fr.kiza.leagueuhc.core.api.gui.core;

import fr.kiza.leagueuhc.core.api.gui.annotation.GuiInfo;
import fr.kiza.leagueuhc.core.api.gui.manager.GuiManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractGui {

    protected final Plugin plugin;
    protected final UUID viewerId;

    protected Inventory inventory;
    protected String title;
    protected int size;

    protected Map<Integer, GuiButton> buttons = new HashMap<>();

    public AbstractGui(Plugin plugin, UUID viewerId) {
        this.plugin = plugin;
        this.viewerId = viewerId;

        final GuiInfo info = this.getClass().getAnnotation(GuiInfo.class);
        this.title = (info != null) ?  info.title() : "Menu";
        this.size = (info != null) ? this.normalizeSize(info.size()) : 9;
    }

    public AbstractGui(Plugin plugin) {
        this(plugin, null);
    }

    public void handleClick(final InventoryClickEvent event) {
        final int slot = event.getRawSlot();
        final GuiButton button = this.buttons.get(slot);

        if (button == null) return;
        if (button.shouldCancelClick()) event.setCancelled(true);

        final HumanEntity human = event.getWhoClicked();

        if(human instanceof Player) {
            try {
                button.getAction().execute((Player) human, inventory);
            } catch (final  Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void handleClose(final InventoryCloseEvent event) {
        final Player player = (Player) event.getPlayer();

        GuiManager.untrackOpenInventory(player.getUniqueId());
        this.close(player);
    }

    public void open(final Player player) {
        if (this.inventory == null) this.createInventory();
        player.openInventory(this.inventory);
        GuiManager.trackOpenInventory(player, this);
    }

    public void close(final Player player) {
        if (this.inventory == null) return;
        player.closeInventory();
    }

    protected void createInventory() {
        this.inventory = Bukkit.createInventory(null, size, title);
        this.refreshInventory();
    }

    private void refreshInventory() {
        if (this.inventory == null) return;
        this.inventory.clear();
        for (Map.Entry<Integer, GuiButton> entries : this.buttons.entrySet()) this.inventory.setItem(entries.getKey(), entries.getValue().getItem());
    }

    private int normalizeSize(int size) {
        if (size < 9) size = 9;
        if (size > 54)  size = 54;
        if (size % 9 != 0) size = ((size / 9) + 1) * 9;
        return size;
    }

    public UUID getViewerId() {
        return viewerId;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Map<Integer, GuiButton> getButtons() {
        return buttons;
    }

    public void setButtons(Map<Integer, GuiButton> buttons) {
        this.buttons = buttons;
    }

    public void setButton(int slot, GuiButton button) {
        this.buttons.put(slot, button);
        if (this.inventory != null) this.inventory.setItem(slot, button.getItem());
    }

    public void removeButton(int slot) {
        this.buttons.remove(slot);
        if (this.inventory!=null) this.inventory.setItem(slot, null);
    }
}
