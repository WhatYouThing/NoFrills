package nofrills.features.general;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingEnum;
import nofrills.events.WorldTickEvent;

import static nofrills.Main.mc;

public class Fullbright {
    public static final Feature instance = new Feature("fullbright");

    public static final SettingEnum<Mode> mode = new SettingEnum<>(Mode.Ambient, Mode.class, "mode", instance);
    public static final SettingBool noEffect = new SettingBool(false, "noEffect", instance);

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && mc.player != null) {
            if (mode.value().equals(Mode.Potion)) {
                mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 840));
            } else if (noEffect.value()) {
                mc.player.removeEffect(MobEffects.NIGHT_VISION);
            }
        }
    }

    public enum Mode {
        Ambient,
        Gamma,
        Potion
    }
}
