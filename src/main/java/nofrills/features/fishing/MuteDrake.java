package nofrills.features.fishing;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.sound.SoundEvents;
import nofrills.config.Feature;
import nofrills.events.PlaySoundEvent;
import nofrills.misc.Utils;

public class MuteDrake {
    public static final Feature instance = new Feature("muteDrake");

    @EventHandler
    private static void onSound(PlaySoundEvent event) {
        if (instance.isActive() && Utils.isInArea("Jerry's Workshop") && event.isSound(SoundEvents.ITEM_TOTEM_USE)) {
            event.cancel();
        }
    }
}
