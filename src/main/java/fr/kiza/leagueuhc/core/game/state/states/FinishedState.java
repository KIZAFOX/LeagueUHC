package fr.kiza.leagueuhc.core.game.state.states;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.game.context.GameContext;
import fr.kiza.leagueuhc.core.game.input.GameInput;
import fr.kiza.leagueuhc.core.game.state.BaseGameState;
import fr.kiza.leagueuhc.core.game.state.GameState;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FinishedState extends BaseGameState {

    public FinishedState() {
        super(GameState.FINISHED.getName());
    }

    @Override
    public void onEnter(GameContext context) {
        final long entryTime = System.currentTimeMillis();
        context.setData("finishedTime", entryTime);

        Bukkit.getOnlinePlayers().forEach(this::sendPlayerToHub);

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
    public void onExit(GameContext context) {
    }

    @Override
    public void update(GameContext context, long deltaTime) {
    }

    private void sendPlayerToHub(Player player) {
        if (player.getServer().getMessenger().isOutgoingChannelRegistered(LeagueUHC.getInstance(), "BungeeCord")) {
            try {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(b);

                out.writeUTF("Connect");
                out.writeUTF("hub");

                player.sendPluginMessage(LeagueUHC.getInstance(), "BungeeCord", b.toByteArray());
            } catch (IOException e) {
                player.kickPlayer("§aRetour au hub !");
            }
        } else {
            player.kickPlayer("§aPartie terminée !\n§7Merci d'avoir joué !");
        }
    }
}