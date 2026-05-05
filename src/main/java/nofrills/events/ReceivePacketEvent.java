package nofrills.events;

import net.minecraft.network.packet.Packet;

public final class ReceivePacketEvent extends Cancellable {
    public Packet<?> packet;

    public ReceivePacketEvent(Packet<?> packet) {
        this.setCancelled(false);
        this.packet = packet;
    }
}
