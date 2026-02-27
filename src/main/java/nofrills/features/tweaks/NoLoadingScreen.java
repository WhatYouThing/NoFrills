package nofrills.features.tweaks;

import nofrills.config.Feature;
import nofrills.config.SettingBool;

public class NoLoadingScreen {
    public static final Feature instance = new Feature("noLoadingScreen");

    public static final SettingBool serverOnly = new SettingBool(true, "serverOnly", instance);
}
