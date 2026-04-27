package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import nofrills.config.Feature;
import nofrills.events.EntityNamedEvent;
import nofrills.events.PlaySoundEvent;
import nofrills.events.ServerTickEvent;
import nofrills.events.WorldTickEvent;
import nofrills.hud.HudManager;
import nofrills.misc.EntityCache;
import nofrills.misc.SlayerUtil;
import nofrills.misc.Utils;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

public class PillarAlert {
    public static final Feature instance = new Feature("pillarAlert");

    private static final Pattern firePillarRegex = Pattern.compile("[0-9]s [0-9] hits");
    private static final CopyOnWriteArrayList<Vec3> pillarData = new CopyOnWriteArrayList<>();
    private static final EntityCache pillarCache = new EntityCache();
    private static int pillarClearTicks = 0;

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && !pillarData.isEmpty() && firePillarRegex.matcher(event.namePlain).matches()) {
            if (Utils.horizontalDistance(event.entity.position(), pillarData.getLast()) <= 3) {
                pillarCache.add(event.entity);
                pillarClearTicks = 60;
            }
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive()) {
            if (SlayerUtil.isFightingBoss(SlayerUtil.blaze)) {
                Entity nametag = pillarCache.getFirst();
                if (nametag != null && nametag.getCustomName() != null) {
                    HudManager.setCustomTitle(nametag.getCustomName().copy(), 1);
                }
            } else if (pillarClearTicks > 0) {
                pillarData.clear();
                pillarClearTicks = 0;
            }
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
        if (instance.isActive() && SlayerUtil.isFightingBoss(SlayerUtil.blaze) && event.isSound(SoundEvents.CHICKEN_EGG)) {
            Entity spawner = SlayerUtil.getSpawnerEntity();
            if (spawner == null) return;
            if (pillarData.isEmpty()) {
                if (Utils.horizontalDistance(event.pos, spawner.position()) <= 1.5) {
                    pillarData.add(event.pos);
                    pillarClearTicks = 60;
                }
            } else {
                if (Utils.horizontalDistance(event.pos, pillarData.getLast()) <= 4) {
                    pillarData.add(event.pos);
                    pillarClearTicks = 60;
                }
            }
        }
    }
}
