package nofrills.features.farming;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.EntityNamedEvent;
import nofrills.events.EventListener;
import nofrills.events.SpawnParticleEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.regex.Pattern;

@EventListener
public class WateringHelper {
    public static final Feature instance = new Feature("wateringHelper");

    public static final SettingBool betterVisibility = new SettingBool(true, "betterVisibility", instance);
    public static final SettingBool hideParticles = new SettingBool(false, "hideParticles", instance);

    private static final EntityCache waterLevels = new EntityCache();
    private static final Pattern namePattern = Pattern.compile("\\|*");

    private static boolean isHoldingWateringCan() {
        CompoundTag data = Utils.getCustomData(Utils.getHeldItem());
        return data != null && data.contains("water_level");
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && Utils.isInGarden() && betterVisibility.value() && namePattern.matcher(event.namePlain).matches()) {
            event.entity.setCustomNameVisible(false);
            waterLevels.add(event.entity);
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && betterVisibility.value() && !waterLevels.empty()) {
            for (Entity ent : waterLevels.get()) {
                if (!ent.hasCustomName()) {
                    continue;
                }
                event.drawText(ent.position().add(0.0, 0.5, 0.0), ent.getName(), 0.025f, true, RenderColor.WHITE);
            }
        }
    }

    @EventHandler
    private static void onParticle(SpawnParticleEvent event) {
        if (instance.isActive() && Utils.isInGarden() && hideParticles.value() && isHoldingWateringCan()) {
            if (event.type.equals(ParticleTypes.DRIPPING_WATER) || event.type.equals(ParticleTypes.ENTITY_EFFECT)) {
                event.cancel();
            }
        }
    }
}
