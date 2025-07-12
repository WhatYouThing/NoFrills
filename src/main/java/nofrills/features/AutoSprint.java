package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Config;
import nofrills.events.WorldTickEvent;

import static nofrills.Main.mc;

public class AutoSprint {
    @EventHandler
    public static void tick(WorldTickEvent event) {
        if (Config.autoSprint && mc.player != null && !mc.player.isSubmergedInWater()) {
            mc.options.sprintKey.setPressed(true);
        }
    }
}
