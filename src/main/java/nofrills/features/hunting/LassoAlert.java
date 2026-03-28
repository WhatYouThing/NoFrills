package nofrills.features.hunting;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundEvents;
import nofrills.config.Feature;
import nofrills.events.EntityNamedEvent;
import nofrills.misc.Utils;

public class LassoAlert {
    public static final Feature instance = new Feature("lassoAlert");

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && event.namePlain.equals("REEL") && Utils.getHeldItem().getItem().equals(Items.LEAD)) {
            Utils.playSound(SoundEvents.NOTE_BLOCK_PLING, 1.0f, 1.0f);
        }
    }
}