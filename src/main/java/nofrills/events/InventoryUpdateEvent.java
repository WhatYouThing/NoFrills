package nofrills.events;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;

public class InventoryUpdateEvent {
    public ScreenHandlerSlotUpdateS2CPacket packet;
    public int slotId;
    public ItemStack stack;

    public InventoryUpdateEvent(ScreenHandlerSlotUpdateS2CPacket packet, ItemStack stack, int slotId) {
        this.packet = packet;
        this.stack = stack;
        this.slotId = slotId;
    }
}
