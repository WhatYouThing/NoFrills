package nofrills.events;

import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;

public class SpawnParticleEvent extends Cancellable {
    public ParticleS2CPacket packet;

    public SpawnParticleEvent(ParticleS2CPacket packet) {
        this.setCancelled(false);
        this.packet = packet;
    }
}
