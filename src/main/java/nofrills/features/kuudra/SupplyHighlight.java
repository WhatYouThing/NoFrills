package nofrills.features.kuudra;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.phys.Vec3;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.WorldRenderEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.EntityCache;
import nofrills.misc.KuudraUtil;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

public class SupplyHighlight {
    public static final Feature instance = new Feature("supplyHighlight");

    public static final SettingColor color = new SettingColor(new RenderColor(0, 255, 255, 170), "color", instance);

    private static final EntityCache cache = new EntityCache();

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && Utils.isInKuudra() && KuudraUtil.getCurrentPhase().equals(KuudraUtil.Phase.Collect)) {
            for (Entity ent : Utils.getEntities()) {
                if (ent instanceof Giant) {
                    cache.add(ent);
                }
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && Utils.isInKuudra() && KuudraUtil.getCurrentPhase().equals(KuudraUtil.Phase.Collect) && !cache.empty()) {
            for (Entity supply : cache.get()) {
                Vec3 pos = supply.getPosition(event.delta());
                float yaw = supply.getViewXRot(event.delta());
                Vec3 supplyPos = new Vec3(
                        pos.x + (3.7 * Math.cos((yaw + 130) * (Math.PI / 180))),
                        75,
                        pos.y + (3.7 * Math.sin((yaw + 130) * (Math.PI / 180)))
                );
                event.drawBeam(supplyPos, 256, false, color.value());
            }
        }
    }
}
