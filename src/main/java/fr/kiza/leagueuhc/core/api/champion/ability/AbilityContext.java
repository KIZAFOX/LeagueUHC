package fr.kiza.leagueuhc.core.api.champion.ability;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import java.util.Optional;

public final class AbilityContext {

    public static final AbilityContext EMPTY = new AbilityContext(null);

    private final Event event;

    public AbilityContext(Event event) {
        this.event = event;
    }

    public Optional<BlockPlaceEvent> asBlockPlace() {
        return event instanceof BlockPlaceEvent ? Optional.of((BlockPlaceEvent) event) : Optional.empty();
    }

    public Optional<PlayerInteractEvent> asInteract() {
        return event instanceof PlayerInteractEvent ? Optional.of((PlayerInteractEvent) event) : Optional.empty();
    }

    public Optional<ProjectileLaunchEvent> asProjectileLaunch() {
        return event instanceof ProjectileLaunchEvent ? Optional.of((ProjectileLaunchEvent) event) : Optional.empty();
    }

    public Optional<Event> getEvent() {
        return Optional.ofNullable(event);
    }
}