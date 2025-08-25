package nofrills.features.tweaks;

import nofrills.config.Feature;

public class MiddleClickFix {
    public static final Feature instance = new Feature("middleClickFix");

    public static boolean active() {
        return instance.isActive();
    }
}
