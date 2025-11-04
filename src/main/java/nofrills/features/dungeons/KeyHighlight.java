package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.EntityNamedEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

public class KeyHighlight {
    public static final Feature instance = new Feature("witherKeyHighlight");

    public static final SettingColor color = new SettingColor(RenderColor.fromArgb(0x8000ff00), "color", instance.key());

    private static final EntityCache keys = new EntityCache();

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && Utils.isInDungeons()) {
            if (event.namePlain.equals("Wither Key") || event.namePlain.equals("Blood Key")) {
                keys.add(event.entity);
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && Utils.isInDungeons()) {
            for (Entity ent : keys.get()) {
                event.drawFilledWithBeam(Box.of(ent.getEntityPos().add(0, 1.5, 0), 1, 1, 1), 256, true, color.value());
            }
        }
    }
}
