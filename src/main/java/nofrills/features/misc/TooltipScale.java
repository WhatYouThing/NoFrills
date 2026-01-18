package nofrills.features.misc;

import nofrills.config.Feature;
import nofrills.config.SettingDouble;
import nofrills.config.SettingEnum;

public class TooltipScale {
    public static final Feature instance = new Feature("tooltipScale");

    public static final SettingEnum<ScaleMode> mode = new SettingEnum<>(ScaleMode.Dynamic, ScaleMode.class, "mode", instance);
    public static final SettingDouble scale = new SettingDouble(1.0, "scale", instance.key());

    public static boolean isDynamic() {
        return mode.value().equals(ScaleMode.Dynamic);
    }

    public static boolean isCustom() {
        return mode.value().equals(ScaleMode.Custom);
    }

    public enum ScaleMode {
        Dynamic,
        Custom
    }
}
