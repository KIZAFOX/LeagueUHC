package fr.kiza.leagueuhc.core.api.packets.builder;

import fr.kiza.leagueuhc.core.api.packets.PacketManager;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;

public class ParticleBuilder {

    private EnumParticle particle;
    private Location location;
    private float offsetX = 0, offsetY = 0, offsetZ = 0, speed = 0;
    private int count = 1;
    private boolean longDistance = true;

    public static ParticleBuilder create() {
        return new ParticleBuilder();
    }

    public ParticleBuilder type(final EnumParticle particle) {
        this.particle = particle;
        return this;
    }

    public ParticleBuilder location(final Location location) {
        this.location = location;
        return this;
    }

    public ParticleBuilder offset(final float x, final float y, final float z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
        return this;
    }

    public ParticleBuilder speed(final float speed) {
        this.speed = speed;
        return this;
    }

    public ParticleBuilder count(final int count) {
        this.count = count;
        return this;
    }

    public ParticleBuilder longDistance(final boolean longDistance) {
        this.longDistance = longDistance;
        return this;
    }

    public void send(final Player player) {
        final PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
                particle,
                longDistance,
                (float) location.getX(),
                (float) location.getY(),
                (float) location.getZ(),
                offsetX, offsetY, offsetZ,
                speed,
                count
        );
        PacketManager.sendPacket(player, packet);
    }

    public void send(final Collection<? extends Player> players) {
        final PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
                particle,
                longDistance,
                (float) location.getX(),
                (float) location.getY(),
                (float) location.getZ(),
                offsetX, offsetY, offsetZ,
                speed,
                count
        );
        PacketManager.sendPacket(players, packet);
    }

    public void sendInRadius(final double radius) {
        final PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
                particle,
                longDistance,
                (float) location.getX(),
                (float) location.getY(),
                (float) location.getZ(),
                offsetX, offsetY, offsetZ,
                speed,
                count
        );
        PacketManager.sendPacketInRadius(location, radius, packet);
    }
}
