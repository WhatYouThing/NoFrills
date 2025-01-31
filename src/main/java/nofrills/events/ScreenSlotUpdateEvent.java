package nofrills.events;

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;

public class ScreenSlotUpdateEvent {
    public ScreenHandlerSlotUpdateS2CPacket packet;
    public GenericContainerScreen screen;
    public GenericContainerScreenHandler handler;
    public Inventory inventory;
    public int slotId;
    public ItemStack stack;
    public String title;
    public boolean isFinal;

    public ScreenSlotUpdateEvent(ScreenHandlerSlotUpdateS2CPacket packet, GenericContainerScreen screen, GenericContainerScreenHandler handler, Inventory inventory, int slotId, ItemStack stack, String title, boolean isFinal) {
        this.packet = packet;
        this.screen = screen;
        this.handler = handler;
        this.inventory = inventory;
        this.slotId = slotId;
        this.stack = stack;
        this.title = title;
        this.isFinal = isFinal;
    }
}
