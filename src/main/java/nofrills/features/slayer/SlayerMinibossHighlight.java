package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.config.SettingEnum;
import nofrills.events.EntityNamedEvent;
import nofrills.events.EventListener;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.*;

import java.util.List;

@EventListener
public class SlayerMinibossHighlight {
    public static final Feature instance = new Feature("slayerMinibossHighlight");

    public static final SettingEnum<RenderStyle> style = new SettingEnum<>(RenderStyle.Outline, RenderStyle.class, "style", instance);
    public static final SettingColor fillColor = new SettingColor(RenderColor.fromArgb(0x5500ffff), "fillColor", instance);
    public static final SettingColor outlineColor = new SettingColor(RenderColor.fromArgb(0xff00ffff), "outlineColor", instance);

    private static final EntityCache cache = EntityCache.create();

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && SlayerUtil.currentBoss != null && event.namePlain.endsWith(Utils.Symbols.heart)) {
            for (String name : SlayerUtil.getMinibossNames()) {
                if (event.namePlain.contains(name)) {
                    List<Entity> otherEntities = Utils.getOtherEntities(event.entity, 1.0, 3.0, 1.0, Utils::isMob);
                    Entity closest = Utils.findNametagOwner(event.entity, otherEntities);
                    if (closest != null) {
                        cache.add(closest.isPassenger() ? closest.getVehicle() : closest);
                    }
                    break;
                }
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && SlayerUtil.currentBoss != null) {
            for (Entity ent : cache.get()) {
                if (!ent.isAlive()) continue;
                AABB box = Utils.getLerpedBox(ent, event.delta());
                event.drawStyled(box, style.value(), false, outlineColor.value(), fillColor.value());
            }
        }
    }
}
