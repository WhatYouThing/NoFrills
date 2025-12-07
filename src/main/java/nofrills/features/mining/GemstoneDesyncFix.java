package nofrills.features.mining;

import com.google.common.collect.Sets;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.block.StainedGlassPaneBlock;
import nofrills.config.Feature;
import nofrills.events.BlockUpdateEvent;
import nofrills.misc.SkyblockData;

import java.util.HashSet;

import static net.minecraft.block.HorizontalConnectingBlock.*;
import static nofrills.Main.mc;

public class GemstoneDesyncFix {
    public static final Feature instance = new Feature("gemstoneDesyncFix");

    private static final HashSet<String> islands = Sets.newHashSet(
            "Dwarven Mines",
            "Crystal Hollows",
            "Mineshaft",
            "Crimson Isle",
            "The Rift"
    );

    public static boolean active() {
        return instance.isActive() && islands.contains(SkyblockData.getArea());
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
