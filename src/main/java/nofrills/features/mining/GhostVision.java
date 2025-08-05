package nofrills.features.mining;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.mob.CreeperEntity;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.EntityUpdatedEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.SkyblockData;
import nofrills.mixin.CreeperEntityAccessor;

public class GhostVision {
    public static final Feature instance = new Feature("ghostVision");

    public static final SettingColor fill = new SettingColor(RenderColor.fromHex(0x00c8c8, 0.5f), "fill", instance.key());
    public static final SettingColor outline = new SettingColor(RenderColor.fromHex(0x00c8c8, 1.0f), "outline", instance.key());

    @EventHandler
    private static void onEntity(EntityUpdatedEvent event) {
        if (instance.isActive() && event.entity instanceof CreeperEntity creeper && SkyblockData.getArea().equals("Dwarven Mines")) {
            if (creeper.getPos().getY() < 100 && creeper.isCharged()) {
                creeper.getDataTracker().set(CreeperEntityAccessor.getChargedFlag(), false);
                Rendering.Entities.drawFilled(creeper, true, fill.value());
                Rendering.Entities.drawOutline(creeper, true, outline.value());
            }
        }
    }
}
