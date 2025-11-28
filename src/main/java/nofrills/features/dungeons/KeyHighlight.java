package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.events.EntityNamedEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.List;

public class KeyHighlight {
    public static final Feature instance = new Feature("witherKeyHighlight");

    public static final SettingBool highlight = new SettingBool(true, "highlight", instance);
    public static final SettingBool tracer = new SettingBool(false, "tracer", instance);
    public static final SettingColor color = new SettingColor(RenderColor.fromArgb(0x8000ff00), "color", instance.key());
    public static final SettingColor tracerColor = new SettingColor(RenderColor.fromArgb(0xff00ff00), "tracerColor", instance.key());

    private static final EntityCache keyCache = new EntityCache();

    private static boolean isKey(String name) {
        return name.equals("Wither Key") || name.equals("Blood Key");
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && Utils.isInDungeons() && isKey(event.namePlain)) {
            keyCache.add(event.entity);
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && Utils.isInDungeons()) {
            List<Entity> keys = keyCache.get();
            if (!keys.isEmpty()) {
                Entity key = keys.getFirst();
                Box box = Box.of(key.getPos().add(0, 1.5, 0), 1, 1, 1);
                if (highlight.value()) event.drawFilledWithBeam(box, 256, true, color.value());
                if (tracer.value()) event.drawTracer(box.getCenter(), tracerColor.value());
            }
        }
    }
}
