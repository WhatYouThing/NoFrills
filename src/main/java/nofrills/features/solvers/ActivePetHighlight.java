package nofrills.features.solvers;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.SlotUpdateEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.SlotOptions;
import nofrills.misc.Utils;

import java.util.regex.Pattern;

public class ActivePetHighlight {
    public static final Feature instance = new Feature("activePetHighlight");

    public static final SettingColor color = new SettingColor(RenderColor.green, "color", instance);

    private static final Pattern titlePattern = Pattern.compile("(.*/.*) Pets");

    private static boolean isEquippedPet(ItemStack stack) {
        return Utils.getSkyblockId(stack).equals("PET") && Utils.getLoreLines(stack).contains("Click to despawn!");
    }

    @EventHandler
    private static void onScreenRender(SlotUpdateEvent event) {
        if (instance.isActive() && event.slot != null && !event.isInventory && titlePattern.matcher(event.title).matches() && isEquippedPet(event.stack)) {
            SlotOptions.setBackground(event.slot, color.value());
        }
    }
}
