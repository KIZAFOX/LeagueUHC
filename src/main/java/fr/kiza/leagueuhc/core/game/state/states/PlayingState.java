package fr.kiza.leagueuhc.core.game.state.states;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.api.champion.ChampionAssignment;
import fr.kiza.leagueuhc.core.api.packets.builder.ActionBarBuilder;
import fr.kiza.leagueuhc.core.api.packets.builder.TitleBuilder;
import fr.kiza.leagueuhc.core.game.context.GameContext;
import fr.kiza.leagueuhc.core.game.event.bus.GameEventBus;
import fr.kiza.leagueuhc.core.game.event.MovementFreezeEvent;
import fr.kiza.leagueuhc.core.game.helper.InventoryHelper;
import fr.kiza.leagueuhc.core.game.input.GameInput;
import fr.kiza.leagueuhc.core.game.mechanics.GameMechanicsManager;
import fr.kiza.leagueuhc.core.game.state.BaseGameState;
import fr.kiza.leagueuhc.core.game.state.GameState;
import fr.kiza.leagueuhc.core.game.timer.GameTimerManager;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
        new GameMechanicsManager().init();

        context.getPlayers().forEach(players -> context.setPlayerAlive(players, true));

        this.broadcast("");
        this.broadcast(ChatColor.GOLD + "" + ChatColor.BOLD + "=============================");
        this.broadcast(ChatColor.YELLOW + "" + ChatColor.BOLD + "   LA PARTIE COMMENCE !");
        this.broadcast(ChatColor.GOLD + "" + ChatColor.BOLD + "=============================");
        this.broadcast("");

        final World uhcWorld = Bukkit.getWorld("uhc-world");

        if (uhcWorld == null) {
            Bukkit.getLogger().severe("[LeagueUHC] Le monde uhc-world n'existe pas !");
            Bukkit.getOnlinePlayers().forEach(players -> players.sendMessage(ChatColor.RED + "[UHC] Le monde UHC n'a pas pu être chargé !"));
            return;
        }

        Bukkit.getLogger().info("[LeagueUHC] Monde UHC trouvé: " + uhcWorld.getName());

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

        final int totalPlayers = players.size();
        final int[] index = {0};
        final List<Location> usedLocations = new ArrayList<>();
        final double minDistance = 10.0;

        players.forEach(player -> player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * (totalPlayers + 2), 1, false, false)));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (index[0] >= players.size()) {
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0f, 1.0f);
                        player.removePotionEffect(PotionEffectType.BLINDNESS);
                    });

                    this.cancel();
                    return;
                }

                final Player player = players.get(index[0]);

                if (player != null && player.isOnline()) {
                    final Location randomLocation = randomLocation(uhcWorld, uhcWorld.getWorldBorder().getSize() / 2.0, usedLocations, minDistance);

                    usedLocations.add(randomLocation);

                    // FORCER LE CHARGEMENT DU CHUNK AVANT LA TÉLÉPORTATION
                    Chunk chunk = randomLocation.getChunk();
                    if (!chunk.isLoaded()) {
                        chunk.load(true); // true = génère le chunk s'il n'existe pas
                    }

                    Bukkit.getLogger().info("[LeagueUHC] Téléportation de " + player.getName() + " vers " +
                            randomLocation.getBlockX() + ", " + randomLocation.getBlockY() + ", " + randomLocation.getBlockZ() +
                            " dans le monde: " + randomLocation.getWorld().getName());

                    // Téléportation avec un petit délai pour s'assurer que le chunk est bien chargé
                    final Location finalLoc = randomLocation.clone();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.teleport(finalLoc);

                            // Vérification après téléportation
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    Location currentLoc = player.getLocation();
                                    Bukkit.getLogger().info("[LeagueUHC] Position actuelle de " + player.getName() + ": " +
                                            currentLoc.getBlockX() + ", " + currentLoc.getBlockY() + ", " + currentLoc.getBlockZ() +
                                            " dans le monde: " + currentLoc.getWorld().getName());

                                    if (!currentLoc.getWorld().getName().equals("uhc-world")) {
                                        Bukkit.getLogger().warning("[LeagueUHC] PROBLEME: " + player.getName() + " n'est pas dans uhc-world ! Re-téléportation...");
                                        player.teleport(finalLoc);
                                    }
                                }
                            }.runTaskLater(LeagueUHC.getInstance(), 5L);
                        }
                    }.runTaskLater(LeagueUHC.getInstance(), 2L);

                    Bukkit.getOnlinePlayers().forEach(p -> new ActionBarBuilder().message(ChatColor.YELLOW + "Téléportation: " + ChatColor.GREEN + (index[0] + 1) + ChatColor.GRAY + "/" + ChatColor.GREEN + totalPlayers).send(p));

                    player.sendMessage(ChatColor.GREEN + "✔ Vous avez été téléporté aléatoirement !");
                    player.playSound(player.getLocation(), Sound.LEVEL_UP, 0.5f, 1.0f);

                    player.setGameMode(GameMode.SURVIVAL);
                    player.setFoodLevel(20);
                    player.setWalkSpeed(0.20F);
                    player.setFlySpeed(0.15F);
                    player.setAllowFlight(false);
                    player.setFlying(false);
                    player.setExp(0);
                    player.setLevel(0);
                    player.setMaxHealth(20.0D);
                    player.setHealth(player.getMaxHealth());

                    if (InventoryHelper.hasStartInventory()) {
                        final InventoryHelper startInventory = InventoryHelper.getStartInventory();
                        player.getInventory().setContents(startInventory.getContents().clone());
                        player.getInventory().setArmorContents(startInventory.getArmor().clone());
                    }

                    if (index[0] == players.size() - 1) {
                        broadcast("");
                        broadcast(ChatColor.YELLOW + "⚠ Début de la partie !");
                        broadcast(ChatColor.LIGHT_PURPLE + "Vous aurez votre champion dans 10 secondes !");
                        broadcast(ChatColor.GREEN + "Le PvP sera activé dans 20 secondes !");
                        broadcast("");

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                ChampionAssignment.assignChampionsFromRegistry(players);
                                GameEventBus.getInstance().publish(new MovementFreezeEvent(false));
                            }
                        }.runTaskLater(LeagueUHC.getInstance(), 20L * 10);
                    }

                    TitleBuilder.create()
                            .title(ChatColor.GOLD + "⚔ " + ChatColor.YELLOW + "LEAGUE UHC" + ChatColor.GOLD + " ⚔")
                            .subTitle(ChatColor.GRAY + "Bonne chance !")
                            .times(10, 70, 20)
                            .send(player);
                }

                index[0]++;
            }
        }.runTaskTimer(LeagueUHC.getInstance(), 0L, 20L);
    }

    @Override
    public void onExit(GameContext context) {
        GameTimerManager.getInstance().stop();
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

            if(block.isSolid() &&
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
}