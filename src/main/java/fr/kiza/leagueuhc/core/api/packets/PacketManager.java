package fr.kiza.leagueuhc.core.api.packets;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldBorder;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PacketManager {

    /**
     * Envoie un packet à un joueur
     */
    public static void sendPacket(final Player player, final Packet<?> packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    /**
     * Envoie un packet à plusieurs joueurs
     */
    public static void sendPacket(final Iterable<? extends Player> players, final Packet<?> packet) {
        players.forEach(player -> sendPacket(player, packet));
    }

    /**
     * Envoie un packet à tous les joueurs dans un rayon
     */
    public static void sendPacketInRadius(final Location location, final double radius, final Packet<?> packet) {
        location.getWorld().getPlayers().stream()
                .filter(players -> players.getLocation().distance(location) <= radius)
                .forEach(players -> sendPacket(players, packet));
    }
}
