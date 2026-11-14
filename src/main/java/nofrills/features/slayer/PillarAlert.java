package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import nofrills.config.Feature;
import nofrills.events.*;
import nofrills.hud.HudManager;
import nofrills.misc.EntityCache;
import nofrills.misc.SlayerUtil;
import nofrills.misc.Utils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

@EventListener
public class PillarAlert {
    public static final Feature instance = new Feature("pillarAlert");

    private static final Pattern firePillarRegex = Pattern.compile("[0-9]s [0-9] hits");
    private static final EntityCache pillarCache = new EntityCache();
    private static final AtomicInteger tickCounter = new AtomicInteger();
    private static final AtomicInteger startTick = new AtomicInteger();

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && firePillarRegex.matcher(event.namePlain).matches()) {
            int difference = tickCounter.get() - startTick.get();
            if (difference >= 90 && difference <= 110) {
                pillarCache.add(event.entity);
            }
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive()) {
            if (SlayerUtil.isFightingBoss(SlayerUtil.BLAZE)) {
                Entity nametag = pillarCache.getFirst();
                if (nametag != null && nametag.getCustomName() != null) {
                    HudManager.setCustomTitle(nametag.getCustomName().copy(), 1);
                }
            } else {
                if (tickCounter.get() != 0) tickCounter.set(0);
                if (startTick.get() != 0) startTick.set(0);
            }
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (instance.isActive() && SlayerUtil.isFightingBoss(SlayerUtil.BLAZE)) {
            tickCounter.incrementAndGet();
        }
    }

    @EventHandler
    private static void onSound(PlaySoundEvent event) {
        if (instance.isActive() && event.isSound(SoundEvents.CHICKEN_EGG) && SlayerUtil.isFightingBoss(SlayerUtil.BLAZE)) {
            Entity spawner = SlayerUtil.getSpawnerEntity();
            if (spawner == null) return;
            if (Utils.horizontalDistance(event.pos, spawner.position()) <= 2.0) {
                startTick.set(tickCounter.get());
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        tickCounter.set(0);
        startTick.set(0);
    }
}
