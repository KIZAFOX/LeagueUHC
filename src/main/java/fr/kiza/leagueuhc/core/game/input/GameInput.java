package fr.kiza.leagueuhc.core.game.input;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameInput {

    private final InputType type;
    private final Player player;
    private final Event event;
    private final long timestamp;

    private final Map<String, Object> data;

    private List<ItemStack> deathDrops;

    public GameInput(InputType type, Player player, Event event) {
        this.type = type;
        this.player = player;
        this.event = event;
        this.timestamp = System.currentTimeMillis();
        this.data = new HashMap<>();
    }

    public InputType getType() {
        return type;
    }

    public Player getPlayer() {
        return player;
    }

    public Event getEvent() {
        return event;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Object getData(final String key) {
        return data.get(key);
    }

    public void setData(final String key, final Object value) {
        data.put(key, value);
    }

    public List<ItemStack> getDeathDrops() {
        return deathDrops;
    }

    public void setDeathDrops(List<ItemStack> deathDrops) {
        this.deathDrops = deathDrops;
    }
}
