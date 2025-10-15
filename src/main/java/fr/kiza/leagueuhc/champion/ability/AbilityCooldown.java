package fr.kiza.leagueuhc.champion.ability;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AbilityCooldown {

    private static final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public static boolean isOnCooldown(UUID uuid, String ability) {
        Map<String, Long> map = cooldowns.get(uuid);
        if (map == null) return false;
        return map.getOrDefault(ability, 0L) > System.currentTimeMillis();
    }

    public static void setCooldown(UUID uuid, String ability, int ticks) {
        cooldowns.computeIfAbsent(uuid, k -> new HashMap<>())
                .put(ability, System.currentTimeMillis() + ticks * 50L);
    }

    public static long getRemaining(UUID uuid, String ability) {
        Map<String, Long> map = cooldowns.get(uuid);
        if (map == null) return 0;
        return Math.max(0, map.getOrDefault(ability, 0L) - System.currentTimeMillis());
    }
}