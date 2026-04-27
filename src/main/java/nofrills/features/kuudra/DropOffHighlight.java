package nofrills.features.kuudra;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
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

    private static Vec3 getGround(Vec3 pos) {
        BlockPos blockPos = BlockPos.containing(pos.x, Math.max(pos.y, 75), pos.z);
        BlockPos ground = Utils.findGround(blockPos, 4);
        return new Vec3(pos.x, ground.getCenter().add(0, 0.5, 0).y, pos.z);
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
                Component name = drop.getCustomName();
                if (name == null) continue;
                String string = Utils.toPlain(name);
                if (string.equals("BRING SUPPLY CHEST HERE")) {
                    event.drawBeam(getGround(drop.getPosition(event.delta())), 256, false, color.value());
                }
            }
        }
    }
}
