package nofrills.features.general;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingEnum;
import nofrills.events.WorldTickEvent;

import static nofrills.Main.mc;

public class Fullbright {
    public static final Feature instance = new Feature("fullbright");

    public static final SettingEnum<modes> mode = new SettingEnum<>(modes.Gamma, modes.class, "mode", instance.key());
    public static final SettingBool noEffect = new SettingBool(false, "noEffect", instance.key());

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && mc.player != null) {
            if (mode.value().equals(modes.Potion)) {
                mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 840));
            }
            if (noEffect.value() && !mode.value().equals(modes.Potion)) {
                mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            }
        }
    }

    public enum modes {
        Gamma,
        Ambient,
        Potion
    }
}
