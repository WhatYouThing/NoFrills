package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
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

    public static final SettingBool hide = new SettingBool(false, "hide", instance);
    public static final SettingEnum<RenderStyle> style = new SettingEnum<>(RenderStyle.Outline, RenderStyle.class, "style", instance);
    public static final SettingColor fillColor = new SettingColor(RenderColor.fromArgb(0x5500ffff), "fillColor", instance);
    public static final SettingColor outlineColor = new SettingColor(RenderColor.fromArgb(0xff00ffff), "outlineColor", instance);

    private static final MappedEntityCache<Entity> cache = new MappedEntityCache<>();

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && event.namePlain.equals("SLAYER MINIBOSS") && !cache.has(event.entity)) {
            List<Entity> otherEntities = Utils.getOtherEntities(event.entity, 1.0, 3.0, 1.0, Utils::isMob);
            Entity closest = Utils.getNameTagOwner(event.entity, otherEntities);
            if (closest != null) {
                if (hide.value()) {
                    event.entity.setCustomNameVisible(false);
                }
                cache.add(event.entity, closest.isPassenger() ? closest.getVehicle() : closest);
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && SlayerUtil.currentBoss != null && !cache.empty()) {
            for (Entity ent : cache.getValues()) {
                if (!ent.isAlive()) continue;
                AABB box = Utils.getLerpedBox(ent, event.delta());
                event.drawStyled(box, style.value(), false, outlineColor.value(), fillColor.value());
            }
        }
    }
}
