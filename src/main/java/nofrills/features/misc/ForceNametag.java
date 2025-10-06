package nofrills.features.misc;

import nofrills.config.Feature;
import nofrills.misc.Utils;

public class ForceNametag {
    public static final Feature instance = new Feature("forceNametag");

    public static boolean isActive() {
        return instance.isActive() && Utils.isInSkyblock();
    }
}
