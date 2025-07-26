package nofrills.features.fixes;

import nofrills.config.Feature;

public class ItemCountFix {
    public static final Feature instance = new Feature("itemCountFix");

    public static boolean active() {
        return instance.isActive();
    }
}
