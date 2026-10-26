package nofrills.features.dungeons.devicesolvers;

import nofrills.config.Feature;
import nofrills.config.SettingBool;

public class DeviceSolvers {
    public static final Feature instance = new Feature("deviceSolvers");

    public static final SettingBool simonSays = new SettingBool(false, "simonSays", instance);
    public static final SettingBool arrowAlign = new SettingBool(false, "arrowAlign", instance);
    public static final SettingBool sharpshooter = new SettingBool(false, "sharpshooter", instance);
}
