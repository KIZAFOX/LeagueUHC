package fr.kiza.leagueuhc.gadget;

import fr.kiza.leagueuhc.packets.builder.ParticleBuilder;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gestionnaire de couronnes de particules pour les joueurs
 */
public class CrownManager {

    private final Plugin plugin;
    private final Map<UUID, BukkitRunnable> activeCrowns = new HashMap<>();

    public CrownManager(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Active une couronne pour un joueur
     */
    public void enableCrown(Player player, CrownType type) {
        // Désactiver la couronne existante si présente
        disableCrown(player);

        BukkitRunnable task = new BukkitRunnable() {
            double angle = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                Location headLoc = player.getLocation().add(0, 2.2, 0);
                type.display(headLoc, angle);

                angle += type.getSpeed();
                if (angle >= 360) angle = 0;
            }
        };

        task.runTaskTimer(plugin, 0L, type.getUpdateInterval());
        activeCrowns.put(player.getUniqueId(), task);
    }

    /**
     * Désactive la couronne d'un joueur
     */
    public void disableCrown(Player player) {
        BukkitRunnable task = activeCrowns.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * Vérifie si un joueur a une couronne active
     */
    public boolean hasCrown(Player player) {
        return activeCrowns.containsKey(player.getUniqueId());
    }

    /**
     * Désactive toutes les couronnes
     */
    public void disableAll() {
        activeCrowns.values().forEach(BukkitRunnable::cancel);
        activeCrowns.clear();
    }

    /**
     * Types de couronnes prédéfinis
     */
    public enum CrownType {
        // Couronne simple qui tourne
        SIMPLE(EnumParticle.VILLAGER_HAPPY, 12, 0.6, 10, 1L),

        // Couronne dorée avec flammes
        GOLDEN(EnumParticle.FLAME, 16, 0.7, 8, 1L),

        // Couronne de coeurs
        LOVE(EnumParticle.HEART, 10, 0.65, 15, 2L),

        // Couronne enchantée
        ENCHANTED(EnumParticle.ENCHANTMENT_TABLE, 20, 0.6, 5, 1L),

        // Couronne de redstone (rouge)
        REDSTONE(EnumParticle.REDSTONE, 15, 0.65, 8, 1L),

        // Couronne critique (étoiles)
        CRITICAL(EnumParticle.CRIT_MAGIC, 14, 0.7, 10, 1L),

        // Couronne de portail
        PORTAL(EnumParticle.PORTAL, 25, 0.6, 6, 1L),

        // Couronne de neige
        SNOW(EnumParticle.SNOW_SHOVEL, 18, 0.65, 12, 1L),

        // Couronne royale (multiples anneaux)
        ROYAL(EnumParticle.FIREWORKS_SPARK, 20, 0.7, 5, 1L) {
            @Override
            public void display(Location center, double angle) {
                // Premier anneau
                super.display(center, angle);
                // Deuxième anneau légèrement décalé
                super.display(center.clone().add(0, 0.15, 0), angle + 180);
            }
        },

        // Couronne de dragon (spirale avec smoke)
        DRAGON(EnumParticle.SMOKE_LARGE, 30, 0.7, 4, 1L) {
            @Override
            public void display(Location center, double angle) {
                for (int i = 0; i < particles; i++) {
                    double currentAngle = angle + (360.0 / particles * i);
                    double radians = Math.toRadians(currentAngle);

                    double x = Math.cos(radians) * radius;
                    double z = Math.sin(radians) * radius;
                    double y = Math.sin(radians * 2) * 0.2; // Effet de vague

                    Location particleLoc = center.clone().add(x, y, z);

                    ParticleBuilder.create()
                            .type(particle)
                            .location(particleLoc)
                            .count(1)
                            .offset(0f, 0f, 0f)
                            .speed(0f)
                            .sendInRadius(50);
                }
            }
        },

        // Couronne arc-en-ciel (note de musique)
        RAINBOW(EnumParticle.NOTE, 20, 0.65, 8, 1L),

        // Couronne de slime
        SLIME(EnumParticle.SLIME, 16, 0.6, 10, 2L),

        // Couronne de lave
        LAVA(EnumParticle.LAVA, 12, 0.7, 12, 2L);

        protected final EnumParticle particle;
        protected final int particles;
        protected final double radius;
        protected final double speed;
        protected final long updateInterval;

        CrownType(EnumParticle particle, int particles, double radius, double speed, long updateInterval) {
            this.particle = particle;
            this.particles = particles;
            this.radius = radius;
            this.speed = speed;
            this.updateInterval = updateInterval;
        }

        /**
         * Affiche la couronne à une position donnée
         */
        public void display(Location center, double angle) {
            for (int i = 0; i < particles; i++) {
                double currentAngle = angle + (360.0 / particles * i);
                double radians = Math.toRadians(currentAngle);

                double x = Math.cos(radians) * radius;
                double z = Math.sin(radians) * radius;

                Location particleLoc = center.clone().add(x, 0, z);

                ParticleBuilder.create()
                        .type(particle)
                        .location(particleLoc)
                        .count(1)
                        .offset(0f, 0f, 0f)
                        .speed(0f)
                        .sendInRadius(50);
            }
        }

        public double getSpeed() {
            return speed;
        }

        public long getUpdateInterval() {
            return updateInterval;
        }
    }
}