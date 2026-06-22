package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import nofrills.config.Feature;
import nofrills.events.ChatMsgEvent;
import nofrills.hud.HudManager;
import nofrills.misc.SlayerUtil;
import nofrills.misc.Utils;

public class SlayerMinibossAlert {
    public static final Feature instance = new Feature("slayerMinibossAlert");

    @EventHandler
    private static void onMessage(ChatMsgEvent event) {
        if (instance.isActive() && SlayerUtil.currentBoss != null && event.msg().startsWith("SLAYER MINI-BOSS ")) {
            for (String name : SlayerUtil.getMinibossNames()) {
                if (event.msg().contains(name)) {
                    Utils.getStyle(event.message, string -> string.trim().equals(name)).ifPresent(style -> {
                        HudManager.setCustomTitle(Component.literal(name).setStyle(style), 40);
                        Utils.playSound(SoundEvents.NOTE_BLOCK_PLING, 1.0f, 0.0f);
                    });
                    break;
                }
            }
        }
    }
}
