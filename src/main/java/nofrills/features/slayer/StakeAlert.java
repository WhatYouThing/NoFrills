package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import nofrills.config.Feature;
import nofrills.events.WorldTickEvent;
import nofrills.misc.SlayerUtil;
import nofrills.misc.Utils;

public class StakeAlert {
    public static final Feature instance = new Feature("stakeAlert");

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && SlayerUtil.isFightingBoss(SlayerUtil.vampire) && Utils.isInChateau()) {
            Entity nameEntity = SlayerUtil.getNameEntity();
            if (nameEntity == null) return;
            String name = Utils.toPlainString(nameEntity.getName());
            if (name.contains(Utils.Symbols.vampLow)) {
                Utils.showTitleCustom("Steak!", 1, 25, 4.0f, 0xff0000);
            }
        }
    }
}
