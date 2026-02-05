package nofrills.features.misc;

import nofrills.config.Feature;
import nofrills.config.SettingDouble;

public class ItemScale {
    public static final Feature instance = new Feature("itemScale");

    public static final SettingDouble scale = new SettingDouble(1.0, "scale", instance);
}
