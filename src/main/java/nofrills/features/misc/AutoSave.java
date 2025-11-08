package nofrills.features.misc;

import nofrills.config.Config;
import nofrills.config.Feature;

public class AutoSave {
    public static final Feature instance = new Feature("autoSave");

    private static int hash = 0;

    public static void save() {
        if (hash != Config.getHash()) {
            Config.saveAsync();
            hash = Config.getHash();
        }
    }
}
