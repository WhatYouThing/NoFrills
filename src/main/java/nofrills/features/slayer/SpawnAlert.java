package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import nofrills.config.Feature;
import nofrills.events.ServerJoinEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.SlayerUtil;
import nofrills.misc.Utils;

public class SpawnAlert {
    public static final Feature instance = new Feature("spawnAlert");

    private static boolean spawned = false;

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive()) {
            if (!spawned && SlayerUtil.bossAlive) {
                Utils.showTitle("§c§lBOSS SPAWNED!", "", 0, 30, 10);
                Utils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1, 0);
                spawned = true;
            } else if (!SlayerUtil.bossAlive) {
                spawned = false;
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        spawned = false;
    }
}
