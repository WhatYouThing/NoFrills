package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import nofrills.config.Feature;
import nofrills.events.WorldTickEvent;
import nofrills.misc.SlayerUtil;
import nofrills.misc.Utils;

public class IceAlert {
    public static final Feature instance = new Feature("iceAlert");

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && SlayerUtil.isFightingBoss(SlayerUtil.vampire) && Utils.isInChateau()) {
            Entity timer = SlayerUtil.getTimerEntity();
            if (timer == null) return;
            String name = Utils.toPlainString(timer.getName());
            if (name.contains("TWINCLAWS")) {
                Utils.showTitleCustom(Utils.format("Ice: {}", name.split("TWINCLAWS")[1].trim().split(" ")[0]), 1, 25, 4.0f, 0x00ffff);
            }
        }
    }
}
