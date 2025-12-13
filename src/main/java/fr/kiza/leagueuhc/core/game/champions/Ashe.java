package fr.kiza.leagueuhc.core.game.champions;

import fr.kiza.leagueuhc.core.api.champion.Champion;
import fr.kiza.leagueuhc.core.api.champion.ability.Ability;
import fr.kiza.leagueuhc.core.api.champion.ability.AbilityContext;
import fr.kiza.leagueuhc.core.api.champion.annotations.ChampionEntry;
import fr.kiza.leagueuhc.core.game.GamePlayer;
import fr.kiza.leagueuhc.utils.ItemBuilder;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

/**
 * Champion Ashe - La Archère de Givre
 * Région: Freljord
 *
 * Abilities:
 * - Active: Flèche de Givre - Tire une flèche qui gèle la zone à l'impact
 */
@ChampionEntry
public class Ashe extends Champion {

    public Ashe() {
        super(
                "Ashe",
                "Archère royale maîtrisant la glace",
                Category.MARKSMAN,
                Region.FRELJORD,
                Material.ARROW
        );

        registerAbility(new FrostArrowAbility());
    }

    @Override
    public void onAssign(GamePlayer player) {
        getAbility("Flèche de Givre").ifPresent(ability -> {
            ItemStack item = new ItemBuilder(ability.getItemMaterial())
                    .setName(ChatColor.AQUA + ability.getName())
                    .setLore(
                            ChatColor.GRAY + ability.getDescription(),
                            "",
                            ChatColor.YELLOW + "Cooldown: " + ability.getCooldownSeconds() + "s",
                            ChatColor.AQUA + "Clic droit pour tirer"
                    )
                    .toItemStack();

            player.getPlayer().getInventory().addItem(item);
            player.getPlayer().updateInventory();
        });
    }

    private static class FrostArrowAbility extends Ability {

        private static final int FREEZE_RADIUS = 5;
        private static final int FREEZE_HEIGHT = 3;
        private static final int FREEZE_DURATION_SECONDS = 4;

        @Override
        public String getName() {
            return "Flèche de Givre";
        }

        @Override
        public String getDescription() {
            return "Tire une flèche qui gèle la zone à l'impact.";
        }

        @Override
        public Trigger getTrigger() {
            return Trigger.RIGHT_CLICK;
        }

        @Override
        public int getCooldownSeconds() {
            return 5;
        }

        @Override
        public Material getItemMaterial() {
            return Material.BONE;
        }

        @Override
        public boolean requiresNamedItem() {
            return true;
        }

        @Override
        public void execute(GamePlayer caster, AbilityContext ctx) {
            ctx.asInteract().ifPresent(event -> {
                event.setCancelled(true);

                Player player = caster.getPlayer();
                World world = player.getWorld();
                Location eyeLoc = player.getEyeLocation();

                // Effets de lancement
                world.playSound(eyeLoc, Sound.SHOOT_ARROW, 2f, 1f);
                spawnFrostParticles(world, eyeLoc, 15);

                // Lancer la flèche
                Arrow arrow = player.launchProjectile(Arrow.class);
                arrow.setShooter(player);
                arrow.setVelocity(eyeLoc.getDirection().multiply(2.0));
                arrow.setCritical(true);

                // Tracker la flèche
                Plugin plugin = Bukkit.getPluginManager().getPlugin("LeagueUHC");

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // Flèche morte ou au sol = impact
                        if (arrow.isDead() || arrow.isOnGround()) {
                            cancel();
                            onImpact(arrow.getLocation(), world, plugin);
                            return;
                        }

                        // Particules pendant le vol
                        spawnFrostParticles(world, arrow.getLocation(), 6);
                    }
                }.runTaskTimer(plugin, 0L, 1L);

                player.sendMessage(ChatColor.AQUA + "❄ Flèche de Givre tirée !");
            });
        }

        private void onImpact(Location impact, World world, Plugin plugin) {
            // Son d'impact
            world.playSound(impact, Sound.GLASS, 3f, 0.7f);

            // Stocker les blocs originaux
            Map<Location, Material> originalBlocks = new HashMap<>();

            // Geler la zone
            for (int x = -FREEZE_RADIUS; x <= FREEZE_RADIUS; x++) {
                for (int y = -1; y <= FREEZE_HEIGHT; y++) {
                    for (int z = -FREEZE_RADIUS; z <= FREEZE_RADIUS; z++) {
                        // Forme circulaire
                        if (x * x + z * z > FREEZE_RADIUS * FREEZE_RADIUS) continue;

                        Location blockLoc = impact.clone().add(x, y, z);
                        Block block = blockLoc.getBlock();
                        Material type = block.getType();

                        // Ne geler que les blocs solides (pas l'air, eau, lave)
                        if (type != Material.AIR && type != Material.WATER && type != Material.LAVA) {
                            originalBlocks.put(blockLoc.clone(), type);
                            block.setType(Material.PACKED_ICE);
                        }
                    }
                }
            }

            // Particules d'explosion de glace
            for (int i = 0; i < 30; i++) {
                Location particleLoc = impact.clone().add(
                        (Math.random() - 0.5) * FREEZE_RADIUS * 2,
                        Math.random() * FREEZE_HEIGHT,
                        (Math.random() - 0.5) * FREEZE_RADIUS * 2
                );
                world.playEffect(particleLoc, Effect.SNOWBALL_BREAK, 0);
            }

            // Restaurer les blocs après le délai
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Map.Entry<Location, Material> entry : originalBlocks.entrySet()) {
                        Location blockLoc = entry.getKey();
                        // Ne restaurer que si c'est toujours de la glace
                        if (blockLoc.getBlock().getType() == Material.PACKED_ICE) {
                            blockLoc.getBlock().setType(entry.getValue());
                        }
                    }

                    // Son de dégel
                    world.playSound(impact, Sound.GLASS, 3f, 1.2f);

                    // Particules de dégel
                    for (int i = 0; i < 20; i++) {
                        Location particleLoc = impact.clone().add(
                                (Math.random() - 0.5) * FREEZE_RADIUS,
                                Math.random() * 2,
                                (Math.random() - 0.5) * FREEZE_RADIUS
                        );
                        world.playEffect(particleLoc, Effect.SNOW_SHOVEL, 0);
                    }
                }
            }.runTaskLater(plugin, FREEZE_DURATION_SECONDS * 20L);
        }

        private void spawnFrostParticles(World world, Location loc, int count) {
            for (int i = 0; i < count; i++) {
                Location particleLoc = loc.clone().add(
                        Math.random() - 0.5,
                        Math.random() - 0.5,
                        Math.random() - 0.5
                );
                world.playEffect(particleLoc, Effect.SNOWBALL_BREAK, 0);
            }
        }
    }
}