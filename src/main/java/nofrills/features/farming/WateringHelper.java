package nofrills.features.farming;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.EntityNamedEvent;
import nofrills.events.SpawnParticleEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

public class WateringHelper {
    public static final Feature instance = new Feature("wateringHelper");

    public static final SettingBool betterVisibility = new SettingBool(true, "betterVisibility", instance);
    public static final SettingBool hideParticles = new SettingBool(false, "hideParticles", instance);

    private static final EntityCache waterLevels = new EntityCache();

    private static boolean isActive() {
        return instance.isActive() && Utils.isInGarden();
    }

    private static boolean isHoldingWateringCan() {
        NbtCompound data = Utils.getCustomData(Utils.getHeldItem());
        return data != null && data.contains("water_level");
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (isActive() && betterVisibility.value() && isHoldingWateringCan() && event.namePlain.equals("||||||||||||||||")) {
            event.entity.setCustomNameVisible(false);
            waterLevels.add(event.entity);
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (isActive() && betterVisibility.value()) {
            for (Entity ent : waterLevels.get()) {
                if (!ent.hasCustomName()) {
                    continue;
                }
                event.drawText(ent.getEntityPos().add(0.0, 0.5, 0.0), ent.getName(), 0.025f, true, RenderColor.white);
            }
        }
    }

    @EventHandler
    private static void onParticle(SpawnParticleEvent event) {
        if (isActive() && hideParticles.value() && isHoldingWateringCan() && event.type.equals(ParticleTypes.DRIPPING_WATER)) {
            event.cancel();
        }
    }
}
