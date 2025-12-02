package fr.kiza.leagueuhc.core.api.champion;

import fr.kiza.leagueuhc.core.game.GamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class ChampionAssignment {

    /**
     * Assigne aléatoirement des champions uniques aux joueurs.
     * Si le nombre de joueurs > nombre de champions, certains joueurs n'auront pas de champion.
     *
     * @param players Liste des joueurs à qui assigner des champions
     * @param availableChampions Liste des champions disponibles
     */
    public static void assignChampionsRandomly(final List<Player> players, List<Champion> availableChampions) {
        if (players == null || players.isEmpty()) {
            return;
        }

        if (availableChampions == null || availableChampions.isEmpty()) {
            players.forEach(p -> p.sendMessage(ChatColor.RED + "Aucun champion disponible !"));
            return;
        }

        List<Player> shuffledPlayers = new ArrayList<>(players);
        List<Champion> shuffledChampions = new ArrayList<>(availableChampions);

        Collections.shuffle(shuffledPlayers);
        Collections.shuffle(shuffledChampions);

        int championsCount = shuffledChampions.size();
        int playersCount = shuffledPlayers.size();

        for (int i = 0; i < playersCount; i++) {
            Player player = shuffledPlayers.get(i);
            GamePlayer gamePlayer = GamePlayer.get(player);

            if (gamePlayer == null) {
                gamePlayer = new GamePlayer(player);
            }

            if (i < championsCount) {
                Champion champion = shuffledChampions.get(i);
                gamePlayer.assignChampion(champion);

                player.sendMessage(ChatColor.GOLD + "══════════════════════════════");
                player.sendMessage(ChatColor.GREEN + "✔ Votre champion: " + ChatColor.YELLOW + champion.getName());
                player.sendMessage(ChatColor.GOLD + "══════════════════════════════");
            } else {
                player.sendMessage(ChatColor.GOLD + "══════════════════════════════");
                player.sendMessage(ChatColor.RED + "⚠ Aucun champion disponible pour vous !");
                player.sendMessage(ChatColor.GRAY + "Vous jouerez sans champion.");
                player.sendMessage(ChatColor.GOLD + "══════════════════════════════");
            }
        }

        int assignedCount = Math.min(playersCount, championsCount);
        int unassignedCount = Math.max(0, playersCount - championsCount);

        String message = ChatColor.GREEN + "Champions assignés: " + assignedCount + "/" + playersCount;
        if (unassignedCount > 0) {
            message += ChatColor.RED + " (" + unassignedCount + " joueur(s) sans champion)";
        }

        String finalMessage = message;
        players.forEach(p -> p.sendMessage(finalMessage));
    }

    /**
     * Assigne aléatoirement des champions uniques aux joueurs depuis le ChampionRegistry.
     * Si le nombre de joueurs > nombre de champions, certains joueurs n'auront pas de champion.
     *
     * @param players Liste des joueurs à qui assigner des champions
     */
    public static void assignChampionsFromRegistry(List<Player> players) {
        if (players == null || players.isEmpty()) {
            return;
        }

        List<Champion> availableChampions = new ArrayList<>(ChampionRegistry.getChampions());

        if (availableChampions == null || availableChampions.isEmpty()) {
            players.forEach(p -> p.sendMessage(ChatColor.RED + "Aucun champion disponible !"));
            return;
        }

        assignChampionsRandomly(players, availableChampions);
    }

    /**
     * Vérifie si un joueur a un champion assigné
     */
    public static boolean hasChampion(final Player player) {
        final GamePlayer gamePlayer = GamePlayer.get(player);
        return gamePlayer != null && gamePlayer.getChampion() != null;
    }

    /**
     * Récupère tous les champions actuellement assignés
     */
    public static Map<UUID, Champion> getAssignedChampions(final List<Player> players) {
        final Map<UUID, Champion> assigned = new HashMap<>();

        for (final Player player : players) {
            final GamePlayer gamePlayer = GamePlayer.get(player);

            if (gamePlayer != null && gamePlayer.getChampion() != null) {
                assigned.put(player.getUniqueId(), gamePlayer.getChampion());
            }
        }

        return assigned;
    }

    /**
     * Retire le champion d'un joueur
     */
    public static void removeChampion(final Player player) {
        final GamePlayer gamePlayer = GamePlayer.get(player);

        if (gamePlayer != null) {
            gamePlayer.setChampion(null);
            player.sendMessage(ChatColor.YELLOW + "Votre champion a été retiré.");
        }
    }
}
