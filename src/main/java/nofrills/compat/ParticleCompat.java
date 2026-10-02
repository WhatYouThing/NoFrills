package nofrills.compat;

import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;

/** Preserves legacy isotropic speed matching against the new per-axis packet record. */
public final class ParticleCompat {
    private ParticleCompat() {}
    public static boolean hasSpeed(ClientboundLevelParticlesPacket packet, float speed) {
        return packet.xMaxSpeed() == speed && packet.yMaxSpeed() == speed && packet.zMaxSpeed() == speed;
    }
    public static String describeSpeed(ClientboundLevelParticlesPacket packet) {
        return packet.xMaxSpeed() == packet.yMaxSpeed() && packet.yMaxSpeed() == packet.zMaxSpeed()
            ? Float.toString(packet.xMaxSpeed())
            : packet.xMaxSpeed() + ", " + packet.yMaxSpeed() + ", " + packet.zMaxSpeed();
    }
}
