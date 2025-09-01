package nofrills.events;

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;

public class SlotUpdateEvent {
    public ScreenHandlerSlotUpdateS2CPacket packet;
    public GenericContainerScreen screen;
    public GenericContainerScreenHandler handler;
    public Inventory inventory;
    public int slotId;
    public Slot slot;
    public ItemStack stack;
    public String title;
    public boolean isFinal;
    public boolean isInventory;

    public SlotUpdateEvent(ScreenHandlerSlotUpdateS2CPacket packet, GenericContainerScreen screen, GenericContainerScreenHandler handler, int slotId) {
        this.packet = packet;
        this.screen = screen;
        this.handler = handler;
        this.inventory = handler.getInventory();
        this.slotId = slotId;
        this.slot = this.slotId >= 0 & this.slotId < this.handler.slots.size() ? this.handler.getSlot(this.slotId) : null;
        this.stack = this.inventory.getStack(this.slotId);
        this.title = screen.getTitle().getString();
        this.isFinal = packet.getSlot() == handler.slots.getLast().id;
        this.isInventory = this.stack.equals(ItemStack.EMPTY);
    }
}
