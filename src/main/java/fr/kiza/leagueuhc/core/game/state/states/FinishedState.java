package fr.kiza.leagueuhc.core.game.state.states;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.game.context.GameContext;
import fr.kiza.leagueuhc.core.game.helper.pregen.PregenManager;
import fr.kiza.leagueuhc.core.game.input.GameInput;
import fr.kiza.leagueuhc.core.game.input.InputType;
import fr.kiza.leagueuhc.core.game.state.BaseGameState;
import fr.kiza.leagueuhc.core.game.state.GameState;
import fr.kiza.leagueuhc.managers.commands.CommandUHC;
import fr.kiza.leagueuhc.utils.FileUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class FinishedState extends BaseGameState {

    public FinishedState() {
        super(GameState.FINISHED.getName());
    }

    @Override
    public void onEnter(GameContext context) {
        final long entryTime = System.currentTimeMillis();
        context.setData("finishedTime", entryTime);

        Bukkit.getWhitelistedPlayers().forEach(players -> players.setWhitelisted(false));
        Bukkit.reloadWhitelist();

        Bukkit.getOnlinePlayers().forEach(this::sendPlayerToHub);

        FileUtils.deleteWorld(Bukkit.getWorld("uhc-world"));

        new BukkitRunnable() {
            @Override
            public void run() {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.shutdown();
                    }
                }.runTaskLater(LeagueUHC.getInstance(), 100L);
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("LeagueUHC"), 40L);
    }

    @Override
    public void onExit(GameContext context) { }

    @Override
    public void update(GameContext context, long deltaTime) { }

    @Override
    public void handleInput(GameContext context, GameInput input) { }

    /**
     * Envoie un joueur spécifique vers le hub via BungeeCord
     */
    private void sendPlayerToHub(Player player) {
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF("hub");

            player.sendPluginMessage(LeagueUHC.getInstance(), "BungeeCord", out.toByteArray());

            player.sendMessage(ChatColor.GREEN + "Redirection vers le hub...");
            Bukkit.getLogger().info("[LeagueUHC] " + player.getName() + " envoyé vers le hub");
        } catch (Exception e) {
            Bukkit.getLogger().warning("[LeagueUHC] Erreur lors de l'envoi de " + player.getName() + " vers le hub: " + e.getMessage());
        }
    }
}