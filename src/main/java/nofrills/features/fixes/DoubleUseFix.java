package nofrills.features.fixes;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.InteractItemEvent;
import nofrills.misc.Utils;

import static nofrills.Main.mc;

public class DoubleUseFix {
    public static final Feature instance = new Feature("doubleUseFix");

    public static final SettingBool skyblockCheck = new SettingBool(false, "skyblockCheck", instance.key());
    public static final SettingBool modernCheck = new SettingBool(false, "modernCheck", instance.key());

    private static type getDisableType() {
        ItemStack held = Utils.getHeldItem();
        if (held.getItem().equals(Items.FISHING_ROD)) {
            return type.Rod;
        }
        if (Utils.getRightClickAbility(held).contains("Attunement")) {
            return type.Dagger;
        }
        return type.None;
    }

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

    @EventHandler
    private static void onUseItem(InteractItemEvent event) {
        if (active() && mc.crosshairTarget != null && mc.crosshairTarget.getType().equals(HitResult.Type.BLOCK)) {
            type disableType = getDisableType();
            if (!disableType.equals(type.None)) {
                if (disableType.equals(type.Rod)) {
                    mc.player.swingHand(Hand.MAIN_HAND);
                }
                event.cancel();
            }
        }
    }

    private enum type {
        Dagger,
        Rod,
        None
    }
}
