package fr.kiza.leagueuhc.core.game.state.states;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.api.packets.builder.TitleBuilder;
import fr.kiza.leagueuhc.core.game.context.GameContext;
import fr.kiza.leagueuhc.core.game.helper.InventoryHelper;
import fr.kiza.leagueuhc.core.game.helper.pregen.PregenManager;
import fr.kiza.leagueuhc.core.game.input.GameInput;
import fr.kiza.leagueuhc.core.game.state.BaseGameState;
import fr.kiza.leagueuhc.core.game.state.GameState;

import fr.kiza.leagueuhc.managers.commands.CommandUHC;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class PlayingState extends BaseGameState {

    protected final LeagueUHC instance = LeagueUHC.getInstance();

    public PlayingState() {
        super(GameState.PLAYING.getName());
    }

    @Override
    public void onEnter(GameContext context) {
        context.getPlayers().forEach(players -> context.setPlayerAlive(players, true));

        this.broadcast("");
        this.broadcast(ChatColor.GOLD + "" + ChatColor.BOLD + "=============================");
        this.broadcast(ChatColor.YELLOW + "" + ChatColor.BOLD + "   LA PARTIE COMMENCE !");
        this.broadcast(ChatColor.GOLD + "" + ChatColor.BOLD + "=============================");
        this.broadcast("");

        final World uhcWorld = PregenManager.world;
        final Location location = uhcWorld.getSpawnLocation();

        if (location == null) {
            Bukkit.getOnlinePlayers().forEach(players -> players.sendMessage(ChatColor.RED + "[UHC] Un problème est survenu lors du chargement de la map uhc !"));
            return;
        }

        final List<Player> players = context.getPlayers().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Collections.shuffle(players);

        final int[] index = {0};
        final List<Location> usedLocations = new ArrayList<>();
        final double minDistance = 10.0;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (index[0] >= players.size()) {
                    Bukkit.getOnlinePlayers().forEach(player ->
                            player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0f, 1.0f)
                    );

                    this.cancel();
                    return;
                }

                final Player player = players.get(index[0]);

                if (player != null && player.isOnline()) {
                    final Location randomLocation = randomLocation(Objects.requireNonNull(uhcWorld), uhcWorld.getWorldBorder().getSize() / 2.0, usedLocations, minDistance);
                    usedLocations.add(randomLocation);

                    player.teleport(randomLocation);

                    player.sendMessage(ChatColor.GREEN + "✔ Vous avez été téléporté aléatoirement !");
                    player.playSound(player.getLocation(), Sound.LEVEL_UP, 0.5f, 1.0f);

                    final InventoryHelper universalKit = InventoryHelper.START_INVENTORY;

                    if (universalKit != null) {
                        player.setGameMode(GameMode.CREATIVE);

                        player.setFoodLevel(20);
                        player.setWalkSpeed(0.20F);
                        player.setFlySpeed(0.15F);
                        player.setAllowFlight(false);
                        player.setFlying(false);
                        player.setExp(0);
                        player.setLevel(0);
                        player.setMaxHealth(20.0D);
                        player.setHealth(player.getMaxHealth());

                        player.getInventory().setContents(universalKit.getContents().clone());
                        player.getInventory().setArmorContents(universalKit.getArmor().clone());

                        broadcast(ChatColor.GREEN + "✔ Vous avez reçus le kit de départ fait par le host !");
                        broadcast("");
                        broadcast(ChatColor.YELLOW + "⚠ La partie commence maintenant !");
                        broadcast("");
                    } else {
                        broadcast(ChatColor.RED + "⚠ Aucun kit de départ n'a été défini !");
                        broadcast(ChatColor.YELLOW + "Les joueurs commenceront sans équipement.");
                    }

                    TitleBuilder.create()
                            .title(ChatColor.GOLD + "⚔ " + ChatColor.YELLOW + "LEAGUE UHC" + ChatColor.GOLD + " ⚔")
                            .subTitle(ChatColor.GRAY + "Bonne chance !")
                            .times(10, 70, 20)
                            .send(player);
                }

                index[0]++;
            }

            private Location randomLocation(final World world, final double borderRadius, final List<Location> usedLocations, final double minDistance) {
                final Random random = new Random();
                Location location;
                int maxAttempts = 100;

                do {
                    double
                            x = (random.nextDouble() * 2 - 1) * (borderRadius - 10),
                            z = (random.nextDouble() * 2 - 1) * (borderRadius - 10);
                    int y = world.getHighestBlockYAt((int) x, (int) z);

                    location = new Location(world, x + 0.5, y + 1, z + 0.5);

                    final Material block = world.getBlockAt((int) x, y - 1, (int) z).getType();

                    if(block.isSolid() && block != Material.LEAVES && block != Material.LEAVES_2 &&
                            block != Material.WATER && block != Material.LAVA && block != Material.CACTUS &&
                            block != Material.FIRE) {

                        boolean tooClose = false;
                        for (Location usedLoc : usedLocations) {
                            if (usedLoc.distance(location) < minDistance) {
                                tooClose = true;
                                break;
                            }
                        }

                        if (!tooClose) {
                            break;
                        }
                    }
                } while (--maxAttempts > 0);

                if (maxAttempts == 0) {
                    location = new Location(world, 0.5, world.getHighestBlockYAt(0, 0) + 1, 0.5);
                }

                return location;
            }
        }.runTaskTimer(LeagueUHC.getInstance(), 0L, 20L);

        this.instance.getGameEngine().getEffectsApplier().startEffectTask();
        this.instance.getGameEngine().getEffectsApplier().applyEffectsToAllPlayers();
    }

    @Override
    public void onExit(GameContext context) {
        this.instance.getGameEngine().getEffectsApplier().stopEffectTask();
        Bukkit.getOnlinePlayers().forEach(this.instance.getGameEngine().getEffectsApplier()::removeAllEffects);
    }

    @Override
    public void update(GameContext context, long deltaTime) {
        if (context.isPaused()) return;
    }

    @Override
    public void handleInput(GameContext context, GameInput input) {
        switch (input.getType()) {
            case PLAYER_JOIN:
                final Player player = input.getPlayer();

                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "La partie a déjà commencé...");
                break;
            case PLAYER_DEATH:
                final Player deadPlayer = input.getPlayer();
                final UUID deadPlayerUUID = deadPlayer.getUniqueId();

                context.setPlayerAlive(deadPlayerUUID, false);

                this.broadcast(ChatColor.RED + "☠ " + deadPlayer.getName() + ChatColor.GRAY + " est mort !");

                Bukkit.getOnlinePlayers().forEach(players ->
                        players.playSound(players.getLocation(), Sound.WITHER_SPAWN, 0.5f, 1.0f)
                );

                final Location deathLocation = deadPlayer.getLocation().clone().add(0, 1, 0);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!deadPlayer.isOnline()) return;

                        deadPlayer.spigot().respawn();

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (!deadPlayer.isOnline()) return;

                                deadPlayer.teleport(deathLocation);
                                deadPlayer.setGameMode(GameMode.SPECTATOR);
                                deadPlayer.playSound(deathLocation, Sound.BAT_TAKEOFF, 0.8f, 1.0f);
                                deadPlayer.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Tu es maintenant en mode spectateur.");
                            }
                        }.runTaskLater(LeagueUHC.getInstance(), 2L);
                    }
                }.runTaskLater(LeagueUHC.getInstance(), 20L);
                break;
            case PLAYER_LEAVE:
                final UUID leavingPlayer = input.getPlayer().getUniqueId();

                if (context.getAlivePlayers().contains(leavingPlayer)) {
                    context.setPlayerAlive(leavingPlayer, false);
                    this.broadcast(ChatColor.RED + input.getPlayer().getName() + " a quitté la partie (éliminé) !");
                }

                context.removePlayer(leavingPlayer);
                break;

            default:
                break;
        }
    }
}