package fr.kiza.leagueuhc.core.state.states;

import fr.kiza.leagueuhc.core.context.GameContext;
import fr.kiza.leagueuhc.core.input.GameInput;
import fr.kiza.leagueuhc.core.input.InputType;
import fr.kiza.leagueuhc.core.state.BaseGameState;
import fr.kiza.leagueuhc.core.state.GameState;
import fr.kiza.leagueuhc.utils.ItemBuilder;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.Objects;

public class FinishedState extends BaseGameState {

    private boolean opsRestored = false;

    public FinishedState() {
        super(GameState.FINISHED.getName());
    }

    @Override
    public void onEnter(GameContext context) {
        this.opsRestored = false;

        final long entryTime = System.currentTimeMillis();
        context.setData("finishedTime", entryTime);

        this.broadcast(ChatColor.GOLD + "" + ChatColor.BOLD + "==========================");
        this.broadcast(ChatColor.YELLOW + "" + ChatColor.BOLD + "PARTIE TERMINÉE");
        this.broadcast(ChatColor.GOLD + "" + ChatColor.BOLD + "==========================");
        this.broadcast("");

        new BukkitRunnable() {
            @Override
            public void run() {
                if (opsRestored) return;
                opsRestored = true;

                broadcast(ChatColor.YELLOW + "" + ChatColor.BOLD + "Préparation du lobby...");

                Bukkit.getOnlinePlayers().forEach(players -> setupPlayerForLobby(players));

                broadcast(ChatColor.GREEN + "Lobby prêt ! Les hosts peuvent relancer une partie.");
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("LeagueUHC"), 200L); // 10 secondes
    }

    @Override
    public void onExit(GameContext context) {
        this.broadcast(ChatColor.GREEN + "Redémarrage de la partie...");
    }

    @Override
    public void update(GameContext context, long deltaTime) { }

    @Override
    public void handleInput(GameContext context, GameInput input) {
        if (Objects.requireNonNull(input.getType()) == InputType.PLAYER_JOIN) {
            final Player player = input.getPlayer();

            this.setupPlayerForLobby(player);

            player.sendMessage(ChatColor.YELLOW + "La partie précédente est terminée.");
            player.sendMessage(ChatColor.GRAY + "Retour au lobby dans quelques secondes...");
        }
    }

    private void setupPlayerForLobby(Player player) {
        if (player.isOp()) {
            player.setGameMode(GameMode.CREATIVE);
            player.teleport(new Location(player.getWorld(), 0, 100, 0));
            player.getInventory().clear();

            player.getInventory().setItem(8,
                    new ItemBuilder(Material.REDSTONE_TORCH_ON)
                            .setName(ChatColor.RED + "" + ChatColor.BOLD + "Settings")
                            .setLore(Collections.singletonList(ChatColor.GRAY + "Clic droit pour ouvrir les paramètres"))
                            .toItemStack()
            );

            player.sendMessage(ChatColor.GREEN + "✔ Vous pouvez maintenant redémarrer une partie !");
        } else {
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(new org.bukkit.Location(player.getWorld(), 0, 100, 0));
        }
    }
}