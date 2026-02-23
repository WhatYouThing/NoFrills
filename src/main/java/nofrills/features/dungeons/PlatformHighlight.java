package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import nofrills.config.Feature;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.DungeonUtil;
import nofrills.misc.RenderColor;

public class PlatformHighlight {
    public static final Feature instance = new Feature("platformHighlight");

    private static final Box box = Box.enclosing(new BlockPos(55, 63, 115), new BlockPos(53, 63, 113));

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && DungeonUtil.isOnFloor("7") && DungeonUtil.isClass("Healer") && DungeonUtil.isInBossRoom()) {
            event.drawOutline(box, false, RenderColor.green);
        }
    }
}
