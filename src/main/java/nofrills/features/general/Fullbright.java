package nofrills.features.general;

import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.misc.RenderColor;

public class Fullbright {
    public static final Feature instance = new Feature("fullbright");

    public static final SettingColor color = new SettingColor(RenderColor.fromHex(0xffffff), "color", instance.key());
}
