package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.EntityNamedEvent;
import nofrills.misc.SlayerUtil;
import nofrills.misc.Utils;

public class VoidgloomSeraph {
    public static final Feature instance = new Feature("voidgloomSeraph");

    public static final SettingBool hits = new SettingBool(false, "hits", instance.key());

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && SlayerUtil.isFightingBoss(SlayerUtil.voidgloom)) {
            if (hits.value() && SlayerUtil.isName(event.namePlain) && SlayerUtil.isNearSpawner(event.entity)) {
                if (event.namePlain.endsWith("Hits")) {
                    String[] parts = event.namePlain.split(" ");
                    Utils.showTitleCustom("Shield: " + parts[parts.length - 2] + " hits", 100, 25, 4.0f, 0xff55ff);
                } else {
                    if (Utils.isRenderingCustomTitle()) {
                        Utils.showTitleCustom("", 0, 0, 0, 0);
                    }
                }
            }
        }
    }
}
