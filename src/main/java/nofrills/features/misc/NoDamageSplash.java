package nofrills.features.misc;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.config.SettingEnum;
import nofrills.events.EntityNamedEvent;
import nofrills.misc.SlayerUtil;
import nofrills.misc.Utils;

import java.util.regex.Pattern;

public class NoDamageSplash {
    public static final Feature instance = new Feature("noDamageSplash");

    public static final SettingEnum<HideMode> mode = new SettingEnum<>(HideMode.Always, HideMode.class, "mode", instance);

    private static final Pattern pattern = Pattern.compile("[0-9]*");

    private static boolean isActive() {
        if (instance.isActive()) {
            return switch (mode.value()) {
                case Always -> true;
                case SlayerOnly -> SlayerUtil.bossAlive;
                case DungeonsOnly -> Utils.isInDungeons();
                case Both -> SlayerUtil.bossAlive || Utils.isInDungeons();
            };
        }
        return false;
    }

    private static boolean isSplash(String name) {
        return pattern.matcher(Utils.toAscii(name.replaceAll(",", ""))).matches();
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (isActive() && isSplash(event.namePlain)) {
            event.entity.setCustomNameVisible(false);
        }
    }

    public enum HideMode {
        Always,
        SlayerOnly,
        DungeonsOnly,
        Both
    }
}
