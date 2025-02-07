package nofrills.events;

import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;

public class BlockUpdateEvent {

    public BlockUpdateS2CPacket packet;

    public BlockUpdateEvent(BlockUpdateS2CPacket packet) {
        this.packet = packet;
    }
}
