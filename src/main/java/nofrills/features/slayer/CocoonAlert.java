package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.sound.SoundEvents;
import nofrills.config.Feature;
import nofrills.events.ChatMsgEvent;
import nofrills.misc.Utils;

public class CocoonAlert {
    public static final Feature instance = new Feature("cocoonAlert");

    @EventHandler
    private static void onChat(ChatMsgEvent event) {
        if (instance.isActive() && Utils.isInSkyblock() && event.messagePlain.trim().equals("YOU COCOONED YOUR SLAYER BOSS")) {
            Utils.showTitle("§c§lCOCOON!", "", 0, 30, 10);
            Utils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
        }
    }
}