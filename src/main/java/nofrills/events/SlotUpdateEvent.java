package nofrills.events;

import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;

public class SlotUpdateEvent {
    public ClientboundContainerSetSlotPacket packet;
    public ContainerScreen screen;
    public ChestMenu handler;
    public Container inventory;
    public int slotId;
    public Slot slot;
    public ItemStack stack;
    public String title;
    public boolean isFinal;
    public boolean isInventory;

    public SlotUpdateEvent(ClientboundContainerSetSlotPacket packet, ContainerScreen screen, ChestMenu handler, int slotId) {
        this.packet = packet;
        this.screen = screen;
        this.handler = handler;
        this.inventory = handler.getContainer();
        this.slotId = slotId;
        this.slot = this.slotId >= 0 & this.slotId < this.handler.slots.size() ? this.handler.getSlot(this.slotId) : null;
        this.stack = this.inventory.getItem(this.slotId);
        this.title = screen.getTitle().getString();
        this.isFinal = packet.getSlot() == handler.slots.getLast().index;
        this.isInventory = this.stack.equals(ItemStack.EMPTY);
    }
}
