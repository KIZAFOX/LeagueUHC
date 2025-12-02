package fr.kiza.leagueuhc.managers.listeners;

import fr.kiza.leagueuhc.LeagueUHC;

import fr.kiza.leagueuhc.core.api.gadget.RainbowWalk;

import fr.kiza.leagueuhc.core.game.event.PvPEvent;
import fr.kiza.leagueuhc.core.game.event.bus.GameEventBus;
import fr.kiza.leagueuhc.core.game.event.MovementFreezeEvent;
import fr.kiza.leagueuhc.core.game.gui.settings.SettingsGUI;
import fr.kiza.leagueuhc.core.game.mechanics.GameMechanicsManager;
import fr.kiza.leagueuhc.core.game.state.GameState;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class GlobalListeners implements Listener {

    protected final LeagueUHC instance;

    private boolean movementFrozen = false;
    private final Map<UUID, Location> frozenLocations = new HashMap<>();

    public GlobalListeners(LeagueUHC instance) {
        this.instance = instance;
        this.instance.getServer().getPluginManager().registerEvents(this, instance);

        GameEventBus.getInstance().subscribe(MovementFreezeEvent.class, event -> {
            this.movementFrozen = event.isFrozen();

            if (event.isFrozen()) {
                Bukkit.getOnlinePlayers().forEach(players -> this.frozenLocations.put(players.getUniqueId(), players.getLocation().clone()));
            } else {
                this.frozenLocations.clear();
            }
        });
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onLogin(final PlayerJoinEvent event) {
        event.setJoinMessage(null);

        final Player player = event.getPlayer();

        player.setGameMode(GameMode.ADVENTURE);
        player.setFoodLevel(20);
        player.setWalkSpeed(.20F);
        player.setFlySpeed(.15F);
        player.setAllowFlight(false);
        player.setExp(0);
        player.setLevel(0);
        player.setMaxHealth(20.0D);
        player.setHealth(player.getMaxHealth());

        player.teleport(new Location(Bukkit.getWorlds().get(0), 0, 100, 0));
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onLogout(final PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final ItemStack itemStack = event.getItem();

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (itemStack == null || itemStack.getType() != Material.REDSTONE_TORCH_ON) return;
        if (!itemStack.getItemMeta().getDisplayName().equals(ChatColor.RED + "" + ChatColor.BOLD + "Settings")) return;
        if (!player.isOp()) return;

        event.setCancelled(true);
        new SettingsGUI(this.instance, player);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onBlockBreak(final BlockBreakEvent event) {
        event.setCancelled(!this.isPlaying());
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onBlockPlace(final BlockPlaceEvent event) {
        event.setCancelled(!this.isPlaying());
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onFoodChange(final FoodLevelChangeEvent event) {
        event.setCancelled(!this.isPlaying());
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onDrop(final PlayerDropItemEvent event) {
        event.setCancelled(!this.isPlaying());
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onWeatherChange(final WeatherChangeEvent event) {
        if (event.toWeatherState()) {
            event.setCancelled(true);
            event.getWorld().setStorm(false);
            event.getWorld().setWeatherDuration(Integer.MAX_VALUE);
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onThunderChange(final ThunderChangeEvent event) {
        if (event.toThunderState()) {
            event.setCancelled(true);
            event.getWorld().setStorm(false);
            event.getWorld().setWeatherDuration(Integer.MAX_VALUE);
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onMove(final PlayerMoveEvent event) {
        if (!this.isPlaying()) RainbowWalk.init(event);

        if (this.movementFrozen) {
            final Player player = event.getPlayer();
             Location location = this.frozenLocations.get(player.getUniqueId());

            if (location == null) {
                location = player.getLocation().clone();
                this.frozenLocations.put(player.getUniqueId(), location);
            }

            final Location
                    from = event.getFrom(),
                    to = event.getTo();

            if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
                final Location newLoc = location.clone();
                newLoc.setYaw(to.getYaw());
                newLoc.setPitch(to.getPitch());
                event.setTo(newLoc);
            }
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerAttack(final EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;

        final Player player = (Player) event.getDamager();
        final Player target = (Player) event.getEntity();

        if (!PvPEvent.PvPHandler.getInstance().isEnabled()) event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerDeath(final PlayerDeathEvent event) {
        final Player player = event.getEntity();
        final Player killer = player.getKiller();

        final Location deathLocation = player.getLocation().clone();
        final List<ItemStack> items = new ArrayList<>(event.getDrops());

        event.getDrops().clear();

        if (killer != null) {
            event.setDeathMessage(ChatColor.RED + "☠ " + ChatColor.YELLOW + player.getName() +
                    ChatColor.GRAY + " a été tué par " +
                    ChatColor.YELLOW + killer.getName());
        } else {
            event.setDeathMessage(ChatColor.RED + "☠ " + ChatColor.YELLOW + player.getName() +
                    ChatColor.GRAY + " est mort");
        }

        Bukkit.getScheduler().runTaskLater(this.instance, () -> {
            if (deathLocation.getBlock().getType() == Material.AIR) {
                deathLocation.getBlock().setType(Material.CHEST);

                if (deathLocation.getBlock().getState() instanceof Chest) {
                    final Chest chest = (Chest) deathLocation.getBlock().getState();
                    final Inventory chestInventory = chest.getInventory();

                    for (ItemStack item : items) {
                        if (item != null && item.getType() != Material.AIR) chestInventory.addItem(item);
                    }

                    chest.update();
                }
            }
        }, 20L * 3L);

    }

    private boolean isPlaying() {
        return this.instance.getGameEngine().getCurrentState().equals(GameState.PLAYING.getName());
    }
}
