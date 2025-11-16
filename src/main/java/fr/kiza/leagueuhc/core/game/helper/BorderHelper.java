package fr.kiza.leagueuhc.core.game.helper;

import fr.kiza.leagueuhc.core.api.packets.PacketManager;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

public class BorderHelper {

    private static final double CENTER_X = 0;
    private static final double CENTER_Z = 0;

    /**
     * Initialise la bordure du monde UHC.
     *
     * @param size Taille de la bordure (diamètre total)
     */
    public static void initialize(final double size) {
        final World bukkitWorld = Bukkit.getWorld("uhc_world");
        if (bukkitWorld == null) {
            Bukkit.getLogger().warning("[BorderHelper] Le monde 'uhc_world' est introuvable !");
            return;
        }

        final WorldServer nmsWorld = ((CraftWorld) bukkitWorld).getHandle();
        final WorldBorder border = nmsWorld.getWorldBorder();

        border.setCenter(CENTER_X, CENTER_Z);
        border.setSize(size);
        border.setWarningTime(0);
        border.setWarningDistance(0);

        final PacketPlayOutWorldBorder packet = new PacketPlayOutWorldBorder(
                border,
                PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE
        );

        // Envoie uniquement aux joueurs dans le monde UHC
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getWorld().equals(bukkitWorld))
                .forEach(p -> PacketManager.sendPacket(p, packet));
    }

    /**
     * Réinitialise la bordure du monde (retour à la configuration vanilla).
     */
    public static void reset() {
        initialize(6.0E7D);
    }

    /**
     * Met à jour dynamiquement la taille de la bordure.
     *
     * @param newSize Nouvelle taille de la bordure
     * @param transition Durée de la transition en secondes
     */
    public static void updateSize(final double newSize, final long transition) {
        final World bukkitWorld = Bukkit.getWorld("uhc_world");
        if (bukkitWorld == null) return;

        final WorldServer nmsWorld = ((CraftWorld) bukkitWorld).getHandle();
        final WorldBorder border = nmsWorld.getWorldBorder();

        border.transitionSizeBetween(border.getSize(), newSize, transition * 1000L);

        final PacketPlayOutWorldBorder packet = new PacketPlayOutWorldBorder(
                border,
                PacketPlayOutWorldBorder.EnumWorldBorderAction.LERP_SIZE
        );

        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getWorld().equals(bukkitWorld))
                .forEach(p -> PacketManager.sendPacket(p, packet));
    }
}
