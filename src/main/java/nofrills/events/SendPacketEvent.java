package nofrills.events;

import net.minecraft.network.packet.Packet;

public class SendPacketEvent extends Cancellable {
    public Packet<?> packet;

    public SendPacketEvent(Packet<?> packet) {
        this.setCancelled(false);
        this.packet = packet;
    }
}
