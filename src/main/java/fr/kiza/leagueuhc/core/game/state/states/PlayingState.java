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
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;

public class PlayingState extends BaseGameState {

    protected final LeagueUHC instance = LeagueUHC.getInstance();

    // Map pour stocker les informations de déconnexion
    private final Map<UUID, DisconnectedPlayerData> disconnectedPlayers = new HashMap<>();
    // Map pour stocker les tâches de timeout
    private final Map<UUID, BukkitTask> reconnectTasks = new HashMap<>();

    private static final int RECONNECT_TIMEOUT = 30; // 30 secondes

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
                    final double safeRadius = (uhcWorld.getWorldBorder().getSize() / 2.0) - 20;
                    final Location randomLocation = randomLocation(uhcWorld, safeRadius, usedLocations, minDistance);

                    usedLocations.add(randomLocation);

                    Chunk chunk = randomLocation.getChunk();
                    if (!chunk.isLoaded()) {
                        chunk.load(true);
                    }

                    final Location finalLoc = randomLocation.clone();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.teleport(finalLoc);

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (!player.getLocation().getWorld().getName().equals("uhc-world")) {
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
                                GameEventBus.getInstance().publish(new MovementFreezeEvent(false));
                            }
                        }.runTaskLater(LeagueUHC.getInstance(), 20L * 2);

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                ChampionAssignment.assignChampionsFromRegistry(players);
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

        // Nettoyer toutes les tâches de reconnexion
        reconnectTasks.values().forEach(BukkitTask::cancel);
        reconnectTasks.clear();
        disconnectedPlayers.clear();
    }

    @Override
    public void update(GameContext context, long deltaTime) {
        if (context.isPaused()) return;
    }

    @Override
    public void handleInput(GameContext context, GameInput input) {
        switch (input.getType()) {
            case PLAYER_JOIN:
                final Player joiningPlayer = input.getPlayer();
                final UUID joiningUUID = joiningPlayer.getUniqueId();

                // Vérifier si le joueur était déconnecté
                if (disconnectedPlayers.containsKey(joiningUUID)) {
                    DisconnectedPlayerData data = disconnectedPlayers.get(joiningUUID);

                    // Annuler la tâche de timeout
                    BukkitTask task = reconnectTasks.get(joiningUUID);
                    if (task != null) {
                        task.cancel();
                        reconnectTasks.remove(joiningUUID);
                    }

                    // Restaurer le joueur
                    restorePlayer(joiningPlayer, data);
                    disconnectedPlayers.remove(joiningUUID);

                    this.broadcast(ChatColor.GREEN + "✔ " + joiningPlayer.getName() + " s'est reconnecté !");
                    joiningPlayer.sendMessage("");
                    joiningPlayer.sendMessage(ChatColor.GREEN + "Tu as été reconnecté avec succès !");
                    joiningPlayer.sendMessage(ChatColor.YELLOW + "Tes items et ta position ont été restaurés.");
                    joiningPlayer.sendMessage("");
                } else {
                    // Nouveau joueur qui rejoint après le début
                    joiningPlayer.setGameMode(GameMode.SPECTATOR);
                    joiningPlayer.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "La partie a déjà commencé...");
                }
                break;

            case PLAYER_DEATH:
                final Player deadPlayer = input.getPlayer();
                final UUID deadPlayerUUID = deadPlayer.getUniqueId();

                // Si le joueur était en attente de reconnexion, annuler
                if (disconnectedPlayers.containsKey(deadPlayerUUID)) {
                    BukkitTask task = reconnectTasks.get(deadPlayerUUID);
                    if (task != null) {
                        task.cancel();
                        reconnectTasks.remove(deadPlayerUUID);
                    }
                    disconnectedPlayers.remove(deadPlayerUUID);
                }

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
                final Player leavingPlayer = input.getPlayer();
                final UUID leavingUUID = leavingPlayer.getUniqueId();

                // Seulement gérer la reconnexion si le joueur est vivant
                if (context.getAlivePlayers().contains(leavingUUID)) {
                    // Sauvegarder les données du joueur
                    DisconnectedPlayerData playerData = new DisconnectedPlayerData(
                            leavingPlayer.getLocation().clone(),
                            leavingPlayer.getInventory().getContents().clone(),
                            leavingPlayer.getInventory().getArmorContents().clone(),
                            leavingPlayer.getHealth(),
                            leavingPlayer.getFoodLevel(),
                            leavingPlayer.getLevel(),
                            leavingPlayer.getExp(),
                            new ArrayList<>(leavingPlayer.getActivePotionEffects())
                    );

                    disconnectedPlayers.put(leavingUUID, playerData);

                    // Créer une tâche pour gérer le timeout
                    BukkitTask timeoutTask = new BukkitRunnable() {
                        int timeLeft = RECONNECT_TIMEOUT;

                        @Override
                        public void run() {
                            if (timeLeft <= 0) {
                                // Timeout atteint, éliminer le joueur
                                context.setPlayerAlive(leavingUUID, false);
                                context.removePlayer(leavingUUID);
                                disconnectedPlayers.remove(leavingUUID);
                                reconnectTasks.remove(leavingUUID);

                                broadcast(ChatColor.RED + "☠ " + leavingPlayer.getName() + " a été éliminé (timeout de reconnexion) !");

                                this.cancel();
                                return;
                            }

                            // Afficher un message toutes les 10 secondes
                            if (timeLeft == RECONNECT_TIMEOUT || timeLeft == 20 || timeLeft == 10) {
                                broadcast(ChatColor.YELLOW + "⚠ " + leavingPlayer.getName() + " a " + timeLeft + " secondes pour se reconnecter...");
                            }

                            timeLeft--;
                        }
                    }.runTaskTimer(LeagueUHC.getInstance(), 0L, 20L);

                    reconnectTasks.put(leavingUUID, timeoutTask);

                    this.broadcast(ChatColor.YELLOW + "⚠ " + leavingPlayer.getName() + " s'est déconnecté (30s pour se reconnecter)");
                } else {
                    // Joueur spectateur ou déjà mort
                    context.removePlayer(leavingUUID);
                }
                break;

            default:
                break;
        }
    }

    private void restorePlayer(Player player, DisconnectedPlayerData data) {
        // Téléporter le joueur à sa position
        player.teleport(data.location);

        // Restaurer l'inventaire
        player.getInventory().setContents(data.inventory);
        player.getInventory().setArmorContents(data.armor);

        // Restaurer les stats
        player.setHealth(Math.min(data.health, player.getMaxHealth()));
        player.setFoodLevel(data.foodLevel);
        player.setLevel(data.level);
        player.setExp(data.exp);

        // Restaurer les effets de potion
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        data.potionEffects.forEach(player::addPotionEffect);

        // Remettre en mode survie
        player.setGameMode(GameMode.SURVIVAL);

        // Effets visuels
        player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0f, 1.0f);
        player.sendTitle(ChatColor.GREEN + "✔ Reconnecté", ChatColor.YELLOW + "Bienvenue !");
    }

    private Location randomLocation(final World world, final double borderRadius, final List<Location> usedLocations, final double minDistance) {
        final Random random = new Random();
        Location location;
        int maxAttempts = 100;

        do {
            double
                    x = (random.nextDouble() * 2 - 1) * borderRadius,
                    z = (random.nextDouble() * 2 - 1) * borderRadius;

            int chunkX = (int) x >> 4;
            int chunkZ = (int) z >> 4;

            if (!world.isChunkLoaded(chunkX, chunkZ)) {
                world.loadChunk(chunkX, chunkZ, true);
            }

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

            if (!world.isChunkLoaded(0, 0)) {
                world.loadChunk(0, 0, true);
            }
        }

        return location;
    }

    private static class DisconnectedPlayerData {
        final Location location;
        final ItemStack[] inventory;
        final ItemStack[] armor;
        final double health;
        final int foodLevel;
        final int level;
        final float exp;
        final List<PotionEffect> potionEffects;

        DisconnectedPlayerData(Location location, ItemStack[] inventory, ItemStack[] armor, double health, int foodLevel, int level, float exp, List<PotionEffect> potionEffects) {
            this.location = location;
            this.inventory = inventory;
            this.armor = armor;
            this.health = health;
            this.foodLevel = foodLevel;
            this.level = level;
            this.exp = exp;
            this.potionEffects = potionEffects;
        }
    }
}