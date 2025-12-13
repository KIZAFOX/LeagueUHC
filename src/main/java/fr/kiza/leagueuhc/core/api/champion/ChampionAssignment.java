package fr.kiza.leagueuhc.core.api.champion;

import fr.kiza.leagueuhc.core.game.GamePlayer;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Utilitaire pour assigner des champions aux joueurs.
 * Utilise ChampionManager pour l'assignation réelle.
 */
public final class ChampionAssignment {

    private ChampionAssignment() {}

    /**
     * Assigne aléatoirement des champions uniques aux joueurs.
     *
     * @param players             Liste des joueurs à qui assigner des champions
     * @param availableChampions  Liste des champions disponibles
     * @return Map des assignations (UUID -> Champion)
     */
    public static Map<UUID, Champion> assignRandomly(List<Player> players, List<Champion> availableChampions) {
        Map<UUID, Champion> assignments = new HashMap<>();

        if (players == null || players.isEmpty()) {
            return assignments;
        }

        if (availableChampions == null || availableChampions.isEmpty()) {
            players.forEach(p -> p.sendMessage(ChatColor.RED + "Aucun champion disponible !"));
            return assignments;
        }

        // Mélanger les deux listes
        List<Player> shuffledPlayers = new ArrayList<>(players);
        List<Champion> shuffledChampions = new ArrayList<>(availableChampions);
        Collections.shuffle(shuffledPlayers);
        Collections.shuffle(shuffledChampions);

        ChampionManager manager = ChampionManager.getInstance();

        int championsCount = shuffledChampions.size();
        int playersCount = shuffledPlayers.size();

        for (int i = 0; i < playersCount; i++) {
            Player player = shuffledPlayers.get(i);
            GamePlayer gamePlayer = GamePlayer.getOrCreate(player);

            if (i < championsCount) {
                Champion champion = shuffledChampions.get(i);

                // Utiliser le manager pour une assignation complète
                if (manager != null) {
                    manager.assignChampion(gamePlayer, champion);
                } else {
                    // Fallback si manager non initialisé
                    gamePlayer.setChampion(champion);
                    sendAssignmentMessage(player, champion);
                }

                assignments.put(player.getUniqueId(), champion);
            } else {
                sendNoChampionMessage(player);
            }
        }

        // Message récapitulatif
        broadcastAssignmentSummary(players, assignments.size(), playersCount - assignments.size());

        return assignments;
    }

    /**
     * Assigne des champions depuis le ChampionRegistry.
     */
    public static Map<UUID, Champion> assignFromRegistry(List<Player> players) {
        return assignRandomly(players, new ArrayList<>(ChampionRegistry.getChampions()));
    }

    /**
     * Assigne des champions d'une catégorie spécifique.
     */
    public static Map<UUID, Champion> assignByCategory(List<Player> players, Champion.Category category) {
        List<Champion> categoryChampions = ChampionRegistry.getChampionsByCategory(category);
        return assignRandomly(players, categoryChampions);
    }

    /**
     * Assigne des champions d'une région spécifique.
     */
    public static Map<UUID, Champion> assignByRegion(List<Player> players, Champion.Region region) {
        List<Champion> regionChampions = ChampionRegistry.getChampionsByRegion(region);
        return assignRandomly(players, regionChampions);
    }

    /**
     * Assigne uniquement des champions Solitaires.
     */
    public static Map<UUID, Champion> assignSoloChampions(List<Player> players) {
        return assignByRegion(players, Champion.Region.SOLITAIRE);
    }

    /**
     * Assigne uniquement des champions Duo.
     */
    public static Map<UUID, Champion> assignDuoChampions(List<Player> players) {
        return assignByRegion(players, Champion.Region.DUO);
    }

