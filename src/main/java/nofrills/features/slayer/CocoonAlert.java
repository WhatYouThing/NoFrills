package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.sounds.SoundEvents;
import nofrills.config.Feature;
import nofrills.events.ChatMsgEvent;
import nofrills.hud.HudManager;
import nofrills.misc.Utils;

public class CocoonAlert {
    public static final Feature instance = new Feature("cocoonAlert");

    @EventHandler
    private static void onChat(ChatMsgEvent event) {
        if (instance.isActive() && event.messagePlain.trim().equals("YOU COCOONED YOUR SLAYER BOSS")) {
            HudManager.setCustomTitle("§c§lCOCOON!", 40);
            Utils.playSound(SoundEvents.NOTE_BLOCK_PLING, 1.0f, 0.0f);
        }
    }
}