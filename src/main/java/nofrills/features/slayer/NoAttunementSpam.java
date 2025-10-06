package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.events.ChatMsgEvent;

public class NoAttunementSpam {
    public static final Feature instance = new Feature("noAttunementSpam");

    @EventHandler
    private static void onChat(ChatMsgEvent event) {
        if (instance.isActive()) {
            String msg = event.messagePlain;
            if (msg.equals("Your hit was reduced by Hellion Shield!") || (msg.startsWith("Strike using the") && msg.endsWith("attunement on your dagger!"))) {
                event.cancel();
            }
        }
    }
}
