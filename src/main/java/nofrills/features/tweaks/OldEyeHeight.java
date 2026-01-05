package nofrills.features.tweaks;

import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.misc.Utils;

public class OldEyeHeight {
    public static final Feature instance = new Feature("oldEyeHeight");

    public static final SettingBool sneakEnabled = new SettingBool(true, "sneakEnabled", instance.key());
    public static final SettingBool sneakSkyblockCheck = new SettingBool(false, "sneakSkyblockCheck", instance.key());
    public static final SettingBool sneakModernCheck = new SettingBool(true, "sneakModernCheck", instance.key());
    public static final SettingBool swimEnabled = new SettingBool(true, "swimEnabled", instance.key());
    public static final SettingBool swimSkyblockCheck = new SettingBool(false, "swimSkyblockCheck", instance.key());
    public static final SettingBool swimModernCheck = new SettingBool(true, "swimModernCheck", instance.key());

    public static boolean active() {
        return instance.isActive();
    }

    public static boolean sneakActive() {
        boolean isActive = sneakEnabled.value();
        if (isActive) {
            if (sneakSkyblockCheck.value() && !Utils.isInSkyblock()) return false;
            if (sneakModernCheck.value() && Utils.isOnModernIsland()) return false;
        }
        return isActive;
    }

    public static boolean swimActive() {
        boolean isActive = swimEnabled.value();
        if (isActive) {
            if (swimSkyblockCheck.value() && !Utils.isInSkyblock()) return false;
            if (swimModernCheck.value() && Utils.isOnModernIsland()) return false;
        }
        return isActive;
    }
}
