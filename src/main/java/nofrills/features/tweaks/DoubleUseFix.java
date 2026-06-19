package nofrills.features.tweaks;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.HitResult;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.InteractBlockEvent;
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
        if (active() && mc.hitResult != null && mc.hitResult.getType().equals(HitResult.Type.BLOCK) && getDisableType().equals(type.Dagger)) {
            event.cancel();
        }
    }

    @EventHandler
    private static void onUseBlock(InteractBlockEvent event) {
        if (active() && getDisableType().equals(type.Rod)) {
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            mc.player.swing(InteractionHand.MAIN_HAND);
            event.cancel();
        }
    }

    private enum type {
        Dagger,
        Rod,
        None
    }
}
