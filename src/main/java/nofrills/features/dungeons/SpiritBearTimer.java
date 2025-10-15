package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import nofrills.config.Feature;
import nofrills.events.BlockUpdateEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.ServerTickEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

public class SpiritBearTimer {
    public static final Feature instance = new Feature("spiritBearTimer");

    private static final BlockPos chargedPos = new BlockPos(7, 77, 34);
    private static int spawnTicks = 0;

    @EventHandler
    private static void onBlock(BlockUpdateEvent event) {
        if (instance.isActive() && event.pos.equals(chargedPos) && event.newState.getBlock().equals(Blocks.SEA_LANTERN) && Utils.isInDungeonBoss("4")) {
            spawnTicks = 70;
        }
    }

    @EventHandler
    private static void onWorldTick(WorldTickEvent event) {
        if (spawnTicks > 0) {
            Utils.showTitleCustom(Utils.format("BEAR: " + Utils.formatDecimal(spawnTicks / 20.0f) + "s"), 1, 25, 2.5f, RenderColor.fromHex(0xffff00));
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (spawnTicks > 0) {
            spawnTicks--;
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        spawnTicks = 0;
    }
}