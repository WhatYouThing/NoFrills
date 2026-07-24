package nofrills.features.tweaks;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.sounds.SoundEvents;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.EventListener;
import nofrills.events.PlaySoundEvent;
import nofrills.misc.Utils;

@EventListener
public class MuteExplosion {
    public static final Feature instance = new Feature("muteExplosion");
    public static final SettingBool dungeonsOnly = new SettingBool(false, "dungeonsOnly", instance.key());

    @EventHandler
    private static void onSound(PlaySoundEvent event) {
        if (instance.isActive() && event.isSound(SoundEvents.GENERIC_EXPLODE) && (!dungeonsOnly.value() || Utils.isInDungeons()))
            event.cancel();
    }
}
