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

public class BuildPileHighlight {
    public static final Feature instance = new Feature("buildPileHighlight");

    public static final SettingColor color = new SettingColor(new RenderColor(255, 0, 0, 127), "color", instance);

    private static final EntityCache cache = new EntityCache();

    private static Vec3 getGround(Vec3 pos) {
        BlockPos blockPos = BlockPos.containing(pos.x, Math.max(pos.y, 75), pos.z);
        BlockPos ground = Utils.findGround(blockPos, 4);
        return new Vec3(pos.x, ground.getCenter().add(0, 0.5, 0).y, pos.z);
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && Utils.isInKuudra() && event.namePlain.startsWith("PROGRESS: ") && event.namePlain.endsWith("%")) {
            cache.add(event.entity);
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && Utils.isInKuudra() && !cache.empty()) {
            for (Entity pile : cache.get()) {
                Component name = pile.getCustomName();
                if (name == null) continue;
                String string = Utils.toPlain(name);
                if (string.startsWith("PROGRESS: ") && string.endsWith("%")) {
                    event.drawBeam(getGround(pile.getPosition(event.delta())), 256, false, color.value());
                }
            }
        }
    }
}
