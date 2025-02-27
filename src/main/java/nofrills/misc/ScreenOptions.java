package nofrills.misc;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public interface ScreenOptions {
    void nofrills_mod$disableSlot(Slot slot, boolean disabled);

    void nofrills_mod$spoofSlot(Slot slot, ItemStack replacement);

    void nofrills_mod$clearSpoof(Slot slot);

    void nofrills_mod$addLeapButton(int slotId, String name, String dungeonClass, RenderColor classColor);
}