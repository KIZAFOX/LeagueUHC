package fr.kiza.leagueuhc.champion.champions;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.champion.Champion;
import fr.kiza.leagueuhc.champion.ability.AbilityContext;
import fr.kiza.leagueuhc.champion.ability.ChampionAbility;
import fr.kiza.leagueuhc.champion.annotations.ChampionEntry;
import fr.kiza.leagueuhc.game.GamePlayer;
import fr.kiza.leagueuhc.utils.ItemBuilder;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@ChampionEntry
public class Ashe extends Champion {

    private final FrostArrow frostArrow = new FrostArrow();

    public Ashe() {
        super("Ashe", Collections.singletonList(new FrostArrow()));
    }

    @Override
    public void onAssigned(GamePlayer player) {
        final ItemBuilder item = new ItemBuilder(this.frostArrow.getItem())
                .setName(this.frostArrow.getName())
                .setLore(Collections.singletonList(this.frostArrow.getDescription()));

        player.getPlayer().getInventory().addItem(item.toItemStack());
        player.getPlayer().updateInventory();
    }

    private static class FrostArrow implements ChampionAbility {

        @Override
        public String getName() {
            return "Flèche de givre";
        }

        @Override
        public String getDescription() {
            return "Tire une flèche qui ralentit la cible et gèle la zone à l’impact, créant une cage de glace autour des entités.";
        }

        @Override
        public ItemStack getItem() {
            return DEFAULT_ITEM;
        }

        @Override
        public int getCooldownTicks() {
            return 5 * 20;
        }

        @Override
        public TriggerType getTriggerType() {
            return TriggerType.RIGHT_CLICK_ITEM;
        }

        @Override
        public void execute(GamePlayer caster, AbilityContext context) {
            final Player player = caster.getPlayer();
            final ItemStack held = player.getItemInHand();

            if (held == null || held.getType() != getItem().getType()) return;
            if (!held.hasItemMeta() || !held.getItemMeta().hasDisplayName()) return;
            if (!held.getItemMeta().getDisplayName().contains(this.getName())) return;

            context.getEvent().ifPresent(event -> {
                if (event instanceof PlayerInteractEvent) {
                    ((PlayerInteractEvent) event).setCancelled(true);
                }
            });

            final World world = player.getWorld();
            final Location loc = player.getEyeLocation();

            world.playSound(loc, Sound.SHOOT_ARROW, 2f, 1f);

            for (int i = 0; i < 15; i++) {
                world.playEffect(loc.clone().add(Math.random(), 1 + Math.random(), Math.random()), Effect.SNOWBALL_BREAK, 0);
            }

            final Arrow arrow = player.launchProjectile(Arrow.class);
            arrow.setShooter(player);
            arrow.setVelocity(loc.getDirection().multiply(2.0));
            arrow.setCritical(true);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (arrow.isDead() || arrow.isOnGround()) {
                        cancel();
                        Location impact = arrow.getLocation();
                        world.playSound(impact, Sound.GLASS, 3f, 0.7f);

                        int radius = 5;
                        Map<Location, Material> originalBlocks = new HashMap<>();

                        for (int x = -radius; x <= radius; x++) {
                            for (int y = -1; y <= 3; y++) {
                                for (int z = -radius; z <= radius; z++) {
                                    Location bLoc = impact.clone().add(x, y, z);
                                    Material type = bLoc.getBlock().getType();
                                    if (type != Material.AIR && type != Material.WATER && type != Material.LAVA) {
                                        originalBlocks.put(bLoc.clone(), type);
                                        bLoc.getBlock().setType(Material.PACKED_ICE);
                                    }
                                }
                            }
                        }

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                for (Map.Entry<Location, Material> entry : originalBlocks.entrySet()) {
                                    Location bLoc = entry.getKey();
                                    if (bLoc.getBlock().getType() == Material.PACKED_ICE) {
                                        bLoc.getBlock().setType(entry.getValue());
                                    }
                                }
                                world.playSound(impact, Sound.GLASS, 3f, 1f);
                            }
                        }.runTaskLater(LeagueUHC.getInstance(), 4 * 20);

                        return;
                    }

                    for (int i = 0; i < 6; i++) {
                        world.playEffect(
                                arrow.getLocation().clone().add(Math.random() - 0.5,
                                        Math.random() - 0.5, Math.random() - 0.5),
                                Effect.SNOWBALL_BREAK, 0);
                    }
                }
            }.runTaskTimer(LeagueUHC.getInstance(), 0L, 1L);
        }
    }
}