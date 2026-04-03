package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.sound.SoundEvents;
import nofrills.config.Feature;
import nofrills.events.PlaySoundEvent;
import nofrills.misc.Utils;

public class MuteVampire {
    public static final Feature instance = new Feature("muteVampire");

    @EventHandler
    private static void onSound(PlaySoundEvent event) {
        if (instance.isActive() && Utils.isInChateau()) { // no boss check since you can also get blasted by someone else's boss
            if (event.isSound(SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE) || event.isSound(SoundEvents.ENTITY_WITHER_SPAWN)) {
                event.cancel();
            }
        }
    }
}
