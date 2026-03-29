package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.EntityNamedEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

public class SpiritBowHighlight {
    public static final Feature instance = new Feature("spiritBowHighlight");

    public static final SettingColor color = new SettingColor(RenderColor.fromArgb(0xaaaf00ff), "color", instance.key());

    private static final EntityCache spiritBows = new EntityCache();

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && Utils.isInDungeons() && event.entity instanceof ArmorStand && event.namePlain.equals("Spirit Bow")) {
            spiritBows.add(event.entity);
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && Utils.isInDungeons()) {
            if (!spiritBows.empty()) {
                for (Entity ent : spiritBows.get()) {
                    BlockPos ground = Utils.findGround(ent.blockPosition(), 4);
                    Vec3 pos = ent.position();
                    Vec3 posAdjust = new Vec3(pos.x, ground.above(1).getY() + 1, pos.z);
                    event.drawFilled(AABB.ofSize(posAdjust, 0.8, 1.75, 0.8), true, color.value());
                }
            }
        }
    }
}
