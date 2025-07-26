package nofrills.features.fixes;

import nofrills.config.Feature;

public class RidingCameraFix {
    public static final Feature instance = new Feature("ridingCameraFix");

    public static boolean active() {
        return instance.isActive();
    }
}
