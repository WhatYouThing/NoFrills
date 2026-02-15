package nofrills.features.kuudra;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.mob.MagmaCubeEntity;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.KuudraUtil;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

public class KuudraHitbox {
    public static final Feature instance = new Feature("kuudraHitbox");

    public static final SettingColor color = new SettingColor(RenderColor.fromHex(0xffff00), "color", instance.key());

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && Utils.isInKuudra()) {
            MagmaCubeEntity kuudra = KuudraUtil.getKuudraEntity();
            if (kuudra != null) {
                event.drawOutline(Utils.getLerpedBox(kuudra, event.tickCounter.getTickProgress(true)), false, color.value());
            }
        }
    }
}