package nofrills.features.general;

import nofrills.config.Feature;
import nofrills.config.SettingBool;

public class AutoSprint {
    public static final Feature instance = new Feature("autoSprint");

    public static final SettingBool waterCheck = new SettingBool(false, "waterCheck", instance.key());

    private static boolean wasSprinting = false;

    public static void setSprinting(boolean sprinting) {
        wasSprinting = sprinting;
    }

    public static boolean wasSprinting() {
        return wasSprinting;
    }
}
