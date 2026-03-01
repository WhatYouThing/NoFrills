package nofrills.features.misc;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.EntityNamedEvent;
import nofrills.misc.SlayerUtil;
import nofrills.misc.Utils;

import java.util.regex.Pattern;

public class NoDamageSplash {
    public static final Feature instance = new Feature("noDamageSplash");

    public static final SettingBool slayerOnly = new SettingBool(false, "slayerOnly", instance.key());
    public static final SettingBool dungeonsOnly = new SettingBool(false, "dungeonsOnly", instance.key());

    private static final Pattern pattern = Pattern.compile("[✧✯]?(\\d+[⚔+✧❤♞☄✷ﬗ✯]*)"); // pattern from skyhanni

    public static boolean active() {
        boolean isActive = instance.isActive();
        if (isActive) {
            if (!Utils.isInDungeons()) {
                if (slayerOnly.value()) return SlayerUtil.bossAlive;
                if (dungeonsOnly.value()) return false;
            }
        }
        return isActive;
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (active() && pattern.matcher(event.namePlain.replaceAll(",", "")).matches()) {
            event.entity.setCustomNameVisible(false);
        }
    }
}
