package nofrills.events;

import net.minecraft.network.packet.s2c.play.TeamS2CPacket;

public class ScoreboardUpdateEvent {
    public TeamS2CPacket packet;

    public ScoreboardUpdateEvent(TeamS2CPacket packet) {
        this.packet = packet;
    }
}
