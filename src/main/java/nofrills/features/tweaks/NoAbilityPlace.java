package nofrills.features.tweaks;

import com.google.common.collect.Sets;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.misc.Utils;

import java.util.HashSet;

public class NoAbilityPlace {
    public static final Feature instance = new Feature("noAbilityPlace");

    public static final SettingBool skyblockCheck = new SettingBool(false, "skyblockCheck", instance.key());
    public static final SettingBool modernCheck = new SettingBool(false, "modernCheck", instance.key());

    private static final HashSet<String> abilityWhitelist = Sets.newHashSet(
            "ABINGOPHONE",
            "SUPERBOOM_TNT",
            "INFINITE_SUPERBOOM_TNT",
            "ARROW_SWAPPER",
            "PUMPKIN_LAUNCHER",
            "SNOW_CANNON",
            "SNOW_BLASTER",
            "SNOW_HOWITZER"
    );

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

    public static boolean hasAbility(ItemPlacementContext context) {
        if (context != null) {
            ItemStack stack = context.getStack();
            String id = Utils.getSkyblockId(stack);
            if (!id.isEmpty()) {
                if (id.startsWith("ABIPHONE")) {
                    return true;
                }
                if (abilityWhitelist.contains(id)) {
                    return true;
                }
            }
            return stack.getItem() instanceof BlockItem && Utils.hasRightClickAbility(stack);
        }
        return false;
    }
}
