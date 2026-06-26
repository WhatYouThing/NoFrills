package nofrills.features.tweaks;

import com.google.common.collect.Sets;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import nofrills.config.Feature;
import nofrills.misc.Utils;

import java.util.HashSet;

import static nofrills.misc.NoFrillsAPI.nonPlaceableItems;

public class NoGhostPlace {
    public static final Feature instance = new Feature("noGhostPlace", Feature.Flags.UseNonPlaceableAPI);

    private static final HashSet<String> abilityWhitelist = Sets.newHashSet(
            "ABINGOPHONE",
            "SUPERBOOM_TNT",
            "INFINITE_SUPERBOOM_TNT",
            "ARROW_SWAPPER",
            "PUMPKIN_LAUNCHER",
            "RED_KING_RELIC",
            "ORANGE_KING_RELIC",
            "BLUE_KING_RELIC",
            "PURPLE_KING_RELIC",
            "GREEN_KING_RELIC"
    );

    public static boolean isNonPlaceable(BlockPlaceContext context) {
        if (context != null) {
            ItemStack stack = context.getItemInHand();
            String id = Utils.getSkyblockId(stack);
            if (stack.getItem() instanceof BlockItem && !id.isEmpty()) {
                return nonPlaceableItems.contains(id) || abilityWhitelist.contains(id) || id.startsWith("ABIPHONE") || Utils.hasRightClickAbility(stack);
            }
        }
        return false;
    }
}