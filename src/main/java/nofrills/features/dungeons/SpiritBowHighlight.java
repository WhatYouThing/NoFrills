package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.EntityNamedEvent;
import nofrills.events.EventListener;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.DungeonUtil;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

@EventListener
public class SpiritBowHighlight {
    public static final Feature instance = new Feature("spiritBowHighlight");

    public static final SettingColor color = new SettingColor(RenderColor.fromFormat(ChatFormatting.LIGHT_PURPLE).withAlpha(0.66f), "color", instance);

    private static final EntityCache spiritBows = new EntityCache();

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && event.namePlain.equals("Spirit Bow") && DungeonUtil.isInBossRoom("4")) {
            spiritBows.add(event.entity);
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && !spiritBows.empty()) {
            for (Entity ent : spiritBows.get()) {
                BlockPos ground = Utils.findGround(ent.blockPosition(), 4);
                AABB box = AABB.ofSize(Vec3.atCenterOf(ground).add(0.0, 1.4875, 0.0), 0.5f, 1.975f, 0.5f);
                event.drawFilled(box, true, color.value());
            }
        }
    }
}
