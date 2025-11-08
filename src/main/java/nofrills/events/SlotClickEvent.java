package nofrills.events;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class SlotClickEvent extends Cancellable {
    public Slot slot;
    public int slotId;
    public int button;
    public SlotActionType actionType;
    public String title;
    public ScreenHandler handler;

    public SlotClickEvent(Slot slot, int slotId, int button, SlotActionType actionType, String title, ScreenHandler handler) {
        this.setCancelled(false);
        this.slot = slot;
        this.slotId = slotId;
        this.button = button;
        this.actionType = actionType;
        this.title = title;
        this.handler = handler;
    }
}
