package fr.kiza.leagueuhc.core.game;

import fr.kiza.leagueuhc.core.api.champion.Champion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GamePlayer {

    private static final Map<UUID, GamePlayer> PLAYERS = new HashMap<>();

    private final Player player;
    private Champion champion;

    public GamePlayer(Player player) {
        this.player = player;
        PLAYERS.put(player.getUniqueId(), this);
    }

    public void assignChampion(Champion champion) {
        this.champion = champion;
        this.champion.onAssigned(this);
        this.player.sendMessage(ChatColor.GREEN + "Champion assigned to " + this.champion.getName());
    }

    public static GamePlayer get(Player player) {
        if (player == null) return null;
        return PLAYERS.get(player.getUniqueId());
    }

    public Player getPlayer() {
        return player;
    }

    public Champion getChampion() {
        return champion;
    }

    public void setChampion(Champion champion) {
        this.champion = champion;
    }
}
