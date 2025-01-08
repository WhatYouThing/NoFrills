package nofrills.events;

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;

public class ScreenSlotUpdateEvent {
    public ScreenHandlerSlotUpdateS2CPacket packet;
    public GenericContainerScreen screen;
    public boolean isFinal;

    public ScreenSlotUpdateEvent(ScreenHandlerSlotUpdateS2CPacket packet, GenericContainerScreen screen, boolean isFinal) {
        this.packet = packet;
        this.screen = screen;
        this.isFinal = isFinal;
    }
}
