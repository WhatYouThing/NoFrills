package nofrills.events;

import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;

public class PlaySoundEvent extends Cancellable {
    public PlaySoundS2CPacket packet;

    public PlaySoundEvent(PlaySoundS2CPacket packet) {
        this.setCancelled(false);
        this.packet = packet;
    }
}
