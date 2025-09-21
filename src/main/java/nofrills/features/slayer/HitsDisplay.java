package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.SlayerUtil;
import nofrills.misc.Utils;

public class HitsDisplay {
    public static final Feature instance = new Feature("hitsDisplay");

    public static final SettingColor color = new SettingColor(RenderColor.fromHex(0xff55ff), "color", instance);

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && SlayerUtil.isFightingBoss(SlayerUtil.voidgloom) && SlayerUtil.bossEntity != null && SlayerUtil.nameEntity != null) {
            String name = Utils.toPlainString(SlayerUtil.nameEntity.getName());
            if (name.endsWith("Hits")) {
                String[] parts = name.split(" ");
                String hits = Utils.format("{} Hits", parts[parts.length - 2]);
                Vec3d pos = SlayerUtil.bossEntity.getLerpedPos(event.tickCounter.getTickProgress(true));
                event.drawText(pos.add(0.0, -0.5, 0), Text.literal(hits), 0.2f, true, color.value());
            }
        }
    }
}