package nofrills.features.tweaks;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.VerticallyAttachableBlockItem;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.misc.Utils;

public class NoSkullPlace {
    public static final Feature instance = new Feature("noSkullPlace");

    public static final SettingBool skyblockCheck = new SettingBool(false, "skyblockCheck", instance.key());
    public static final SettingBool modernCheck = new SettingBool(false, "modernCheck", instance.key());

    public static boolean active() {
        boolean isActive = instance.isActive();
        if (isActive) {
            if (skyblockCheck.value() && !Utils.isInSkyblock()) {
                return false;
            }
            if (modernCheck.value() && Utils.isOnModernIsland()) {
                return false;
            }
        }
        return isActive;
    }

    public static boolean isSkull(ItemPlacementContext context) {
        if (context != null) {
            Item item = context.getStack().getItem();
            EquippableComponent component = item.getComponents().get(DataComponentTypes.EQUIPPABLE);
            return item instanceof VerticallyAttachableBlockItem && component != null && component.slot().equals(EquipmentSlot.HEAD);
        }
        return false;
    }
}
