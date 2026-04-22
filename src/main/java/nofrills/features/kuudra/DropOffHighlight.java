package nofrills.features.kuudra;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.EntityNamedEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

public class DropOffHighlight {
    public static final Feature instance = new Feature("dropOffHighlight");

    public static final SettingColor color = new SettingColor(new RenderColor(255, 255, 0, 127), "color", instance);

    private static final EntityCache cache = new EntityCache();

    private static Vec3d getGround(Vec3d pos) {
        BlockPos blockPos = BlockPos.ofFloored(pos.getX(), Math.max(pos.getY(), 75), pos.getZ());
        BlockPos ground = Utils.findGround(blockPos, 4);
        return new Vec3d(pos.getX(), ground.toCenterPos().add(0, 0.5, 0).getY(), pos.getZ());
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && Utils.isInKuudra() && event.namePlain.equals("BRING SUPPLY CHEST HERE")) {
            cache.add(event.entity);
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && Utils.isInKuudra() && !cache.empty()) {
            for (Entity drop : cache.get()) {
                Text name = drop.getCustomName();
                if (name == null) continue;
                String string = Utils.toPlain(name);
                if (string.equals("BRING SUPPLY CHEST HERE")) {
                    event.drawBeam(getGround(drop.getLerpedPos(event.delta())), 256, false, color.value());
                }
            }
        }
    }
}
