package fr.kiza.leagueuhc.core.event;

import java.util.HashMap;
import java.util.Map;

public class GameEvent {

    private final String type;
    private final long timestamp;

    private final Map<String, Object> data;

    public GameEvent(String type) {
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.data = new HashMap<>();
    }

    public String getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Object getData(String key) {
        return data.get(key);
    }

    public void setData(String key, Object value) {
        data.put(key, value);
    }

}
