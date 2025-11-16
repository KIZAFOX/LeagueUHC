package fr.kiza.leagueuhc.core.api.packets.builder;

import fr.kiza.leagueuhc.core.api.packets.PacketManager;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.entity.Player;

import java.util.Collection;

public class TitleBuilder {

    private String title, subTitle;
    private int fadeIn = 10, stay = 70, fadeOut = 20;

    public static TitleBuilder create() {
        return new TitleBuilder();
    }

    public TitleBuilder title(final String title) {
        this.title = title;
        return this;
    }

    public TitleBuilder subTitle(final String subTitle) {
        this.subTitle = subTitle;
        return this;
    }

    public TitleBuilder fadeIn(final int fadeIn) {
        this.fadeIn = fadeIn;
        return this;
    }

    public TitleBuilder stay(final int stay) {
        this.stay = stay;
        return this;
    }

    public TitleBuilder fadeOut(final int fadeOut) {
        this.fadeOut = fadeOut;
        return this;
    }

    public TitleBuilder times(final int fadeIn, final int stay, final int fadeOut) {
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
        return this;
    }

    public void send(final Player player) {
        final PacketPlayOutTitle timesPacket = new PacketPlayOutTitle(
                PacketPlayOutTitle.EnumTitleAction.TIMES,
                null, this.fadeIn, this.stay, this.fadeOut
        );
        PacketManager.sendPacket(player, timesPacket);

        if (this.title != null && !this.title.isEmpty()) {
            final IChatBaseComponent titleComponent = new ChatComponentText(this.title);
            final PacketPlayOutTitle titlePacket = new PacketPlayOutTitle(
                    PacketPlayOutTitle.EnumTitleAction.TITLE,
                    titleComponent
            );
            PacketManager.sendPacket(player, titlePacket);
        }

        if (this.subTitle != null && !this.subTitle.isEmpty()) {
            final IChatBaseComponent subTitleComponent = new ChatComponentText(this.subTitle);
            final PacketPlayOutTitle subTitlePacket = new PacketPlayOutTitle(
                    PacketPlayOutTitle.EnumTitleAction.SUBTITLE,
                    subTitleComponent
            );
            PacketManager.sendPacket(player, subTitlePacket);
        }
    }

    public void send(final Collection<Player> players) {
        players.forEach(this::send);
    }

    public static void clear(final Player player) {
        final PacketPlayOutTitle packet = new PacketPlayOutTitle(
                PacketPlayOutTitle.EnumTitleAction.CLEAR,
                null
        );
        PacketManager.sendPacket(player, packet);
    }

    public static void reset(Player player) {
        PacketPlayOutTitle packet = new PacketPlayOutTitle(
                PacketPlayOutTitle.EnumTitleAction.RESET,
                null
        );
        PacketManager.sendPacket(player, packet);
    }
}
