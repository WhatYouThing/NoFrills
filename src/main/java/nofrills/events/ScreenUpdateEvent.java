package nofrills.events;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;

public class ScreenUpdateEvent {
    public InventoryS2CPacket packet;
    public Screen screen;

    public ScreenUpdateEvent(InventoryS2CPacket packet, Screen screen) {
        this.packet = packet;
        this.screen = screen;
    }
}
