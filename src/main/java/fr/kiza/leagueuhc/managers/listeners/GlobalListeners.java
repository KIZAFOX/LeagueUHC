package fr.kiza.leagueuhc.managers.listeners;

import fr.kiza.leagueuhc.LeagueUHC;

import fr.kiza.leagueuhc.core.api.gadget.RainbowWalk;

import fr.kiza.leagueuhc.core.game.cycle.DayCycleManager;
import fr.kiza.leagueuhc.core.game.event.bus.GameEventBus;
import fr.kiza.leagueuhc.core.game.event.MovementFreezeEvent;
import fr.kiza.leagueuhc.core.game.gui.settings.SettingsGUI;
import fr.kiza.leagueuhc.core.game.host.HostManager;
import fr.kiza.leagueuhc.core.game.state.GameState;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class GlobalListeners implements Listener {

    protected final LeagueUHC instance;

    private boolean movementFrozen = false;
    private final Map<UUID, Location> frozenLocations = new HashMap<>();

    private final World uhcWorld = Bukkit.getWorld("uhc_world");

    public GlobalListeners(LeagueUHC instance) {
        this.instance = instance;
        this.instance.getServer().getPluginManager().registerEvents(this, instance);

        if (uhcWorld != null) DayCycleManager.forceDay(uhcWorld);

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
        final boolean wasPending = HostManager.isPendingHost(player.getName());

        HostManager.onPlayerJoin(player);

        if (wasPending) {
            player.sendMessage(ChatColor.GREEN + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            player.sendMessage(ChatColor.GOLD + "  ⚔ " + ChatColor.BOLD + "HOST DÉSIGNÉ" + ChatColor.GOLD + " ⚔");
            player.sendMessage("");
            player.sendMessage(ChatColor.GRAY + "  Vous avez été désigné comme host !");
            player.sendMessage(ChatColor.GRAY + "  Vous pouvez maintenant gérer la partie.");
            player.sendMessage(ChatColor.GREEN + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            Bukkit.broadcastMessage(ChatColor.GOLD + "[UHC] " + ChatColor.YELLOW + player.getName() + ChatColor.GOLD + " est maintenant host de la partie !");
        } else if (HostManager.isHost(player)) {
            player.sendMessage(ChatColor.GREEN + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            player.sendMessage(ChatColor.GOLD + "  ⚔ " + ChatColor.BOLD + "HOST" + ChatColor.GOLD + " ⚔");
            player.sendMessage("");
            player.sendMessage(ChatColor.GRAY + "  Vous êtes host de cette partie !");
            player.sendMessage(ChatColor.GRAY + "  Utilisez la torche pour ouvrir les settings.");
            player.sendMessage(ChatColor.GREEN + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onLogout(final PlayerQuitEvent event) { event.setQuitMessage(null); }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final ItemStack itemStack = event.getItem();

        if (itemStack == null) return;

        if (isPlaying()) {
            if (itemStack.getType() == Material.ENDER_PEARL) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "✘ Les ender pearls sont désactivées !");
                player.updateInventory();
                return;
            }

            if (itemStack.getType() == Material.LAVA_BUCKET) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "✘ Le seau de lave est désactivé !");
                player.updateInventory();
            }
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (itemStack == null || itemStack.getType() != Material.REDSTONE_TORCH_ON) return;
        if (!itemStack.getItemMeta().getDisplayName().equals(ChatColor.RED + "" + ChatColor.BOLD + "Settings")) return;
        if (!HostManager.isHost(player)) return;

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
    public void onClick(final InventoryClickEvent event) {
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

    @EventHandler (priority = EventPriority.HIGH)
    public void onCraft(final CraftItemEvent event) {
        if (event.getRecipe().getResult().getType() == Material.FISHING_ROD) {
            event.setCancelled(true);
            event.getInventory().setResult(null);

            if (event.getWhoClicked() instanceof Player) {
                event.getWhoClicked().sendMessage(ChatColor.RED + "✘ Le craft de la canne à pêche est désactivé !");
            }
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onMove(final PlayerMoveEvent event) {
        if (!this.isPlaying()) RainbowWalk.init(event);

        if (this.movementFrozen) {
            final Player player = event.getPlayer();
            Location frozenLocation = this.frozenLocations.get(player.getUniqueId());

            if (frozenLocation == null) {
                frozenLocation = player.getLocation().clone();
                this.frozenLocations.put(player.getUniqueId(), frozenLocation);
            }

            if (!player.getWorld().getName().equals(frozenLocation.getWorld().getName())) {
                frozenLocation = player.getLocation().clone();
                this.frozenLocations.put(player.getUniqueId(), frozenLocation);
                return;
            }

            final Location from = event.getFrom();
            final Location to = event.getTo();

            if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
                final Location newLoc = frozenLocation.clone();
                newLoc.setYaw(to.getYaw());
                newLoc.setPitch(to.getPitch());
                event.setTo(newLoc);
            }
        }
    }

    @EventHandler (priority =  EventPriority.MONITOR)
    public void onWorldLoad(final WorldLoadEvent event) {
        DayCycleManager.forceDay(uhcWorld);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onFireSpread(BlockSpreadEvent event) {
        if (event.getSource().getType() == Material.FIRE) {
            event.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onBlockIgnite(final BlockIgniteEvent event) {
        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        EntityType type = event.getEntityType();

        switch (type) {
            case ZOMBIE:
            case SKELETON:
            case SPIDER:
            case CAVE_SPIDER:
            case CREEPER:
            case ENDERMAN:
            case WITCH:
            case SLIME:
            case SILVERFISH:
            case ENDERMITE:
            case GUARDIAN:
            case BLAZE:
            case GHAST:
            case MAGMA_CUBE:
            case PIG_ZOMBIE:
                event.setCancelled(true);
                break;
            default:
                break;
        }
    }

    private boolean isPlaying() {
        return this.instance.getGameEngine().getCurrentState().equals(GameState.PLAYING.getName());
    }
}