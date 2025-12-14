package fr.kiza.leagueuhc.core.game.champions;

import fr.kiza.leagueuhc.core.api.champion.Champion;
import fr.kiza.leagueuhc.core.api.champion.ability.Ability;
import fr.kiza.leagueuhc.core.api.champion.ability.AbilityContext;
import fr.kiza.leagueuhc.core.api.champion.ability.StatefulAbility;
import fr.kiza.leagueuhc.core.api.champion.annotations.ChampionEntry;
import fr.kiza.leagueuhc.core.api.packets.builder.ParticleBuilder;
import fr.kiza.leagueuhc.core.game.GamePlayer;
import fr.kiza.leagueuhc.utils.ItemBuilder;

import net.minecraft.server.v1_8_R3.EnumParticle;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@ChampionEntry
public class Teemo extends Champion {

    public Teemo() {
        super(
                "Teemo",
                "Scout agile posant des pièges empoisonnés",
                Category.ASSASSIN,
                Region.BANDLE_CITY,
                Material.BROWN_MUSHROOM
        );

        registerAbility(new CamouflagePassive());
        registerAbility(new MushroomTrapAbility());
    }

    @Override
    public void onAssign(GamePlayer player) {
        getAbility("Piège Nocif").ifPresent(ability -> {
            ItemStack item = new ItemBuilder(ability.getItemMaterial())
                    .setName(ChatColor.GREEN + ability.getName())
                    .setLore(
                            ChatColor.GRAY + ability.getDescription(),
                            "",
                            ChatColor.YELLOW + "Cooldown: " + ability.getCooldownSeconds() + "s"
                    )
                    .toItemStack();

            item.setAmount(5);
            player.getPlayer().getInventory().addItem(item);
            player.getPlayer().updateInventory();
        });
    }

    @Override
    public void onRevoke(GamePlayer player) {
        player.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
    }

    private static class CamouflagePassive extends Ability {

        private final Map<UUID, Location> lastPositions = new HashMap<>();

        @Override
        public String getName() {
            return "Camouflage";
        }

        @Override
        public String getDescription() {
            return "Devient invisible en restant accroupi et immobile.";
        }

        @Override
        public Trigger getTrigger() {
            return Trigger.PASSIVE;
        }

