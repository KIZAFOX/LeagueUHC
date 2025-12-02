package fr.kiza.leagueuhc.utils;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.kiza.leagueuhc.LeagueUHC;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class PluginMessage implements PluginMessageListener {

    private static final String CHANNEL = "leagueuhc:whitelist";
    private final LeagueUHC plugin;

    public PluginMessage(LeagueUHC plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] msg) {
        if (!channel.equals("BungeeCord")) {
            plugin.getLogger().warning("Canal ignoré: " + channel);
            return;
        }

        try {
            ByteArrayDataInput in = ByteStreams.newDataInput(msg);
            String subchannel = in.readUTF();

            // Vérifier que c'est un message Forward
            if (!subchannel.equals("Forward")) {
                return;
            }

            // Lire le canal de destination
            String targetServer = in.readUTF(); // "uhc-server"
            String forwardChannel = in.readUTF();

            if (!forwardChannel.equals(CHANNEL)) {
                return;
            }

            // Lire le message
            short len = in.readShort();
            byte[] msgBytes = new byte[len];
            in.readFully(msgBytes);

            ByteArrayDataInput msgIn = ByteStreams.newDataInput(msgBytes);
            String request = msgIn.readUTF();

            plugin.getLogger().info("Requête reçue: " + request);

            if (request.equals("REQUEST_WHITELIST")) {
                // Vérifier s'il y a des joueurs sur le serveur
                Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

                if (onlinePlayers.isEmpty()) {
                    plugin.getLogger().info("Aucun joueur en ligne, envoi de NO_GAME");
                    sendResponseToHub("NO_GAME");
                    return;
                }

                // Récupérer les informations de la whitelist
                boolean whitelistEnabled = Bukkit.hasWhitelist();
                plugin.getLogger().info("Whitelist activée: " + whitelistEnabled);

                String whitelistPlayers = Arrays.stream(Bukkit.getWhitelistedPlayers().toArray(new OfflinePlayer[0]))
                        .map(OfflinePlayer::getName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(","));

                if (whitelistPlayers.isEmpty()) {
                    plugin.getLogger().info("Aucun joueur whitelisté trouvé");
                }

                String response = "WHITELIST_STATUS:" + whitelistEnabled + ":" + whitelistPlayers;
                plugin.getLogger().info("Envoi de la réponse: " + response);

                sendResponseToHub(response);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Erreur lors du traitement du message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Envoie une réponse au hub via BungeeCord
     */
    private void sendResponseToHub(String message) {
        try {
            // Vérifier qu'il y a au moins un joueur pour envoyer le message
            Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

            if (onlinePlayers.isEmpty()) {
                plugin.getLogger().warning("Impossible d'envoyer la réponse: aucun joueur en ligne");
                return;
            }

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Forward");
            out.writeUTF("hub"); // Nom du serveur hub dans BungeeCord
            out.writeUTF(CHANNEL);

            ByteArrayDataOutput msgOut = ByteStreams.newDataOutput();
            msgOut.writeUTF(message);

            byte[] msgBytes = msgOut.toByteArray();
            out.writeShort(msgBytes.length);
            out.write(msgBytes);

            // Utiliser le premier joueur en ligne pour envoyer via BungeeCord
            Player sender = onlinePlayers.iterator().next();
            sender.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
            plugin.getLogger().info("Réponse envoyée au hub via " + sender.getName());
        } catch (Exception e) {
            plugin.getLogger().severe("Erreur lors de l'envoi de la réponse: " + e.getMessage());
            e.printStackTrace();
        }
    }
}