package nofrills.features.kuudra;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.GiantEntity;
import net.minecraft.util.math.Vec3d;
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
                if (ent instanceof GiantEntity) {
                    cache.add(ent);
                }
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && Utils.isInKuudra() && KuudraUtil.getCurrentPhase().equals(KuudraUtil.Phase.Collect) && !cache.empty()) {
            for (Entity supply : cache.get()) {
                Vec3d pos = supply.getLerpedPos(event.delta());
                float yaw = supply.getYaw(event.delta());
                Vec3d supplyPos = new Vec3d(
                        pos.getX() + (3.7 * Math.cos((yaw + 130) * (Math.PI / 180))),
                        75,
                        pos.getZ() + (3.7 * Math.sin((yaw + 130) * (Math.PI / 180)))
                );
                event.drawBeam(supplyPos, 256, false, color.value());
            }
        }
    }
}
