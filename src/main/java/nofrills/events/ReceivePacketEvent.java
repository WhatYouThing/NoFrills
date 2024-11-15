package nofrills.events;

import net.minecraft.network.packet.Packet;

public class ReceivePacketEvent extends Cancellable {
    public Packet<?> packet;

    public ReceivePacketEvent(Packet<?> packet) {
        this.setCancelled(false);
        this.packet = packet;
    }
}
