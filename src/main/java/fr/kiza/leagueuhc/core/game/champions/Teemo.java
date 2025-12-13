package fr.kiza.leagueuhc.core.game.champions;

import fr.kiza.leagueuhc.core.api.champion.Champion;
import fr.kiza.leagueuhc.core.api.champion.ability.Ability;
import fr.kiza.leagueuhc.core.api.champion.ability.AbilityContext;
import fr.kiza.leagueuhc.core.api.champion.ability.StatefulAbility;
import fr.kiza.leagueuhc.core.api.champion.annotations.ChampionEntry;
import fr.kiza.leagueuhc.core.game.GamePlayer;
import fr.kiza.leagueuhc.utils.ItemBuilder;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Iterator;
import java.util.Set;

/**
 * Champion Teemo - Le Scout Swift
 *
 * Abilities:
 * - Passive: Camouflage - Devient invisible en restant accroupi
 * - Active: Piège Nocif - Pose des champignons explosifs (invisibles après 2s)
 */
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
        // Donner l'item de l'ability Piège Nocif
        getAbility("Piège Nocif").ifPresent(ability -> {
            ItemStack item = new ItemBuilder(ability.getItemMaterial())
                    .setName(ChatColor.GREEN + ability.getName())
                    .setLore(
                            ChatColor.GRAY + ability.getDescription(),
                            "",
                            ChatColor.YELLOW + "Cooldown: " + ability.getCooldownSeconds() + "s"
                    )
                    .toItemStack();

            item.setAmount(5); // Donner 5 champignons de base
            player.getPlayer().getInventory().addItem(item);
            player.getPlayer().updateInventory();
        });
    }

    @Override
    public void onRevoke(GamePlayer player) {
        // Retirer les effets de potion
        player.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PASSIVE : Camouflage - Invisibilité en sneak
    // ═══════════════════════════════════════════════════════════════════════════

    private static class CamouflagePassive extends Ability {

        @Override
        public String getName() {
            return "Camouflage";
        }

        @Override
        public String getDescription() {
            return "Devient invisible en restant accroupi.";
        }

        @Override
        public Trigger getTrigger() {
            return Trigger.PASSIVE;
        }

        @Override
        public void onTick(GamePlayer owner) {
            Player player = owner.getPlayer();
            Location location = player.getLocation();

            if (location.getX() != player.getLocation().getX() || location.getY() != player.getLocation().getY() || location.getZ() != player.getLocation().getZ()) return;

            if (player.isSneaking()) {
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.INVISIBILITY,
                        5, // 5 ticks = 0.25 secondes
                        0, // Niveau 1
                        false, // Pas de particules ambiantes
                        false  // Pas d'icône
                ), true);
            }
        }

        @Override
        public void execute(GamePlayer caster, AbilityContext ctx) {  }
    }

    /**
     * Représente un piège champignon avec sa location et son timestamp de pose.
     */
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

                // Annuler l'event pour éviter les actions par défaut
                event.setCancelled(true);

                // Placer le champignon
                Block target = clicked.getRelative(event.getBlockFace());

                // Vérifier si on peut placer
                if (target.getType() != Material.AIR) {
                    caster.getPlayer().sendMessage(ChatColor.RED + "Impossible de placer le piège ici !");
                    return;
                }

                // Placer le champignon visible
                target.setType(Material.BROWN_MUSHROOM);

                // Ajouter le piège à la liste
                MushroomTrap trap = new MushroomTrap(target.getLocation());
                addState(caster.getUUID(), trap);

                // Consommer l'item
                ItemStack item = event.getItem();
                if (item != null && item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    caster.getPlayer().setItemInHand(null);
                }

                // Feedback
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

                // Vérifier si le piège doit devenir invisible
                if (!trap.hidden && (now - trap.placedAt) >= HIDE_DELAY_MS) {
                    // Retirer le bloc champignon (devient invisible)
                    if (trapLoc.getBlock().getType() == Material.BROWN_MUSHROOM) {
                        trapLoc.getBlock().setType(Material.AIR);
                    }
                    trap.hidden = true;

                    // Particules discrètes pour le owner uniquement
                    ownerPlayer.playEffect(trapLoc.clone().add(0.5, 0.2, 0.5), Effect.HAPPY_VILLAGER, 0);
                }

                // Particules périodiques pour le owner (pour voir ses pièges cachés)
                if (trap.hidden && now % 1000 < 50) { // Toutes les ~1 seconde
                    ownerPlayer.playEffect(trapLoc.clone().add(0.5, 0.1, 0.5), Effect.COLOURED_DUST, 0);
                }

                // Centre du bloc pour la détection
                Location center = trapLoc.clone().add(0.5, 0, 0.5);

                // Vérifier les joueurs à proximité
                for (Player target : world.getPlayers()) {
                    // Ignorer le propriétaire
                    if (target.equals(ownerPlayer)) continue;

                    // Vérifier la distance (pieds du joueur)
                    double distSq = target.getLocation().distanceSquared(center);
                    if (distSq > TRIGGER_RADIUS * TRIGGER_RADIUS) continue;

                    // BOOM !
                    explodeTrap(trapLoc, ownerPlayer, target, trap.hidden);
                    iterator.remove();
                    break;
                }
            }
        }

        private void explodeTrap(Location location, Player owner, Player victim, boolean wasHidden) {
            World world = location.getWorld();

            // Si le piège était visible, on retire le bloc
            if (!wasHidden && location.getBlock().getType() == Material.BROWN_MUSHROOM) {
                location.getBlock().setType(Material.AIR);
            }

            // Effets visuels
            world.playEffect(location, Effect.POTION_BREAK, 0);
            world.playEffect(location, Effect.SMOKE, 4);
            world.playSound(location, Sound.EXPLODE, 0.8f, 1.2f);

            // Particules de champignon
            for (int i = 0; i < 10; i++) {
                world.playEffect(
                        location.clone().add(Math.random() - 0.5, Math.random(), Math.random() - 0.5),
                        Effect.SPELL,
                        0
                );
            }

            // Dégâts et effets
            victim.damage(DAMAGE, owner);
            victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, POISON_DURATION, 1));
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, SLOW_DURATION, 1));

            // Messages
            victim.sendMessage(ChatColor.RED + "🍄 Tu as marché sur un piège de " + owner.getName() + " !");
            owner.sendMessage(ChatColor.GREEN + "🍄 Ton piège a explosé sur " + victim.getName() + " !");
        }

        @Override
        public void onDisable(GamePlayer owner) {
            // Nettoyer tous les champignons posés (visibles uniquement)
            for (MushroomTrap trap : getStates(owner.getUUID())) {
                if (!trap.hidden && trap.location.getBlock().getType() == Material.BROWN_MUSHROOM) {
                    trap.location.getBlock().setType(Material.AIR);
                }
            }
            super.onDisable(owner);
        }
    }
}