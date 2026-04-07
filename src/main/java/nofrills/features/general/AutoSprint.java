package nofrills.features.general;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.WorldTickEvent;

import static nofrills.Main.mc;

public class AutoSprint {
    public static final Feature instance = new Feature("autoSprint");

    public static final SettingBool waterCheck = new SettingBool(false, "waterCheck", instance.key());

    private static boolean wasSprinting = false;

    private static void setSprinting(boolean sprinting) {
        if (mc.options.getSprintToggled().getValue()) {
            if (mc.options.sprintKey.isPressed() == !sprinting) {
                mc.options.sprintKey.setPressed(true);
            }
        } else {
            mc.options.sprintKey.setPressed(sprinting);
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && mc.player != null) {
            if (waterCheck.value() && mc.player.isTouchingWater()) {
                if (wasSprinting) {
                    setSprinting(false);
                    wasSprinting = false;
                }
                return;
            }
            setSprinting(true);
            wasSprinting = mc.options.sprintKey.isPressed();
        }
    }
}
