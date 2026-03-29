package nofrills.events;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;

public class SlotClickEvent extends Cancellable {
    public Slot slot;
    public int slotId;
    public int button;
    public ContainerInput actionType;
    public String title;
    public AbstractContainerMenu handler;

    public SlotClickEvent(Slot slot, int slotId, int button, ContainerInput actionType, String title, AbstractContainerMenu handler) {
        this.setCancelled(false);
        this.slot = slot;
        this.slotId = slotId;
        this.button = button;
        this.actionType = actionType;
        this.title = title;
        this.handler = handler;
    }
}
