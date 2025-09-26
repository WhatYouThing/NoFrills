package nofrills.features.tweaks;

import nofrills.config.Feature;

public class DisconnectFix {
    public static final Feature instance = new Feature("disconnectFix");

    public static boolean active() {
        return instance.isActive();
    }
}
