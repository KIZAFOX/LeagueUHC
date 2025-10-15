package fr.kiza.leagueuhc.champion.champions;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.champion.Champion;
import fr.kiza.leagueuhc.champion.ability.AbilityContext;
import fr.kiza.leagueuhc.champion.ability.ChampionAbility;
import fr.kiza.leagueuhc.champion.annotations.ChampionEntry;
import fr.kiza.leagueuhc.game.GamePlayer;
import fr.kiza.leagueuhc.utils.ItemBuilder;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@ChampionEntry
public class Teemo extends Champion {

    private static final Map<Location, UUID> MUSHROOMS = new HashMap<>();

    private MushroomAbility mushroomAbility = new MushroomAbility();

    public Teemo() {
        super("Teemo", Collections.singletonList(new MushroomAbility()));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (MUSHROOMS.isEmpty()) return;

                for (Iterator<Map.Entry<Location, UUID>> it = MUSHROOMS.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<Location, UUID> entry = it.next();
                    Location loc = entry.getKey();
                    UUID ownerId = entry.getValue();

                    Player owner = Bukkit.getPlayer(ownerId);
                    if (owner == null) {
                        it.remove();
                        continue;
                    }

                    World world = loc.getWorld();
                    for (Player target : world.getPlayers()) {
                        if (target.getUniqueId().equals(ownerId)) continue; // owner safe

                        if (target.getLocation().distance(loc.clone().add(0.5, 0, 0.5)) <= 1.1) {
                            // Explosion du champignon
                            mushroomAbility = new MushroomAbility(owner, target, loc);
                            mushroomAbility.execute(GamePlayer.get(owner), AbilityContext.EMPTY);

                            loc.getBlock().setType(Material.AIR);
                            it.remove();
                            break;
                        }
                    }
                }
            }
        }.runTaskTimer(LeagueUHC.getInstance(), 0L, 3L);
    }

    @Override
    public void onAssigned(GamePlayer player) {
        ItemStack item = new ItemBuilder(this.mushroomAbility.getItem())
                .setName(this.mushroomAbility.getName())
                .setLore(Collections.singletonList(this.mushroomAbility.getDescription()))
                .toItemStack();

        player.getPlayer().getInventory().addItem(item);
        player.getPlayer().updateInventory();
    }

    @Override
    public void onUpdate(GamePlayer player) {
        if (player.getPlayer().isSneaking()) {
            player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20, 0));
        }
    }

    private static class MushroomAbility implements ChampionAbility {

        private Player owner, target;
        private Location location;

        public MushroomAbility() { }

        public MushroomAbility(Player owner, Player target, Location location) {
            this.owner = owner;
            this.target = target;
            this.location = location;
        }

        @Override
        public String getName() {
            return "Piège Nocif";
        }

        @Override
        public String getDescription() {
            return "Pose un champignon explosif infligeant du poison et du ralentissement.";
        }

        @Override
        public ItemStack getItem() {
            return new ItemBuilder(Material.BONE).toItemStack();
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
            if(owner == null && target == null && location == null) {
                context.getEvent().ifPresent(event -> {
                    if (!(event instanceof PlayerInteractEvent)) return;

                    final PlayerInteractEvent e = (PlayerInteractEvent) event;
                    ItemStack item = e.getItem();
                    if (item == null || !item.hasItemMeta()) return;
                    if (item.getType() != getItem().getType()) return;
                    if (!item.getItemMeta().getDisplayName().contains(getName())) return;

                    e.setCancelled(true);

                    Player player = e.getPlayer();
                    Block clicked = e.getClickedBlock();
                    if (clicked == null) return;

                    Block targetBlock = clicked.getRelative(e.getBlockFace());
                    targetBlock.setType(Material.BROWN_MUSHROOM);

                    MUSHROOMS.put(targetBlock.getLocation(), player.getUniqueId());
                    player.sendMessage(ChatColor.GREEN + "Champignon posé !");

                    item.setAmount(item.getAmount() - 1);
                });
                return;
            }

            if(owner != null && target != null && location != null) {
                World world = location.getWorld();
                world.playEffect(location, Effect.POTION_BREAK, 0);
                world.playEffect(location, Effect.SMOKE, 4);
                world.playSound(location, Sound.EXPLODE, 1f, 1f);

                target.damage(2.0, owner);
                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * 5, 1));
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 3, 1));

                target.sendMessage(ChatColor.RED + "Tu as marché sur un champignon de Teemo !");
                owner.sendMessage(ChatColor.GREEN + "Ton piège a explosé !");
            }
        }
    }
}