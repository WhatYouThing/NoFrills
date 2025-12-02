package nofrills.features.mining;

import nofrills.config.Feature;

public class BreakResetFix {
    public static final Feature instance = new Feature("breakResetFix");

    public static boolean active() {
        return instance.isActive();
    }
}
