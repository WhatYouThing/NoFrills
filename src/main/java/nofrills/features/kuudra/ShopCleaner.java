package nofrills.features.kuudra;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Formatting;
import nofrills.config.Feature;
import nofrills.events.ScreenSlotUpdateEvent;
import nofrills.misc.SlotOptions;
import nofrills.misc.Utils;

import java.util.List;

public class ShopCleaner {
    public static final Feature instance = new Feature("shopCleaner");

    private static final List<String> garbageList = List.of(
            "Cannoneer",
            "Crowd Control",
            "Support",
            "Steady Hands",
            "Mining Frenzy",
            "Bomberman",
            "Auto Revive",
            "Elle's"
    );

    @EventHandler
    private static void onSlot(ScreenSlotUpdateEvent event) {
        if (instance.isActive() && Utils.isInKuudra() && !event.inventory.getStack(event.slotId).isEmpty()) {
            String name = Formatting.strip(event.stack.getName().getString());
            for (String garbage : garbageList) {
                if (name.startsWith(garbage)) {
                    Slot slot = event.handler.getSlot(event.slotId);
                    SlotOptions.disableSlot(slot, true);
                    SlotOptions.spoofSlot(slot, SlotOptions.background);
                }
            }
        }
    }
}
