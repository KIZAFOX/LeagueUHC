package fr.kiza.leagueuhc.packets.builder;

import fr.kiza.leagueuhc.packets.PacketManager;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerListHeaderFooter;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Collection;

public class TablistBuilder {

    private String header, footer;

    public static TablistBuilder create() {
        return new TablistBuilder();
    }

    public TablistBuilder header(final String header) {
        this.header = header;
        return this;
    }

    public TablistBuilder footer(final String footer) {
        this.footer = footer;
        return this;
    }

    public void send(Player player) {
        final IChatBaseComponent headerComponent = header != null ? new ChatComponentText(header) : new ChatComponentText("");
        final IChatBaseComponent footerComponent = footer != null ? new ChatComponentText(footer) : new ChatComponentText("");

        final PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();

        try {
            final Field headerField = packet.getClass().getDeclaredField("a");
            headerField.setAccessible(true);
            headerField.set(packet, headerComponent);

            final Field footerField = packet.getClass().getDeclaredField("b");
            footerField.setAccessible(true);
            footerField.set(packet, footerComponent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        PacketManager.sendPacket(player, packet);
    }

    public void send(Collection<? extends Player> players) {
        players.forEach(this::send);
    }
}