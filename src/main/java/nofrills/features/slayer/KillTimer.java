package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.events.WorldTickEvent;
import nofrills.misc.SlayerUtil;
import nofrills.misc.Utils;

public class KillTimer {
    public static final Feature instance = new Feature("killTimer");

    private static int aliveTicks = 0;

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive()) {
            if (SlayerUtil.bossAlive) {
                aliveTicks++;
            } else if (aliveTicks > 0) {
                Utils.infoFormat("{}aSlayer boss took {}s to kill.", Utils.Symbols.format, Utils.formatDecimal(aliveTicks / 20.0f));
                aliveTicks = 0;
            }
        }
    }
}
