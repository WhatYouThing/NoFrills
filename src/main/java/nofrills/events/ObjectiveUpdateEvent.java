package nofrills.events;

import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;

public class ObjectiveUpdateEvent {
    public ScoreboardObjectiveUpdateS2CPacket packet;

    public ObjectiveUpdateEvent(ScoreboardObjectiveUpdateS2CPacket packet) {
        this.packet = packet;
    }
}
