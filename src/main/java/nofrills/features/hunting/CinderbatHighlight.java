package nofrills.features.hunting;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ambient.Bat;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.EntityUpdatedEvent;
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
                event.drawOutline(Utils.getLerpedBox(bat, event.tickCounter.getGameTimeDeltaPartialTick(true)), false, color.value());
            }
        }
    }

    @EventHandler
    private static void onUpdated(EntityUpdatedEvent event) {
        if (instance.isActive() && Utils.isInArea("Crimson Isle")) {
            if (event.entity instanceof Bat bat) {
                if (bat.getHealth() > 4800000.0f && bat.getMaxHealth() == 6.0f && !cinderbatList.has(event.entity)) {
                    cinderbatList.add(event.entity);
                }
            }
        }
    }
}
