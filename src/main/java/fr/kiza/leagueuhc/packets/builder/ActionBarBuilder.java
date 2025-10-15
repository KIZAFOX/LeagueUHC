package fr.kiza.leagueuhc.packets.builder;

import fr.kiza.leagueuhc.packets.PacketManager;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.entity.Player;

import java.util.Collection;

public class ActionBarBuilder {

    private String message;

    public static ActionBarBuilder create() {
        return new ActionBarBuilder();
    }

    public ActionBarBuilder message(final String message) {
        this.message = message;
        return this;
    }

    public void send(final Player player) {
        final IChatBaseComponent component = new ChatComponentText(this.message);
        final PacketPlayOutChat packet = new PacketPlayOutChat(component, (byte) 2);
        PacketManager.sendPacket(player, packet);
    }

    public void send(final Iterable<? extends Player> players) {
        final IChatBaseComponent component = new ChatComponentText(this.message);
        final PacketPlayOutChat packet = new PacketPlayOutChat(component, (byte) 2);
        PacketManager.sendPacket(players, packet);
    }
}
