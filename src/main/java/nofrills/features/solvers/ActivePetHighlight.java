package nofrills.features.solvers;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.item.ItemStack;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.EventListener;
import nofrills.events.SlotUpdateEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.SlotOptions;
import nofrills.misc.Utils;

@EventListener
public class ActivePetHighlight {
    public static final Feature instance = new Feature("activePetHighlight");

    public static final SettingColor color = new SettingColor(RenderColor.GREEN, "color", instance);

    private static boolean isEquippedPet(ItemStack stack) {
        return Utils.getSkyblockId(stack).equals("PET") && Utils.getLoreLines(stack).contains("Click to despawn!");
    }

    @EventHandler
    private static void onSlotUpdate(SlotUpdateEvent event) {
        if (instance.isActive() && event.slot != null && !event.isInventory && Utils.isPaginatedMenu(event.title, "Pets") && isEquippedPet(event.stack)) {
            SlotOptions.setBackground(event.slot, color.value());
        }
    }
}
