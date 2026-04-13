package nofrills.features.mining;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.events.InventoryUpdateEvent;
import nofrills.mixin.ClientPlayerInteractionManagerAccessor;
import nofrills.mixin.HeldItemRendererAccessor;

import static nofrills.Main.mc;

public class BreakResetFix {
    public static final Feature instance = new Feature("breakResetFix");

    @EventHandler
    private static void onInventory(InventoryUpdateEvent event) {
        if (instance.isActive() && mc.player != null && mc.interactionManager != null) {
            if (event.slotId >= 36 && event.slotId <= 44 && mc.player.getInventory().getSelectedSlot() == event.slotId - 36) {
                ((ClientPlayerInteractionManagerAccessor) mc.interactionManager).setStack(event.stack);
                ((HeldItemRendererAccessor) mc.getEntityRenderDispatcher().getHeldItemRenderer()).setStack(event.stack);
            } // manually update the variable once the server updates our held item, prevents the mismatch and thus fixes the break cancel
        }
    }
}
