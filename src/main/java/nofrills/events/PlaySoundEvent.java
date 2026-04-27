package nofrills.events;

import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;

public class PlaySoundEvent extends Cancellable {
    public ClientboundSoundPacket packet;
    public Vec3 pos;

    public PlaySoundEvent(ClientboundSoundPacket packet) {
        this.setCancelled(false);
        this.packet = packet;
        this.pos = new Vec3(packet.getX(), packet.getY(), packet.getZ());
    }

    /**
     * Returns true if the SoundEvent from the packet matches the provided SoundEvent.
     */
    public boolean isSound(SoundEvent sound) {
        return packet.getSound().value().location().equals(sound.location());
    }

    public boolean isSound(Holder.Reference<SoundEvent> sound) {
        return this.isSound(sound.value());
    }

    public float volume() {
        return this.packet.getVolume();
    }

    public float pitch() {
        return this.packet.getPitch();
    }
}
