package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.sound.SoundEvents;
import nofrills.config.Feature;
import nofrills.events.PlaySoundEvent;

public class MuteEnderman {
    public static final Feature instance = new Feature("muteEnderman");

    @EventHandler
    private static void onSound(PlaySoundEvent event) {
        if (instance.isActive() && (event.isSound(SoundEvents.ENTITY_ENDERMAN_STARE) || event.isSound(SoundEvents.ENTITY_ENDERMAN_SCREAM))) {
            event.cancel();
        }
    }
}