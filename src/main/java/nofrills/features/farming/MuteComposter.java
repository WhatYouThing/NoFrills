package nofrills.features.farming;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.events.PlaySoundEvent;
import nofrills.misc.Utils;

public class MuteComposter {
    public static final Feature instance = new Feature("muteComposter");

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
        if (instance.isActive() && isComposterSound(event) && Utils.isInGarden()) {
            event.cancel();
        }
    }
}
