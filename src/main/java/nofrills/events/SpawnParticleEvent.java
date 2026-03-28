package nofrills.events;

import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.phys.Vec3;

public class SpawnParticleEvent extends Cancellable {
    public ClientboundLevelParticlesPacket packet;
    public ParticleType<?> type;
    public Vec3 pos;

    public SpawnParticleEvent(ClientboundLevelParticlesPacket packet) {
        this.setCancelled(false);
        this.packet = packet;
        this.type = packet.getParticle().getType();
        this.pos = new Vec3(packet.getX(), packet.getY(), packet.getZ());
    }

    public boolean matchParameters(ParticleType<?> type, int count, double speed, double offsetX, double offsetY, double offsetZ) {
        return this.type.equals(type) && this.packet.getCount() == count && this.packet.getMaxSpeed() == (float) speed
                && this.packet.getXDist() == (float) offsetX && this.packet.getYDist() == (float) offsetY
                && this.packet.getZDist() == (float) offsetZ;
    }
}
