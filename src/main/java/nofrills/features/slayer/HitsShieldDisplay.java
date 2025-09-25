package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.config.SettingDouble;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.SlayerUtil;
import nofrills.misc.Utils;

import java.util.List;

public class HitsShieldDisplay {
    public static final Feature instance = new Feature("hitsShieldDisplay");

    public static final SettingColor color = new SettingColor(RenderColor.fromHex(0xff55ff), "color", instance);
    public static final SettingDouble scale = new SettingDouble(0.2, "scale", instance);

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && SlayerUtil.isFightingBoss(SlayerUtil.voidgloom)) {
            Entity nameEntity = SlayerUtil.getNameEntity();
            List<Entity> bossEntities = SlayerUtil.getBossEntities();
            if (nameEntity == null || bossEntities.isEmpty()) return;
            String name = Utils.toPlainString(nameEntity.getName());
            if (name.endsWith("Hits") || name.endsWith("Hit")) {
                String[] parts = name.split(" ");
                String hits = Utils.format("{} Hits", parts[parts.length - 2]);
                Vec3d pos = bossEntities.getFirst().getLerpedPos(event.tickCounter.getTickProgress(true));
                event.drawText(pos, Text.literal(hits), scale.valueFloat(), true, color.value());
            }
        }
    }
}