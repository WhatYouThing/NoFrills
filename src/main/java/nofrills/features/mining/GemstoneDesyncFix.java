package nofrills.features.mining;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.block.StainedGlassPaneBlock;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.BlockUpdateEvent;
import nofrills.misc.Utils;

import static net.minecraft.block.HorizontalConnectingBlock.*;
import static nofrills.Main.mc;

public class GemstoneDesyncFix {
    public static final Feature instance = new Feature("gemstoneDesyncFix");

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

    public static boolean isStainedGlass(BlockState state) {
        Block block = state.getBlock();
        return block instanceof StainedGlassBlock || block instanceof StainedGlassPaneBlock;
    }

    public static boolean isDefaultPane(BlockState state) {
        return isStainedGlass(state) && !isConnectedPane(state);
    }

    public static boolean isConnectedPane(BlockState state) {
        return state.get(NORTH) || state.get(EAST) || state.get(SOUTH) || state.get(WEST);
    }

    public static BlockState asFullPane(BlockState state) {
        return state.with(NORTH, true).with(EAST, true).with(SOUTH, true).with(WEST, true);
    }

    @EventHandler
    private static void onBlock(BlockUpdateEvent event) {
        if (active() && event.newState.isAir() && isStainedGlass(event.oldState)) {
            event.newState.updateNeighbors(mc.world, event.pos, Block.NOTIFY_ALL);
        }
    }
}
