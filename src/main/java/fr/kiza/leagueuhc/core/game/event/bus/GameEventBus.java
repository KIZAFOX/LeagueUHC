package fr.kiza.leagueuhc.core.game.event.bus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GameEventBus {

    private static final GameEventBus INSTANCE = new GameEventBus();

    private final Map<Class<?>, List<Consumer<?>>> listeners = new HashMap<>();

    private GameEventBus() { }

    public <T> void subscribe(final Class<T> eventType, final Consumer<T> listener) {
        this.listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    public <T> void unsubscribe(final Class<T> eventType, final Consumer<T> listener) {
        final List<Consumer<?>> consumers = this.listeners.get(eventType);
        if (consumers != null) consumers.remove(listener);
    }

    public <T> void publish(final T event) {
        final List<Consumer<?>> consumers = this.listeners.get(event.getClass());
        if (consumers != null) {
            for (final Consumer<?> consumer : consumers) {
                ((Consumer<T>) consumer).accept(event);
            }
        }
    }

    public void clear() {
        this.listeners.clear();
    }

    public static GameEventBus getInstance() {
        return INSTANCE;
    }
}
