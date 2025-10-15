package fr.kiza.leagueuhc.champion.ability;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.game.GamePlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class AbilityTriggerManager implements Listener {

    protected final LeagueUHC instance;

    public AbilityTriggerManager(LeagueUHC instance) {
        this.instance = instance;
        Bukkit.getPluginManager().registerEvents(this, instance);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(final PlayerInteractEvent event) {
        if (event.getItem() == null) return;

        final Player player = event.getPlayer();
        final GamePlayer gamePlayer = GamePlayer.get(player);

        if (gamePlayer == null || gamePlayer.getChampion() == null) return;

        ChampionAbility.TriggerType type = null;

        switch (event.getAction()) {
            case RIGHT_CLICK_AIR : case RIGHT_CLICK_BLOCK : {
                type = ChampionAbility.TriggerType.RIGHT_CLICK_ITEM;
                break;
            }
            case LEFT_CLICK_AIR : case LEFT_CLICK_BLOCK : {
                type = ChampionAbility.TriggerType.LEFT_CLICK_ITEM;
                break;
            }
		default:
			break;
        }

        if (type == null) return;

        for (ChampionAbility ability : gamePlayer.getChampion().getAbilities()) {
            if (ability.getTriggerType() != type) continue;

            if (AbilityCooldown.isOnCooldown(player.getUniqueId(), ability.getName())) {
                long remain = (AbilityCooldown.getRemaining(player.getUniqueId(), ability.getName()) + 999) / 1000;
                player.sendMessage(ChatColor.RED + "⏳ " + ability.getName() + " disponible dans " + remain + "s.");
                continue;
            }

            final ItemStack held = event.getItem();
            final ItemStack requiredItem = ability.getItem();
            final String requiredName = ability.getName();

            if (requiredItem != null && held.getType() != requiredItem.getType()) continue;
            if (requiredName != null) {
                if (!held.hasItemMeta() || !held.getItemMeta().hasDisplayName() || !held.getItemMeta().getDisplayName().contains(requiredName)) continue;
            }

            ability.execute(gamePlayer, new AbilityContext(event));
            AbilityCooldown.setCooldown(player.getUniqueId(), ability.getName(), ability.getCooldownTicks());
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onBlockPlace(final BlockPlaceEvent event) {
        this.execute(event.getPlayer(), ChampionAbility.TriggerType.BLOCK_PLACE, new AbilityContext(event));
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onProjectileLaunch(final ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) return;
        this.execute((Player) event.getEntity().getShooter(), ChampionAbility.TriggerType.PROJECTILE_LAUNCH, new AbilityContext(event));
    }

    private void execute(final Player player, final ChampionAbility.TriggerType type, final AbilityContext context) {
        GamePlayer gamePlayer = GamePlayer.get(player);
        if(gamePlayer == null || gamePlayer.getChampion() == null) return;

        final List<ChampionAbility> abilities = gamePlayer.getChampion().getAbilities();

        for (final ChampionAbility ability : abilities) {
            if (ability.getTriggerType() != type) continue;

            if (AbilityCooldown.isOnCooldown(player.getUniqueId(), ability.getName())) {
                long remain = (AbilityCooldown.getRemaining(player.getUniqueId(), ability.getName()) + 999) / 1000;
                player.sendMessage(ChatColor.RED + "⏳ " + ability.getName() + " disponible dans " + remain + "s.");
                return;
            }

            ability.execute(gamePlayer, context == null ? AbilityContext.EMPTY : context);
            AbilityCooldown.setCooldown(player.getUniqueId(), ability.getName(), ability.getCooldownTicks());
            return;
        }
    }
}
