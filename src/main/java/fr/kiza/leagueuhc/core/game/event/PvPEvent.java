package fr.kiza.leagueuhc.core.game.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PvPEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final boolean isEnabled;

    public PvPEvent(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}