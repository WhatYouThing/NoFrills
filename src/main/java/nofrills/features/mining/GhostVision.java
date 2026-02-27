package nofrills.features.mining;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.math.Box;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.EntityUpdatedEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

public class GhostVision {
    public static final Feature instance = new Feature("ghostVision");

    public static final SettingColor fill = new SettingColor(RenderColor.fromHex(0x00c8c8, 0.5f), "fill", instance.key());
    public static final SettingColor outline = new SettingColor(RenderColor.fromHex(0x00c8c8, 1.0f), "outline", instance.key());

    private static final EntityCache cache = new EntityCache();

    @EventHandler
    private static void onEntity(EntityUpdatedEvent event) {
        if (instance.isActive() && event.entity instanceof CreeperEntity creeper && Utils.isInArea("Dwarven Mines")) {
            if (creeper.getEntity().getY() < 100) {
                if (creeper.isCharged()) creeper.getDataTracker().set(CreeperEntity.CHARGED, false);
                cache.add(event.entity);
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && Utils.isInArea("Dwarven Mines")) {
            for (Entity ent : cache.get()) {
                if (!ent.isAlive()) continue;
                Box box = Utils.getLerpedBox(ent, event.tickCounter.getTickProgress(true));
                event.drawFilled(box, false, fill.value());
                event.drawOutline(box, false, outline.value());
            }
        }
    }
}
