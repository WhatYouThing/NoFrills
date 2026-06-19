package nofrills.features.farming;

import com.google.common.collect.Sets;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.ScreenRenderEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.HashSet;

public class EquipmentHighlight {
    public static final Feature instance = new Feature("equipmentHighlight");

    public static final SettingColor farmColor = new SettingColor(RenderColor.fromHex(0xFFAA00), "farmColor", instance);
    public static final SettingColor pestColor = new SettingColor(RenderColor.fromHex(0x00AA00), "pestColor", instance);

    private static final HashSet<String> farmEquipment = Sets.newHashSet(
            "LOTUS_BRACELET",
            "LOTUS_BELT",
            "LOTUS_NECKLACE",
            "LOTUS_CLOAK",
            "BLOSSOM_BRACELET",
            "BLOSSOM_BELT",
            "BLOSSOM_NECKLACE",
            "BLOSSOM_CLOAK",
            "ZORROS_CAPE"
    );
    private static final HashSet<String> pestEquipment = Sets.newHashSet(
            "PESTHUNTERS_NECKLACE",
            "PESTHUNTERS_CLOAK",
            "PESTHUNTERS_BELT",
            "PESTHUNTERS_GLOVES",
            "PEST_VEST"
    );

    @EventHandler
    private static void onScreenRender(ScreenRenderEvent.Before event) {
        if (instance.isActive() && event.title.equals("Your Equipment and Stats") && Utils.isInGarden()) {
            for (Slot slot : event.handler.slots) {
                ItemStack stack = slot.getItem();
                if (stack.isEmpty()) continue;
                String id = Utils.getSkyblockId(stack);
                if (!id.isEmpty()) {
                    if (farmEquipment.contains(id)) {
                        event.drawFill(slot.index, farmColor.value());
                    } else if (pestEquipment.contains(id)) {
                        event.drawFill(slot.index, pestColor.value());
                    }
                }
            }
        }
    }
}
