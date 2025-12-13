package fr.kiza.leagueuhc.core.game;

import fr.kiza.leagueuhc.core.api.champion.Champion;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Représente un joueur dans la partie UHC.
 * Contient les données de jeu et le champion assigné.
 */
public class GamePlayer {

    private static final Map<UUID, GamePlayer> PLAYERS = new HashMap<>();

    private final UUID uuid;
    private final String name;

    private Champion champion;
    private boolean alive = true;
    private int kills = 0;

    private double damageDealt = 0;
    private double damageTaken = 0;

    public GamePlayer(Player player) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();
        PLAYERS.put(uuid, this);
    }

    /**
     * Récupère le GamePlayer d'un joueur Bukkit.
     * Retourne null si le joueur n'est pas enregistré.
     */
    public static GamePlayer get(Player player) {
        return player == null ? null : PLAYERS.get(player.getUniqueId());
    }

    /**
     * Récupère le GamePlayer par UUID.
     */
    public static GamePlayer get(UUID uuid) {
        return PLAYERS.get(uuid);
    }

    /**
     * Récupère ou crée le GamePlayer d'un joueur.
     */
    public static GamePlayer getOrCreate(Player player) {
        GamePlayer gp = get(player);
        return gp != null ? gp : new GamePlayer(player);
    }

    /**
     * Vérifie si un joueur est enregistré.
     */
    public static boolean isRegistered(Player player) {
        return player != null && PLAYERS.containsKey(player.getUniqueId());
    }

    /**
     * Retourne tous les GamePlayers enregistrés.
     */
    public static Map<UUID, GamePlayer> getAll() {
        return new HashMap<>(PLAYERS);
    }

    /**
     * Supprime un joueur du registre.
     */
    public static void remove(Player player) {
        if (player != null) {
            PLAYERS.remove(player.getUniqueId());
        }
    }

    /**
     * Supprime un joueur du registre par UUID.
     */
    public static void remove(UUID uuid) {
        PLAYERS.remove(uuid);
    }

    /**
     * Nettoie tous les joueurs.
     */
    public static void clearAll() {
        PLAYERS.clear();
    }

    /**
     * Récupère le champion assigné.
     */
    public Champion getChampion() {
        return champion;
    }

    /**
     * Définit le champion (utilisé par ChampionManager).
     */
    public void setChampion(Champion champion) {
        this.champion = champion;
    }

    /**
     * Assigne un champion via le ChampionManager.
     * Utilise cette méthode plutôt que setChampion directement.
     */
    public void assignChampion(Champion champion) {
        // Note: Préférer utiliser ChampionManager.assignChampion()
        // pour une assignation complète avec hooks
        this.champion = champion;
    }

    /**
     * Vérifie si le joueur a un champion.
     */
    public boolean hasChampion() {
        return champion != null;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    /**
     * Récupère le joueur Bukkit.
     * Peut retourner null si le joueur est déconnecté.
     */
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    /**
     * Vérifie si le joueur est en ligne.
     */
    public boolean isOnline() {
        return getPlayer() != null;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public void addKill() {
        this.kills++;
    }

    public double getDamageDealt() {
        return damageDealt;
    }

    public void addDamageDealt(double damage) {
        this.damageDealt += damage;
    }

    public double getDamageTaken() {
        return damageTaken;
    }

    public void addDamageTaken(double damage) {
        this.damageTaken += damage;
    }

    /**
     * Réinitialise les stats du joueur.
     */
    public void resetStats() {
        this.kills = 0;
        this.damageDealt = 0;
        this.damageTaken = 0;
        this.alive = true;
    }

    /**
     * Réinitialise complètement le joueur (stats + champion).
     */
    public void reset() {
        resetStats();
        this.champion = null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GamePlayer)) return false;
        return uuid.equals(((GamePlayer) obj).uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public String toString() {
        return "GamePlayer{" +
                "name='" + name + '\'' +
                ", champion=" + (champion != null ? champion.getName() : "none") +
                ", alive=" + alive +
                ", kills=" + kills +
                '}';
    }
}