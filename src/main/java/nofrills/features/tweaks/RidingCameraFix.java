package nofrills.features.tweaks;

import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.misc.Utils;

public class RidingCameraFix {
    public static final Feature instance = new Feature("ridingCameraFix");

    public static final SettingBool skyblockCheck = new SettingBool(false, "skyblockCheck", instance.key());

    public static boolean active() {
        if (instance.isActive()) {
            if (skyblockCheck.value()) {
                return Utils.isInSkyblock();
            }
            return true;
        }
        return false;
    }
}
