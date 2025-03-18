package nofrills.events;

import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleType;

public class SpawnParticleEvent extends Cancellable {
    public ParticleS2CPacket packet;
    public ParticleType<?> type;

    public SpawnParticleEvent(ParticleS2CPacket packet) {
        this.setCancelled(false);
        this.packet = packet;
        this.type = packet.getParameters().getType();
    }
}
