package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Config;
import nofrills.events.WorldTickEvent;
import nofrills.misc.Utils;

import static nofrills.Main.eventBus;

public class UpdateChecker {
    private static int ticksInSkyblock = 0;

    @EventHandler
    public static void onTick(WorldTickEvent event) {
        if (Utils.isInSkyblock()) {
            ticksInSkyblock++;
        }
        if (ticksInSkyblock >= 60) {
            if (Config.updateChecker) {
                Utils.checkUpdate(false);
            }
            eventBus.unsubscribe(UpdateChecker.class);
        }
    }
}
