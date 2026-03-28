package nofrills.events;

import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;

public class InventoryUpdateEvent {
    public ClientboundContainerSetSlotPacket packet;
    public int slotId;
    public ItemStack stack;

    public InventoryUpdateEvent(ClientboundContainerSetSlotPacket packet, ItemStack stack, int slotId) {
        this.packet = packet;
        this.stack = stack;
        this.slotId = slotId;
    }
}
