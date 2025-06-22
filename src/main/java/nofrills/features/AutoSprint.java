package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import nofrills.events.WorldTickEvent;

import static nofrills.Main.Config;
import static nofrills.Main.mc;

public class AutoSprint {
    @EventHandler
    public static void tick(WorldTickEvent event) {
        if (Config.autoSprint()) {
            mc.options.sprintKey.setPressed(true);
        }
    }
}
