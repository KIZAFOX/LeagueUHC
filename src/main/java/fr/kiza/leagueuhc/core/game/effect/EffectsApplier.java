package fr.kiza.leagueuhc.core.game.effect;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.game.context.GameContext;
import fr.kiza.leagueuhc.core.game.state.GameState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Applique et maintient les effets configurés sur les joueurs
 */
public class EffectsApplier implements Listener {

    private final LeagueUHC instance;
    private final GameContext context;
    private BukkitRunnable effectTask;

    public EffectsApplier(LeagueUHC instance, GameContext context) {
        this.instance = instance;
        this.context = context;
    }

    /**
     * Vérifie si on est en état PLAYING
     */
    private boolean isInGame() {
        String currentState = instance.getGameEngine().getCurrentState();
        return GameState.PLAYING.getName().equals(currentState);
    }

    /**
     * Démarre la task qui applique les effets en continu
     */
    public void startEffectTask() {
        if (effectTask != null) {
            effectTask.cancel();
        }

        effectTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Applique les effets uniquement si on est en jeu
                if (isInGame()) {
                    applyEffectsToAllPlayers();
                }
            }
        };

        // Applique les effets toutes les 5 secondes (100 ticks)
        effectTask.runTaskTimer(instance, 0L, 100L);
    }

    /**
     * Arrête la task d'application des effets
     */
    public void stopEffectTask() {
        if (effectTask != null) {
            effectTask.cancel();
            effectTask = null;
        }
    }

    /**
     * Applique les effets à tous les joueurs en ligne
     */
    public void applyEffectsToAllPlayers() {
        // Sécurité : n'applique que si on est en jeu
        if (!isInGame()) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            applyEffectsToPlayer(player);
        }
    }

    /**
     * Applique les effets configurés à un joueur
     */
    public void applyEffectsToPlayer(Player player) {
        // Sécurité : n'applique que si on est en jeu
        if (!isInGame()) {
            return;
        }

        for (EffectType effectType : EffectType.values()) {
            applyEffect(player, effectType);
        }
    }

    /**
     * Applique un effet spécifique selon la configuration
     */
    private void applyEffect(Player player, EffectType effectType) {
        int percentage = context.getEffectPercentage(effectType.getPotionType());

        if (!context.isEffectActive(effectType.getPotionType()) || percentage < EffectType.MIN_PERCENTAGE) {
            player.removePotionEffect(effectType.getPotionType());
            return;
        }

        int level = calculateEffectLevel(percentage);

        // Durée infinie (999999 ticks = ~13 heures)
        // ambient = false, particles = false (pas de particules)
        PotionEffect effect = new PotionEffect(
                effectType.getPotionType(),
                999999,
                level,
                false,
                false
        );
        player.addPotionEffect(effect, true);
    }

    /**
     * Calcule le niveau d'effet en fonction du pourcentage
     * 20% = niveau 0, 100% = niveau 4
     */
    private int calculateEffectLevel(int percentage) {
        if (percentage <= 20) return 0;
        if (percentage <= 40) return 1;
        if (percentage <= 60) return 2;
        if (percentage <= 80) return 3;
        return 4; // 100%
    }

    /**
     * Applique les effets à un joueur qui rejoint
     * UNIQUEMENT si la partie est en cours
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Ne pas appliquer les effets si on n'est pas en jeu
        if (!isInGame()) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                // Double vérification au cas où l'état a changé
                if (isInGame()) {
                    applyEffectsToPlayer(event.getPlayer());
                }
            }
        }.runTaskLater(instance, 20L);
    }

    /**
     * Réapplique les effets après un respawn
     * UNIQUEMENT si la partie est en cours
     */
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // Ne pas appliquer les effets si on n'est pas en jeu
        if (!isInGame()) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                // Double vérification au cas où l'état a changé
                if (isInGame()) {
                    applyEffectsToPlayer(event.getPlayer());
                }
            }
        }.runTaskLater(instance, 5L);
    }

    /**
     * Retire tous les effets d'un joueur
     */
    public void removeAllEffects(Player player) {
        for (EffectType effectType : EffectType.values()) {
            player.removePotionEffect(effectType.getPotionType());
        }
    }

    /**
     * Met à jour les effets de tous les joueurs (après changement config)
     * UNIQUEMENT si on est en jeu
     */
    public void refreshEffects() {
        // Ne rafraîchir que si on est en jeu
        if (!isInGame()) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            removeAllEffects(player);
            applyEffectsToPlayer(player);
        }
    }

    /**
     * Récupère le pourcentage actuel d'un effet
     */
    public int getCurrentPercentage(EffectType effectType) {
        return context.getEffectPercentage(effectType.getPotionType());
    }

    /**
     * Récupère le niveau actuel d'un effet
     */
    public int getCurrentLevel(EffectType effectType) {
        int percentage = getCurrentPercentage(effectType);
        return calculateEffectLevel(percentage);
    }
}