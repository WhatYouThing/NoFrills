package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.config.SettingDouble;
import nofrills.events.EntityNamedEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.SlayerUtil;
import nofrills.misc.Utils;

public class EggHitsDisplay {
    public static final Feature instance = new Feature("eggHitsDisplay");

    public static final SettingColor color = new SettingColor(RenderColor.fromHex(0xffffff), "color", instance);
    public static final SettingDouble scale = new SettingDouble(0.1, "scale", instance);

    private static final EntityCache cache = new EntityCache();

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && SlayerUtil.isFightingBoss(SlayerUtil.tarantula)) {
            String name = event.namePlain;
            if (name.contains("s ") && name.contains("/")) {
                String hits = name.substring(name.indexOf(" ") + 1);
                if (hits.length() == 3) cache.add(event.entity);
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && SlayerUtil.isFightingBoss(SlayerUtil.tarantula)) {
            for (Entity ent : cache.get()) {
                String name = Utils.toPlainString(ent.getName());
                event.drawText(ent.getPos(), Text.literal(name.substring(name.indexOf(" ") + 1)), scale.valueFloat(), true, color.value());
            }
        }
    }
}