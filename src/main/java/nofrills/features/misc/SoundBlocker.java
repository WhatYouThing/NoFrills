package nofrills.features.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.sounds.SoundEvents;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingEnum;
import nofrills.events.EventListener;
import nofrills.events.PlaySoundEvent;
import nofrills.misc.Utils;

@EventListener
public class SoundBlocker {
    public static final Feature instance = new Feature("soundBlocker");

    public static final SettingEnum<ExplosionMode> explosions = new SettingEnum<>(ExplosionMode.Disabled, ExplosionMode.class, "explosions", instance);
    public static final SettingBool angryEnderman = new SettingBool(false, "angryEnderman", instance);
    public static final SettingBool vampireAbilities = new SettingBool(false, "vampireAbilities", instance);
    public static final SettingBool reindrakeGifts = new SettingBool(false, "reindrakeGifts", instance);
    public static final SettingBool composter = new SettingBool(false, "composter", instance);

    private static boolean isComposterSound(PlaySoundEvent event) {
        if (event.pitch() == 0.5873016f) {
            return (event.isSound("minecraft:entity.wolf.growl") && event.volume() == 0.15f)
                    || (event.isSound("minecraft:block.water.ambient") && event.volume() == 0.25f);
        }
        if (event.volume() == 1.0f) {
            return (event.isSound("minecraft:block.piston.extend") && event.pitch() == 1.4920635f)
                    || (event.isSound("minecraft:entity.chicken.egg") && event.pitch() == 0.7936508f);
        }
        return false;
    }

    @EventHandler
    private static void onSound(PlaySoundEvent event) {
        if (instance.isActive()) {
            if (!explosions.value().equals(ExplosionMode.Disabled) && event.isSound(SoundEvents.GENERIC_EXPLODE)) {
                if (explosions.value().equals(ExplosionMode.DungeonsOnly) && !Utils.isInDungeons()) {
                    return;
                }
                event.cancel();
            }
            if (reindrakeGifts.value() && event.isSound(SoundEvents.TOTEM_USE) && Utils.isInArea("Jerry's Workshop")) {
                event.cancel();
            }
            if (vampireAbilities.value() && (event.isSound(SoundEvents.ELDER_GUARDIAN_CURSE) || event.isSound(SoundEvents.WITHER_SPAWN)) && Utils.isInChateau()) {
                event.cancel();
            }
            if (composter.value() && isComposterSound(event) && Utils.isInGarden()) {
                event.cancel();
            }
        }
    }

    public enum ExplosionMode {
        Disabled("Disabled"),
        DungeonsOnly("Dungeons Only"),
        Always("Always");

        private final String displayName;

        ExplosionMode(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}
