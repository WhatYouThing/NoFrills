package nofrills.features.fixes;

import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.misc.Utils;

public class SneakLagFix {
    public static final Feature instance = new Feature("stonkFix");

    public static final SettingBool skyblockCheck = new SettingBool(false, "skyblockCheck", instance.key());
    public static final SettingBool modernCheck = new SettingBool(true, "modernCheck", instance.key());

    public static boolean active() {
        boolean isActive = instance.isActive();
        if (isActive) {
            if (skyblockCheck.value() && !Utils.isInSkyblock()) {
                return false;
            }
            if (modernCheck.value() && Utils.isOnModernIsland()) {
                return false;
            }
        }
        return isActive;
    }
}
