package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
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
        if (instance.isActive() && Utils.isInDungeons() && event.entity instanceof ArmorStandEntity && event.namePlain.equals("Spirit Bow")) {
            spiritBows.add(event.entity);
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && Utils.isInDungeons()) {
            if (!spiritBows.empty()) {
                for (Entity ent : spiritBows.get()) {
                    BlockPos ground = Utils.findGround(ent.getBlockPos(), 4);
                    Vec3d pos = ent.getPos();
                    Vec3d posAdjust = new Vec3d(pos.x, ground.up(1).getY() + 1, pos.z);
                    event.drawFilled(Box.of(posAdjust, 0.8, 1.75, 0.8), true, color.value());
                }
            }
        }
    }
}
