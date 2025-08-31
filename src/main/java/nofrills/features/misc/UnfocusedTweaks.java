package nofrills.features.misc;

import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingInt;

import static nofrills.Main.mc;

public class UnfocusedTweaks {
    public static final Feature instance = new Feature("unfocusedTweaks");

    public static final SettingBool noWorldRender = new SettingBool(false, "noWorldRender", instance.key());
    public static final SettingBool muteSounds = new SettingBool(false, "muteSounds", instance.key());
    public static final SettingBool noVanilla = new SettingBool(false, "noVanilla", instance.key());
    public static final SettingInt fpsLimit = new SettingInt(0, "fpsLimit", instance.key());

    public static boolean active() {
        return instance.isActive() && !mc.isWindowFocused();
    }
}