package nofrills.features.hunting;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.EntityUpdatedEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

public class CinderbatHighlight {
    public static final Feature instance = new Feature("cinderbatHighlight");

    public static final SettingColor color = new SettingColor(RenderColor.fromHex(0x00ff00), "color", instance.key());

    private static final EntityCache cinderbatList = new EntityCache();

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && Utils.isInArea("Crimson Isle")) {
            for (Entity bat : cinderbatList.get()) {
                Vec3d pos = bat.getLerpedPos(event.tickCounter.getTickProgress(true)).add(0, 0.45, 0);
                event.drawOutline(Box.of(pos, 2.5, 2.5, 2.5), false, color.value());
            }
        }
    }

    @EventHandler
    private static void onUpdated(EntityUpdatedEvent event) {
        if (instance.isActive() && Utils.isInArea("Crimson Isle") && event.entity instanceof BatEntity bat) {
            if (bat.getHealth() > 6.0f && !cinderbatList.has(event.entity)) {
                cinderbatList.add(event.entity);
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        cinderbatList.clear();
    }
}
