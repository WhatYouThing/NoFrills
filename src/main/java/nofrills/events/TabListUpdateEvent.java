package nofrills.events;

import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;

import java.util.List;

public class TabListUpdateEvent {
    public PlayerListS2CPacket packet;
    public List<PlayerListS2CPacket.Entry> entries;

    public TabListUpdateEvent(PlayerListS2CPacket packet, List<PlayerListS2CPacket.Entry> entries) {
        this.packet = packet;
        this.entries = entries;
    }
}
