package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.config.SettingSound;
import nofrills.config.SettingTitle;
import nofrills.events.EventListener;
import nofrills.events.ServerJoinEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.SlayerUtil;
import nofrills.misc.Utils;

@EventListener
public class SpawnAlert {
    public static final Feature instance = new Feature("spawnAlert");

    public static final SettingTitle title = new SettingTitle("§c§lBOSS SPAWNED!", 0, 30, 10, true, "title", instance);
    public static final SettingSound sound = new SettingSound("entity.experience_orb.pickup", 1.0, 0.0, "sound", instance);

    private static boolean spawned = false;

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive()) {
            if (!spawned && SlayerUtil.isBossAlive()) {
                Utils.showTitle(title);
                Utils.playSound(sound);
                spawned = true;
            } else if (!SlayerUtil.isBossAlive()) {
                spawned = false;
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        spawned = false;
    }
}