    /**
     * Assigne un champion spécifique à un joueur.
     *
     * @return true si l'assignation a réussi
     */
    public static boolean assignSpecific(Player player, Champion champion) {
        if (player == null || champion == null) {
            return false;
        }

        GamePlayer gamePlayer = GamePlayer.getOrCreate(player);
        ChampionManager manager = ChampionManager.getInstance();

        if (manager != null) {
            manager.assignChampion(gamePlayer, champion);
        } else {
            gamePlayer.setChampion(champion);
            sendAssignmentMessage(player, champion);
        }

        return true;
    }

    /**
     * Assigne un champion spécifique à un joueur par nom.
     */
    public static boolean assignSpecific(Player player, String championName) {
        Champion champion = ChampionRegistry.getChampion(championName);
        if (champion == null) {
            player.sendMessage(ChatColor.RED + "Champion introuvable: " + championName);
            return false;
        }
        return assignSpecific(player, champion);
    }

    /**
     * Retire le champion d'un joueur.
     */
    public static void removeChampion(Player player) {
        if (player == null) return;

        GamePlayer gamePlayer = GamePlayer.get(player);
        if (gamePlayer == null) return;

        ChampionManager manager = ChampionManager.getInstance();
        if (manager != null) {
            manager.revokeChampion(gamePlayer);
        } else {
            gamePlayer.setChampion(null);
            player.sendMessage(ChatColor.YELLOW + "Votre champion a été retiré.");
        }
    }

    /**
     * Vérifie si un joueur a un champion assigné.
     */
    public static boolean hasChampion(Player player) {
        GamePlayer gamePlayer = GamePlayer.get(player);
        return gamePlayer != null && gamePlayer.hasChampion();
    }

    /**
     * Récupère le champion d'un joueur.
     */
    public static Champion getChampion(Player player) {
        GamePlayer gamePlayer = GamePlayer.get(player);
        return gamePlayer != null ? gamePlayer.getChampion() : null;
    }

    /**
     * Récupère tous les champions actuellement assignés.
     */
    public static Map<UUID, Champion> getAssignedChampions(Collection<Player> players) {
        Map<UUID, Champion> assigned = new HashMap<>();

        for (Player player : players) {
            GamePlayer gamePlayer = GamePlayer.get(player);
            if (gamePlayer != null && gamePlayer.hasChampion()) {
                assigned.put(player.getUniqueId(), gamePlayer.getChampion());
            }
        }

        return assigned;
    }

    /**
     * Retire tous les champions de tous les joueurs.
     */
    public static void removeAllChampions(Collection<Player> players) {
        for (Player player : players) {
            removeChampion(player);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MESSAGES
    // ═══════════════════════════════════════════════════════════════════════════

    private static void sendAssignmentMessage(Player player, Champion champion) {
        player.sendMessage(ChatColor.GOLD + "══════════════════════════════════════");
        player.sendMessage(ChatColor.GREEN + "✔ Champion assigné: " + ChatColor.YELLOW + champion.getName());
        player.sendMessage(champion.getRegion().getColoredName() + ChatColor.GRAY + " • " + champion.getCategory().getColoredName());
        player.sendMessage(ChatColor.GRAY + champion.getDescription());
        player.sendMessage(ChatColor.GOLD + "══════════════════════════════════════");
    }

    private static void sendNoChampionMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "══════════════════════════════════════");
        player.sendMessage(ChatColor.RED + "⚠ Aucun champion disponible pour vous !");
        player.sendMessage(ChatColor.GRAY + "Vous jouerez sans champion.");
        player.sendMessage(ChatColor.GOLD + "══════════════════════════════════════");
    }

    private static void broadcastAssignmentSummary(List<Player> players, int assigned, int unassigned) {
        StringBuilder message = new StringBuilder();
        message.append(ChatColor.GREEN).append("Champions assignés: ").append(assigned).append("/").append(players.size());

        if (unassigned > 0) {
            message.append(ChatColor.RED).append(" (").append(unassigned).append(" joueur(s) sans champion)");
        }

        String finalMessage = message.toString();
        players.forEach(p -> p.sendMessage(finalMessage));
    }
}