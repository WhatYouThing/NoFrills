package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.sounds.SoundEvents;
import nofrills.config.Feature;
import nofrills.events.PlaySoundEvent;

public class MuteEnderman {
    public static final Feature instance = new Feature("muteEnderman");

    @EventHandler
    private static void onSound(PlaySoundEvent event) {
        if (instance.isActive() && (event.isSound(SoundEvents.ENDERMAN_STARE) || event.isSound(SoundEvents.ENDERMAN_SCREAM))) {
            event.cancel();
        }
    }
}