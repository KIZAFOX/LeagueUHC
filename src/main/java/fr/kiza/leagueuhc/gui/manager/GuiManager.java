package fr.kiza.leagueuhc.gui.manager;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.gui.annotation.GuiInfo;
import fr.kiza.leagueuhc.gui.core.AbstractGui;
import fr.kiza.leagueuhc.utils.ClassScanner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class GuiManager implements Listener {

    protected static LeagueUHC instance = LeagueUHC.getInstance();

    private static final Map<UUID, AbstractGui> openMenus = new ConcurrentHashMap<>();
    private static final Map<String, AbstractGui> registeredMenus = new ConcurrentHashMap<>();

    public static void initialize(String basePackage) {
        instance.getServer().getPluginManager().registerEvents(new GuiManager(), instance);

        try {
            for (Class<?> clazz : ClassScanner.findClasses(basePackage)) {
                if (AbstractGui.class.isAssignableFrom(clazz)
                        && clazz.isAnnotationPresent(GuiInfo.class)
                        && !Modifier.isAbstract(clazz.getModifiers())) {

                    AbstractGui gui = (AbstractGui) clazz.getConstructor(LeagueUHC.class).newInstance(instance);
                    registerMenu(gui);
                    Bukkit.getLogger().info("✅ Registered GUI: " + gui.getTitle());
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "❌ Error while loading GUIs", e);
        }
    }

    public static void registerMenu(AbstractGui menu) {
        if (menu == null) return;
        registeredMenus.put(menu.getClass().getSimpleName(), menu);
    }

    public static AbstractGui getRegisteredMenu(String exampleMenu) {
        return registeredMenus.get(exampleMenu);
    }

    public static void trackOpenInventory(Player p, AbstractGui menu) {
        openMenus.put(p.getUniqueId(), menu);
    }

    public static void untrackOpenInventory(UUID playerId) {
        openMenus.remove(playerId);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(final InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            final Player player = (Player) event.getWhoClicked();
            final AbstractGui menu = openMenus.get(player.getUniqueId());

            if (menu != null && event.getInventory().equals(menu.getInventory())) {
                try {
                    menu.handleClick(event);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(final InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            final Player player = (Player) event.getPlayer();
            final AbstractGui menu = openMenus.remove(player.getUniqueId());

            if (menu != null) {
                try {
                    menu.handleClose(event);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
