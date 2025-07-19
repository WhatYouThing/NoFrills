package nofrills.features.general;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.events.ServerJoinEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.Utils;

public class UpdateChecker {
    public static final Feature instance = new Feature("updateChecker");

    private static int ticks = 0;

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && ticks > 0) {
            ticks--;
            if (ticks == 0) {
                Utils.checkUpdate(false);
                ticks = -1;
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        if (ticks != -1) {
            ticks = 100;
        }
    }
}
