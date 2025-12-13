package fr.kiza.leagueuhc.core.api.champion;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.api.champion.ability.Ability;
import fr.kiza.leagueuhc.core.api.champion.ability.AbilityContext;
import fr.kiza.leagueuhc.core.game.GamePlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gestionnaire centralisé pour tous les champions et leurs abilities.
 * Gère les cooldowns, les events, et le tick des abilities passives.
 */
public class ChampionManager implements Listener {

    private final LeagueUHC plugin;
    private final Map<String, Long> cooldowns = new HashMap<>();
    private BukkitTask tickTask;

    private static ChampionManager instance;

    public ChampionManager(LeagueUHC plugin) {
        this.plugin = plugin;
        instance = this;

        Bukkit.getPluginManager().registerEvents(this, plugin);
        startTickTask();

        plugin.getLogger().info("ChampionManager initialized");
    }

    public static ChampionManager getInstance() {
        return instance;
    }

    /**
     * Démarre la tâche de tick pour les champions et abilities passives.
     */
    private void startTickTask() {
        tickTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    GamePlayer gp = GamePlayer.get(player);
                    if (gp == null || gp.getChampion() == null) continue;

                    Champion champion = gp.getChampion();

                    // Tick du champion
                    try {
                        champion.onTick(gp);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error in champion tick for " + champion.getName() + ": " + e.getMessage());
                    }

                    // Tick des abilities (passives ET stateful)
                    for (Ability ability : champion.getAbilities()) {
                        try {
                            ability.onTick(gp);
                        } catch (Exception e) {
                            plugin.getLogger().warning("Error in ability tick for " + ability.getName() + ": " + e.getMessage());
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Arrête proprement le manager.
     */
    public void shutdown() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
        cooldowns.clear();
        plugin.getLogger().info("ChampionManager shutdown");
    }

    // ═══════════════════════════════════════════════════════════════
    // GESTION DES COOLDOWNS
    // ═══════════════════════════════════════════════════════════════

    private String cooldownKey(UUID uuid, String abilityName) {
        return uuid.toString() + ":" + abilityName;
    }

    public boolean isOnCooldown(UUID uuid, Ability ability) {
        Long expiry = cooldowns.get(cooldownKey(uuid, ability.getName()));
        return expiry != null && expiry > System.currentTimeMillis();
    }

    public long getRemainingCooldown(UUID uuid, Ability ability) {
        Long expiry = cooldowns.get(cooldownKey(uuid, ability.getName()));
        return expiry == null ? 0 : Math.max(0, expiry - System.currentTimeMillis());
    }

    public void setCooldown(UUID uuid, Ability ability) {
        if (ability.getCooldownSeconds() > 0) {
            cooldowns.put(
                    cooldownKey(uuid, ability.getName()),
                    System.currentTimeMillis() + ability.getCooldownSeconds() * 1000L
            );
        }
    }

    public void clearCooldown(UUID uuid, Ability ability) {
        cooldowns.remove(cooldownKey(uuid, ability.getName()));
    }

    public void clearAllCooldowns(UUID uuid) {
        cooldowns.entrySet().removeIf(entry -> entry.getKey().startsWith(uuid.toString()));
    }

    // ═══════════════════════════════════════════════════════════════
    // ASSIGNATION DES CHAMPIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Assigne un champion à un joueur.
     */
    public void assignChampion(GamePlayer gamePlayer, Champion champion) {
        // Révoquer l'ancien champion si présent
        if (gamePlayer.getChampion() != null) {
            revokeChampion(gamePlayer);
        }

        gamePlayer.setChampion(champion);

        // Activer les abilities
        for (Ability ability : champion.getAbilities()) {
            try {
                ability.onEnable(gamePlayer);
            } catch (Exception e) {
                plugin.getLogger().warning("Error enabling ability " + ability.getName() + ": " + e.getMessage());
            }
        }

        // Hook du champion
        try {
            champion.onAssign(gamePlayer);
        } catch (Exception e) {
            plugin.getLogger().warning("Error in onAssign for " + champion.getName() + ": " + e.getMessage());
        }

        // Message au joueur
        Player player = gamePlayer.getPlayer();
        player.sendMessage(ChatColor.GOLD + "══════════════════════════════════════");
        player.sendMessage(ChatColor.GREEN + "✔ Champion assigné: " + ChatColor.YELLOW + champion.getName());
        player.sendMessage(champion.getRegion().getColoredName() + ChatColor.GRAY + " • " + champion.getCategory().getColoredName());
        player.sendMessage(ChatColor.GRAY + champion.getDescription());
        player.sendMessage(ChatColor.GOLD + "══════════════════════════════════════");
    }

    /**
     * Retire le champion d'un joueur.
     */
    public void revokeChampion(GamePlayer gamePlayer) {
        Champion champion = gamePlayer.getChampion();
        if (champion == null) return;

        // Désactiver les abilities
        for (Ability ability : champion.getAbilities()) {
            try {
                ability.onDisable(gamePlayer);
            } catch (Exception e) {
                plugin.getLogger().warning("Error disabling ability " + ability.getName() + ": " + e.getMessage());
            }
        }

        // Hook du champion
        try {
            champion.onRevoke(gamePlayer);
        } catch (Exception e) {
            plugin.getLogger().warning("Error in onRevoke for " + champion.getName() + ": " + e.getMessage());
        }

        // Nettoyer les cooldowns
        clearAllCooldowns(gamePlayer.getUUID());

        gamePlayer.setChampion(null);
        gamePlayer.getPlayer().sendMessage(ChatColor.YELLOW + "Votre champion a été retiré.");
    }

    // ═══════════════════════════════════════════════════════════════
    // EVENT HANDLERS
    // ═══════════════════════════════════════════════════════════════

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        Ability.Trigger trigger = mapActionToTrigger(event.getAction(), event.getPlayer().isSneaking());
        if (trigger != null) {
            tryExecute(event.getPlayer(), trigger, new AbilityContext(event), event.getItem());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            tryExecute(event.getPlayer(), Ability.Trigger.SNEAK, new AbilityContext(event), null);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        tryExecute(event.getPlayer(), Ability.Trigger.BLOCK_PLACE, new AbilityContext(event), event.getItemInHand());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) return;
        Player shooter = (Player) event.getEntity().getShooter();
        tryExecute(shooter, Ability.Trigger.PROJECTILE_LAUNCH, new AbilityContext(event), null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        Player damager = extractDamager(event);
        if (damager == null) return;

        GamePlayer gp = GamePlayer.get(damager);
        if (gp == null || gp.getChampion() == null) return;

        try {
            gp.getChampion().onDamageDealt(gp, event.getEntity(), event.getFinalDamage());
        } catch (Exception e) {
            plugin.getLogger().warning("Error in onDamageDealt: " + e.getMessage());
        }

        // Vérifier aussi les dégâts reçus
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            GamePlayer victimGp = GamePlayer.get(victim);
            if (victimGp != null && victimGp.getChampion() != null) {
                try {
                    victimGp.getChampion().onDamageTaken(victimGp, event.getFinalDamage(), damager);
                } catch (Exception e) {
                    plugin.getLogger().warning("Error in onDamageTaken: " + e.getMessage());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        GamePlayer victimGp = GamePlayer.get(victim);
        if (victimGp != null && victimGp.getChampion() != null) {
            try {
                victimGp.getChampion().onDeath(victimGp, killer);
            } catch (Exception e) {
                plugin.getLogger().warning("Error in onDeath: " + e.getMessage());
            }
        }

        if (killer != null) {
            GamePlayer killerGp = GamePlayer.get(killer);
            if (killerGp != null && killerGp.getChampion() != null) {
                try {
                    killerGp.getChampion().onKill(killerGp, victim);
                } catch (Exception e) {
                    plugin.getLogger().warning("Error in onKill: " + e.getMessage());
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Nettoyer les cooldowns quand le joueur quitte
        clearAllCooldowns(event.getPlayer().getUniqueId());
    }

    // ═══════════════════════════════════════════════════════════════
    // LOGIQUE D'EXÉCUTION
    // ═══════════════════════════════════════════════════════════════

    private Ability.Trigger mapActionToTrigger(Action action, boolean sneaking) {
        switch (action) {
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                return sneaking ? Ability.Trigger.SNEAK_RIGHT_CLICK : Ability.Trigger.RIGHT_CLICK;
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
                return sneaking ? Ability.Trigger.SNEAK_LEFT_CLICK : Ability.Trigger.LEFT_CLICK;
            default:
                return null;
        }
    }

    private void tryExecute(Player player, Ability.Trigger trigger, AbilityContext ctx, ItemStack heldItem) {
        GamePlayer gp = GamePlayer.get(player);
        if (gp == null || gp.getChampion() == null) return;

        for (Ability ability : gp.getChampion().getAbilities()) {
            if (ability.getTrigger() != trigger) continue;
            if (!matchesItem(ability, heldItem)) continue;

            // Vérifier le cooldown
            if (isOnCooldown(player.getUniqueId(), ability)) {
                long remaining = getRemainingCooldown(player.getUniqueId(), ability);
                player.sendMessage(ChatColor.RED + "⏳ " + ability.getName() + " disponible dans " + ((remaining / 1000) + 1) + "s");
                continue;
            }

            // Exécuter l'ability
            try {
                ability.execute(gp, ctx);
                setCooldown(player.getUniqueId(), ability);
            } catch (Exception e) {
                plugin.getLogger().warning("Error executing ability " + ability.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean matchesItem(Ability ability, ItemStack held) {
        // Pas d'item requis = toujours OK
        if (ability.getItemMaterial() == null) {
            return true;
        }

        // Item requis mais pas d'item en main
        if (held == null || held.getType() != ability.getItemMaterial()) {
            return false;
        }

        // Si on ne requiert pas un item nommé, c'est OK
        if (!ability.requiresNamedItem()) {
            return true;
        }

        // Vérifier le nom
        return held.hasItemMeta()
                && held.getItemMeta().hasDisplayName()
                && held.getItemMeta().getDisplayName().contains(ability.getName());
    }

    private Player extractDamager(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            return (Player) event.getDamager();
        }
        if (event.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile) event.getDamager();
            if (proj.getShooter() instanceof Player) {
                return (Player) proj.getShooter();
            }
        }
        return null;
    }
}