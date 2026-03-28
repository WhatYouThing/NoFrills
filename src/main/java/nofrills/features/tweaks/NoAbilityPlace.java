package nofrills.features.tweaks;

import com.google.common.collect.Sets;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import nofrills.config.Feature;
import nofrills.misc.Utils;

import java.util.HashSet;

public class NoAbilityPlace {
    public static final Feature instance = new Feature("noAbilityPlace");

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

    public static boolean hasAbility(ItemPlacementContext context) {
        if (context != null) {
            ItemStack stack = context.getStack();
            String id = Utils.getSkyblockId(stack);
            if (!id.isEmpty()) {
                if (abilityWhitelist.contains(id) || id.startsWith("ABIPHONE")) {
                    return true;
                }
            }
            return stack.getItem() instanceof BlockItem && Utils.hasRightClickAbility(stack);
        }
        return false;
    }
}