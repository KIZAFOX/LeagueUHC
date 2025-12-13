package fr.kiza.leagueuhc.core.game.host;

import fr.kiza.leagueuhc.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public final class HostManager {

    private static final Set<UUID> hosts = new HashSet<>();
    private static final Set<String> pendingHosts = new HashSet<>();

    private HostManager() {}

    public static void addHost(Player player) {
        hosts.add(player.getUniqueId());
        pendingHosts.remove(player.getName().toLowerCase());
        player.setWhitelisted(true);
    }

    public static void addHost(UUID uuid) {
        hosts.add(uuid);
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        offlinePlayer.setWhitelisted(true);
    }

    public static void addHostByName(String name) {
        Player online = Bukkit.getPlayer(name);
        if (online != null) {
            addHost(online);
            return;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
        if (offline.hasPlayedBefore()) {
            hosts.add(offline.getUniqueId());
            offline.setWhitelisted(true);
        } else {
            pendingHosts.add(name.toLowerCase());
        }
    }

    public static void removeHost(Player player) {
        hosts.remove(player.getUniqueId());
        pendingHosts.remove(player.getName().toLowerCase());
        player.setWhitelisted(false);
    }

    public static void removeHost(UUID uuid) {
        hosts.remove(uuid);
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        offlinePlayer.setWhitelisted(false);
    }

    public static void removeHostByName(String name) {
        Player online = Bukkit.getPlayer(name);
        if (online != null) {
            removeHost(online);
            return;
        }

        pendingHosts.remove(name.toLowerCase());

        @SuppressWarnings("deprecation")
        OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
        if (offline.hasPlayedBefore()) {
            hosts.remove(offline.getUniqueId());
            offline.setWhitelisted(false);
        }
    }

    public static void onPlayerJoin(Player player) {
        if (pendingHosts.remove(player.getName().toLowerCase())) {
            hosts.add(player.getUniqueId());
            player.setWhitelisted(true);
        }
    }

    public static boolean isHost(Player player) {
        return hosts.contains(player.getUniqueId());
    }

    public static boolean isHost(UUID uuid) {
        return hosts.contains(uuid);
    }

    public static boolean isPendingHost(String name) {
        return pendingHosts.contains(name.toLowerCase());
    }

    public static boolean hasHostPermission(Player player) {
        return isHost(player);
    }

    public static Set<UUID> getHosts() {
        return new HashSet<>(hosts);
    }

    public static Set<String> getPendingHosts() {
        return new HashSet<>(pendingHosts);
    }

    public static Player getFirstHost() {
        return hosts.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public static String getFirstHostName() {
        if (!hosts.isEmpty()) {
            UUID firstUuid = hosts.iterator().next();
            return getHostName(firstUuid);
        }
        if (!pendingHosts.isEmpty()) {
            return pendingHosts.iterator().next();
        }
        return "Aucun";
    }

    public static int getHostCount() {
        return hosts.size() + pendingHosts.size();
    }

    public static void clearHosts() {
        for (UUID uuid : hosts) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            offlinePlayer.setWhitelisted(false);
        }
        hosts.clear();
        pendingHosts.clear();
    }

    public static void clearWhitelist() {
        Bukkit.getWhitelistedPlayers().forEach(player -> player.setWhitelisted(false));
    }

    public static String getHostName(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            return player.getName();
        }
        return Bukkit.getOfflinePlayer(uuid).getName();
    }

    public static void giveItem(Player player) {
        if (isHost(player)) {
            player.getInventory().setItem(4,
                    new ItemBuilder(Material.REDSTONE_TORCH_ON)
                            .setName(ChatColor.RED + "" + ChatColor.BOLD + "Settings")
                            .setLore(Collections.singletonList(ChatColor.GRAY + "Clic droit pour les paramètres"))
                            .toItemStack()
            );
        }
    }
}