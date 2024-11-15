package nofrills.misc;

import net.minecraft.item.ItemStack;

public interface ScreenOptions {
    void nofrills_mod$disableSlot(int slotId, boolean disabled);

    void nofrills_mod$disableSlot(int slotId, boolean disabled, ItemStack replacement);
}