package nofrills.features.misc;

import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.misc.Utils;

public class ForceNametag {
    public static final Feature instance = new Feature("forceNametag");

    public static final SettingBool self = new SettingBool(false, "self", instance);

    public static boolean isActive() {
        return instance.isActive() && Utils.isInSkyblock();
    }
}
