package nofrills.features.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Util;
import nofrills.config.Feature;
import nofrills.events.WorldTickEvent;
import nofrills.misc.Utils;

public class AutoTip {
    public static final Feature instance = new Feature("autoTip");

    private static long time = 0L;

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && Utils.isOnHypixel() && Util.getMeasuringTimeMs() > time) {
            Utils.sendMessage("/tipall");
            time = Util.getMeasuringTimeMs() + 900000;
        }
    }
}
