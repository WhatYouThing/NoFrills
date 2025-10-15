package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Feature;
import nofrills.events.EntityNamedEvent;
import nofrills.events.PlaySoundEvent;
import nofrills.events.ServerTickEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.SlayerUtil;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PillarAlert {
    public static final Feature instance = new Feature("pillarAlert");

    private static final Pattern firePillarRegex = Pattern.compile("[0-9]s [0-9] hits");
    private static final List<Vec3d> pillarData = new ArrayList<>();
    private static int pillarClearTicks = 0;

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && !pillarData.isEmpty() && firePillarRegex.matcher(event.namePlain).matches()) {
            if (Utils.horizontalDistance(event.entity.getPos(), pillarData.getLast()) <= 3) {
                Utils.showTitleCustom("Pillar: " + event.namePlain, 30, 25, 4.0f, RenderColor.fromHex(0xffff00));
                pillarClearTicks = 60;
            }
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && !SlayerUtil.bossAlive && pillarClearTicks > 0) {
            pillarData.clear();
            pillarClearTicks = 0;
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (instance.isActive() && SlayerUtil.isFightingBoss(SlayerUtil.blaze)) {
            if (pillarClearTicks > 0) {
                pillarClearTicks--;
                if (pillarClearTicks == 0) {
                    pillarData.clear();
                }
            }
        }
    }

    @EventHandler
    private static void onSound(PlaySoundEvent event) {
        if (instance.isActive() && SlayerUtil.isFightingBoss(SlayerUtil.blaze) && event.isSound(SoundEvents.ENTITY_CHICKEN_EGG)) {
            Entity spawner = SlayerUtil.getSpawnerEntity();
            if (spawner == null) return;
            Vec3d pos = new Vec3d(event.packet.getX(), event.packet.getY(), event.packet.getZ());
            if (pillarData.isEmpty()) {
                if (Utils.horizontalDistance(pos, spawner.getPos()) <= 1.5) {
                    pillarData.add(pos);
                    pillarClearTicks = 60;
                }
            } else {
                if (Utils.horizontalDistance(pos, pillarData.getLast()) <= 4) {
                    pillarData.add(pos);
                    pillarClearTicks = 60;
                }
            }
        }
    }
}
