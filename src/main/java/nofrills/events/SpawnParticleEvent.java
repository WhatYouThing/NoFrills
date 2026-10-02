package nofrills.events;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public final class SpawnParticleEvent extends Cancellable {
    public ClientboundLevelParticlesPacket packet;
    public ParticleType<?> type;
    public Vec3 pos;

    public SpawnParticleEvent(ClientboundLevelParticlesPacket packet) {
        this.setCancelled(false);
        this.packet = packet;
        this.type = packet.particle().getType();
        this.pos = new Vec3(packet.x(), packet.y(), packet.z());
    }

    public boolean matchParameters(ParticleType<?> type, int count, double speed, double offsetX, double offsetY, double offsetZ) {
        return this.type.equals(type) && this.packet.count() == count && nofrills.compat.ParticleCompat.hasSpeed(this.packet, (float) speed)
                && this.packet.xDist() == (float) offsetX && this.packet.yDist() == (float) offsetY
                && this.packet.zDist() == (float) offsetZ;
    }

    public boolean isCurveParticle() {
        return this.matchParameters(ParticleTypes.ENCHANT, 10, -2.0f, 0.0f, 0.0f, 0.0f);
    }

    public String getParticleId() {
        Identifier identifier = BuiltInRegistries.PARTICLE_TYPE.getKey(this.type);
        return identifier != null ? identifier.toString() : "";
    }
}
