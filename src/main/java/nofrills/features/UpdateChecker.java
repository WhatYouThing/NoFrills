package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import nofrills.events.WorldTickEvent;
import nofrills.misc.Utils;

import static nofrills.Main.Config;
import static nofrills.Main.eventBus;

public class UpdateChecker {
    private static int ticksInSkyblock = 0;

    @EventHandler
    public static void onTick(WorldTickEvent event) {
        if (Utils.isInSkyblock()) {
            ticksInSkyblock++;
        }
        if (ticksInSkyblock >= 60) {
            if (Config.updateChecker()) {
                Utils.checkUpdate(false);
            }
            eventBus.unsubscribe(UpdateChecker.class);
        }
    }
}
