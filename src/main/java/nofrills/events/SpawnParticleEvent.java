package nofrills.events;

import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.Vec3d;

public class SpawnParticleEvent extends Cancellable {
    public ParticleS2CPacket packet;
    public ParticleType<?> type;
    public Vec3d pos;

    public SpawnParticleEvent(ParticleS2CPacket packet) {
        this.setCancelled(false);
        this.packet = packet;
        this.type = packet.getParameters().getType();
        this.pos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
    }

    public boolean matchParameters(ParticleType<?> type, int count, double speed, double offsetX, double offsetY, double offsetZ) {
        return this.type.equals(type) && this.packet.getCount() == count && this.packet.getSpeed() == (float) speed
                && this.packet.getOffsetX() == (float) offsetX && this.packet.getOffsetY() == (float) offsetY
                && this.packet.getOffsetZ() == (float) offsetZ;
    }
}
