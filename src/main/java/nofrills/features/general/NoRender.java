package nofrills.features.general;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.WorldTickEvent;

import static nofrills.Main.mc;

public class NoRender {
    public static final Feature instance = new Feature("autoSprint");
    public static final SettingBool waterCheck = new SettingBool(false, "waterCheck", instance.key());

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && mc.player != null) {
            if (waterCheck.value() && mc.player.isSubmergedInWater()) {
                return;
            }
            mc.options.sprintKey.setPressed(true);
        }
    }
}
