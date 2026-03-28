package nofrills.events;

import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;


public class PlaySoundEvent extends Cancellable {
    public ClientboundSoundPacket packet;

    public PlaySoundEvent(ClientboundSoundPacket packet) {
        this.setCancelled(false);
        this.packet = packet;
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
}
