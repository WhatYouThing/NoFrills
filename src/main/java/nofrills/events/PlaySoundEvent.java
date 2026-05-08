package nofrills.events;

import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;

public final class PlaySoundEvent extends Cancellable {
    public PlaySoundS2CPacket packet;
    public Vec3d pos;

    public PlaySoundEvent(PlaySoundS2CPacket packet) {
        this.setCancelled(false);
        this.packet = packet;
        this.pos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
    }

    public boolean isSound(String identifier) {
        return packet.getSound().value().id().toString().equalsIgnoreCase(identifier);
    }

    /**
     * Returns true if the SoundEvent from the packet matches the provided SoundEvent.
     */
    public boolean isSound(SoundEvent sound) {
        return this.isSound(sound.id().toString());
    }

    public boolean isSound(RegistryEntry.Reference<SoundEvent> sound) {
        return this.isSound(sound.value());
    }

    public float volume() {
        return this.packet.getVolume();
    }

    public float pitch() {
        return this.packet.getPitch();
    }
}
