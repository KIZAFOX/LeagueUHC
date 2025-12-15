package fr.kiza.leagueuhc.core.game.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerFreezeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final boolean frozen;
    private final Location freezeLocation;

    public PlayerFreezeEvent(Player player, boolean frozen) {
        this(player, frozen, null);
    }

    public PlayerFreezeEvent(Player player, boolean frozen, Location freezeLocation) {
        this.player = player;
        this.frozen = frozen;
        this.freezeLocation = freezeLocation;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public Location getFreezeLocation() {
        return freezeLocation;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}