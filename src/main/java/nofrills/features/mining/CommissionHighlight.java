package nofrills.features.mining;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.ScreenRenderEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

public class CommissionHighlight {
    public static final Feature instance = new Feature("commissionHighlight");

    public static final SettingColor color = new SettingColor(RenderColor.fromHex(0x55FF55), "color", instance);

    @EventHandler
    private static void onScreenRender(ScreenRenderEvent.Before event) {
        if (instance.isActive() && event.title.equals("Commissions")) {
            for (Slot slot : Utils.getContainerSlots(event.handler)) {
                ItemStack stack = slot.getStack();
                if (stack.isEmpty() || !stack.getItem().equals(Items.WRITABLE_BOOK) || !Utils.getSkyblockId(stack).isEmpty())
                    continue;
                for (String line : Utils.getLoreLines(stack)) {
                    if (line.equals("COMPLETED")) {
                        event.drawFill(slot.id, color.value());
                    }
                }
            }
        }
    }
}
