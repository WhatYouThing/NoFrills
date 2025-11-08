package nofrills.features.kuudra;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import nofrills.config.Feature;
import nofrills.events.SlotUpdateEvent;
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
    private static void onSlot(SlotUpdateEvent event) {
        if (instance.isActive() && Utils.isInKuudra() && event.title.equals("Perk Menu")) {
            if (event.isInventory || event.stack.getItem().equals(Items.BLACK_STAINED_GLASS_PANE)) {
                return;
            }
            String name = Utils.toPlain(event.stack.getName());
            for (String garbage : garbageList) {
                if (name.startsWith(garbage)) {
                    Slot slot = event.handler.getSlot(event.slotId);
                    SlotOptions.setDisabled(slot, true);
                    SlotOptions.setSpoofed(slot, SlotOptions.BACKGROUND);
                }
            }
        }
    }
}
