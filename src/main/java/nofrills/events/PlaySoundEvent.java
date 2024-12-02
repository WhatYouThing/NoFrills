package nofrills.events;

import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvent;

public class PlaySoundEvent extends Cancellable {
    public PlaySoundS2CPacket packet;

    /**
     * Returns true if the SoundEvent from the packet matches the provided SoundEvent.
     */
    public boolean isSound(SoundEvent sound) {
        return packet.getSound().value().getId().equals(sound.getId());
    }

    public PlaySoundEvent(PlaySoundS2CPacket packet) {
        this.setCancelled(false);
        this.packet = packet;
    }
}
