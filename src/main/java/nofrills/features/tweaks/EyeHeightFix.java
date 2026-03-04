package nofrills.features.tweaks;

import nofrills.config.Feature;
import nofrills.misc.Utils;

public class EyeHeightFix {
    public static final Feature instance = new Feature("eyeHeightFix");

    public static boolean active() {
        return instance.isActive() && !Utils.isOnModernIsland();
    }
}