        @Override
        public void onTick(GamePlayer owner) {
            Player player = owner.getPlayer();
            UUID uuid = player.getUniqueId();
            Location currentLoc = player.getLocation();
            Location lastLoc = lastPositions.get(uuid);

            boolean hasMoved = lastLoc != null &&
                    (lastLoc.getX() != currentLoc.getX() ||
                            lastLoc.getY() != currentLoc.getY() ||
                            lastLoc.getZ() != currentLoc.getZ());

            lastPositions.put(uuid, currentLoc.clone());

            if (player.isSneaking() && !hasMoved) {
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.INVISIBILITY,
                        5,
                        0,
                        false,
                        false
                ), true);
            } else if (hasMoved && player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
            }
        }

        @Override
        public void onDisable(GamePlayer owner) {
            lastPositions.remove(owner.getUUID());
        }

        @Override
        public void execute(GamePlayer caster, AbilityContext ctx) {  }
    }

    private static class MushroomTrap {
        final Location location;
        final long placedAt;
        boolean hidden = false;

        MushroomTrap(Location location) {
            this.location = location;
            this.placedAt = System.currentTimeMillis();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MushroomTrap)) return false;
            return location.equals(((MushroomTrap) obj).location);
        }

        @Override
        public int hashCode() {
            return location.hashCode();
        }
    }

    private static class MushroomTrapAbility extends StatefulAbility<MushroomTrap> {

        private static final double TRIGGER_RADIUS = 1.2;
        private static final double DAMAGE = 2.0;
        private static final int POISON_DURATION = 5 * 20;
        private static final int SLOW_DURATION = 3 * 20;
        private static final long HIDE_DELAY_MS = 2000;

        @Override
        public String getName() {
            return "Piège Nocif";
        }

        @Override
        public String getDescription() {
            return "Pose un champignon explosif (invisible après 2s).";
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
                Block clicked = event.getClickedBlock();
                if (clicked == null) {
                    caster.getPlayer().sendMessage(ChatColor.RED + "Cliquez sur un bloc pour poser le piège !");
                    return;
                }

                event.setCancelled(true);

                Block target = clicked.getRelative(event.getBlockFace());

                if (target.getType() != Material.AIR) {
                    caster.getPlayer().sendMessage(ChatColor.RED + "Impossible de placer le piège ici !");
                    return;
                }

                target.setType(Material.BROWN_MUSHROOM);

                MushroomTrap trap = new MushroomTrap(target.getLocation());
                addState(caster.getUUID(), trap);

                ItemStack item = event.getItem();
                if (item != null && item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    caster.getPlayer().setItemInHand(null);
                }

                caster.getPlayer().sendMessage(ChatColor.GREEN + "🍄 Champignon posé ! (invisible dans 2s)");
                caster.getPlayer().playSound(caster.getPlayer().getLocation(), Sound.DIG_GRASS, 1f, 1f);
            });
        }

        @Override
        public void onTick(GamePlayer owner) {
            Set<MushroomTrap> traps = getStates(owner.getUUID());
            if (traps.isEmpty()) return;

            Player ownerPlayer = owner.getPlayer();
            World world = ownerPlayer.getWorld();
            long now = System.currentTimeMillis();

            Iterator<MushroomTrap> iterator = traps.iterator();
            while (iterator.hasNext()) {
                MushroomTrap trap = iterator.next();
                Location trapLoc = trap.location;

                if (!trap.hidden && (now - trap.placedAt) >= HIDE_DELAY_MS) {
                    if (trapLoc.getBlock().getType() == Material.BROWN_MUSHROOM) {
                        trapLoc.getBlock().setType(Material.AIR);
                    }
                    trap.hidden = true;

                    ParticleBuilder.create()
                            .type(EnumParticle.VILLAGER_HAPPY)
                            .location(trapLoc.clone().add(0.5, 0.2, 0.5))
                            .count(5)
                            .send(ownerPlayer);
                }

                if (trap.hidden && now % 1000 < 50) {
                    ParticleBuilder.create()
                            .type(EnumParticle.REDSTONE)
                            .location(trapLoc.clone().add(0.5, 0.1, 0.5))
                            .count(3)
                            .send(ownerPlayer);
                }

                Location center = trapLoc.clone().add(0.5, 0, 0.5);

                for (Player target : world.getPlayers()) {
                    if (target.equals(ownerPlayer)) continue;

                    double distSq = target.getLocation().distanceSquared(center);
                    if (distSq > TRIGGER_RADIUS * TRIGGER_RADIUS) continue;

                    explodeTrap(trapLoc, ownerPlayer, target, trap.hidden);
                    iterator.remove();
                    break;
                }
            }
        }

        private void explodeTrap(Location location, Player owner, Player victim, boolean wasHidden) {
            World world = location.getWorld();

            if (!wasHidden && location.getBlock().getType() == Material.BROWN_MUSHROOM) {
                location.getBlock().setType(Material.AIR);
            }

            world.playEffect(location, Effect.POTION_BREAK, 0);
            world.playEffect(location, Effect.SMOKE, 4);
            world.playSound(location, Sound.EXPLODE, 0.8f, 1.2f);

            ParticleBuilder.create()
                    .type(EnumParticle.SPELL)
                    .location(location.clone().add(0.5, 0.5, 0.5))
                    .offset(0.5f, 0.5f, 0.5f)
                    .count(10)
                    .sendInRadius(30);

            victim.damage(DAMAGE, owner);
            victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, POISON_DURATION, 1));
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, SLOW_DURATION, 1));

            victim.sendMessage(ChatColor.RED + "🍄 Tu as marché sur un piège de " + owner.getName() + " !");
            owner.sendMessage(ChatColor.GREEN + "🍄 Ton piège a explosé sur " + victim.getName() + " !");
        }

        @Override
        public void onDisable(GamePlayer owner) {
            for (MushroomTrap trap : getStates(owner.getUUID())) {
                if (!trap.hidden && trap.location.getBlock().getType() == Material.BROWN_MUSHROOM) {
                    trap.location.getBlock().setType(Material.AIR);
                }
            }
            super.onDisable(owner);
        }
    }
}