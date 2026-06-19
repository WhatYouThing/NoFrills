package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import nofrills.config.Feature;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.DungeonUtil;
import nofrills.misc.RenderColor;

public class PlatformHighlight {
    public static final Feature instance = new Feature("platformHighlight");

    private static final AABB box = AABB.encapsulatingFullBlocks(new BlockPos(55, 63, 115), new BlockPos(53, 63, 113));

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && DungeonUtil.isOnFloor("7") && DungeonUtil.isClass("Healer") && DungeonUtil.isInBossRoom()) {
            event.drawOutline(box, false, RenderColor.green);
        }
    }
}
