package nofrills.features.mining;

import com.google.common.collect.Sets;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import nofrills.config.Feature;
import nofrills.events.BlockUpdateEvent;
import nofrills.misc.SkyblockData;

import java.util.HashSet;

import static net.minecraft.world.level.block.CrossCollisionBlock.*;
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
        return state.getValue(NORTH) || state.getValue(EAST) || state.getValue(SOUTH) || state.getValue(WEST);
    }

    public static BlockState asFullPane(BlockState state) {
        return state.setValue(NORTH, true).setValue(EAST, true).setValue(SOUTH, true).setValue(WEST, true);
    }

    @EventHandler
    private static void onBlock(BlockUpdateEvent event) {
        if (active() && event.newState.isAir() && isStainedGlass(event.oldState)) {
            event.newState.updateNeighbourShapes(mc.level, event.pos, Block.UPDATE_ALL);
        }
    }
}
