package fr.kiza.leagueuhc.core.game.state.states;

import fr.kiza.leagueuhc.core.game.context.GameContext;
import fr.kiza.leagueuhc.core.game.state.BaseGameState;
import fr.kiza.leagueuhc.core.game.state.GameState;
import fr.kiza.leagueuhc.core.game.timer.GameTimerManager;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;

public class EndingState extends BaseGameState {

    private long displayTime = 0;
    private boolean fireworksLaunched = false, playersElevated = false;

    public EndingState() {
        super(GameState.ENDING.getName());
    }

    @Override
    public void onEnter(GameContext context) {
        this.displayTime = 0;
        this.fireworksLaunched = false;
        this.playersElevated = false;

        context.setData("endingStartTime", System.currentTimeMillis());

        calculateAndDisplayResults(context);
    }

    @Override
    public void onExit(GameContext context) { }

    @Override
    public void update(GameContext context, long deltaTime) {
        this.displayTime += deltaTime;

        if (!this.playersElevated && this.displayTime >= 500) {
            this.playersElevated = true;
            this.elevatePlayers();
        }

        if (!this.fireworksLaunched && this.displayTime >= 1000) {
            this.fireworksLaunched = true;
            launchFireworks(context);
        }

        if (this.displayTime >= 5000 && this.displayTime < 5000 + deltaTime) {
            broadcast("§7Retour au lobby dans §e5 secondes§7...");
        }
    }

    private void calculateAndDisplayResults(GameContext context) {
        broadcast("§6§l=============================");
        broadcast("§e§l    FIN DE LA PARTIE !");
        broadcast("§6§l=============================");

        UUID winner = null;

        for (UUID playerId : context.getAlivePlayers()) {
            winner = playerId;
            break;
        }

        if (winner != null) {
            final Player winnerPlayer = Bukkit.getPlayer(winner);

            if (winnerPlayer != null) {
                this.broadcast("§6§l⭐ VAINQUEUR: §e§l" + winnerPlayer.getName());
                this.broadcast("§a§lFélicitations !");
                this.broadcast(" ");
                this.broadcast(ChatColor.GREEN + "⏱ Durée: " + ChatColor.WHITE + ChatColor.BOLD + GameTimerManager.getInstance().getFormattedTime());

                int score = context.getScore(winner);

                this.broadcast("§7Score final: §e" + score);

                context.setData("winner", winner);

                Bukkit.getOnlinePlayers().forEach(players -> players.playSound(players.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f));
            }
        } else {
            this.broadcast("§cAucun gagnant ! Égalité ou abandon.");
        }

        this.displayLeaderboard(context);

        this.broadcast("§6§l=============================");
    }

    private void displayLeaderboard(final GameContext context) {
        final Map<UUID, Integer> scores = context.getAllScores();

        if (scores.isEmpty()) return;

        this.broadcast("");
        this.broadcast("§e§lCLASSEMENT FINAL:");

        scores.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .forEach(entry -> {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    String name = player != null ? player.getName() : "Inconnu";
                    boolean alive = context.getAlivePlayers().contains(entry.getKey());
                    String status = alive ? "§a✓" : "§c✗";

                    this.broadcast("  " + status + " §7" + name + " §8- §e" + entry.getValue() + " points");
                });

        this.broadcast("");
    }

    private void elevatePlayers() {
        Bukkit.getOnlinePlayers().forEach(players -> {
            players.setGameMode(GameMode.SPECTATOR);
            players.teleport(players.getLocation().clone().add(0, 5, 0));
            players.playSound(players.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0f, 1.5f);
        });
    }

    private void launchFireworks(final GameContext context) {
        final UUID winnerId = context.getData("winner", null);

        if (winnerId == null) return;

        final Player winner = Bukkit.getPlayer(winnerId);

        if (winner == null) return;

        final Location winnerLoc = winner.getLocation();

        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= 15 * 4) {
                    cancel();
                    return;
                }

                double offsetX = (Math.random() - 0.5) * 4;
                double offsetZ = (Math.random() - 0.5) * 4;

                final Location fireworkLoc = winnerLoc.clone().add(offsetX, 0, offsetZ);

                launchSingleFirework(fireworkLoc);
                count++;
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("LeagueUHC"), 0L, 4L);
    }

    private void launchSingleFirework(final Location location) {
        final Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        final FireworkMeta meta = firework.getFireworkMeta();

        final FireworkEffect.Type[] types = {
                FireworkEffect.Type.BALL,
                FireworkEffect.Type.BALL_LARGE,
                FireworkEffect.Type.BURST,
                FireworkEffect.Type.STAR
        };

        final Color[] colors = {
                Color.RED, Color.YELLOW, Color.LIME, Color.AQUA,
                Color.BLUE, Color.FUCHSIA, Color.ORANGE, Color.WHITE
        };

        final FireworkEffect effect = FireworkEffect.builder()
                .with(types[(int) (Math.random() * types.length)])
                .withColor(colors[(int) (Math.random() * colors.length)])
                .withFade(colors[(int) (Math.random() * colors.length)])
                .flicker(Math.random() > 0.5)
                .trail(Math.random() > 0.5)
                .build();

        meta.addEffect(effect);
        meta.setPower((int) (Math.random() * 2) + 1);
        firework.setFireworkMeta(meta);
    }
}