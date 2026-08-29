package nofrills.features.kuudra;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingString;
import nofrills.events.ChatMsgEvent;
import nofrills.events.EventListener;
import nofrills.misc.Utils;

@EventListener
public class FreshTimer {
    public static final Feature instance = new Feature("freshTimer");

    public static final SettingBool send = new SettingBool(false, "send", instance.key());
    public static final SettingString message = new SettingString("/pc FRESH!", "message", instance.key());

    @EventHandler
    private static void onChatMsg(ChatMsgEvent event) {
        if (instance.isActive() && event.msg().equals("Your Fresh Tools Perk bonus doubles your building speed for the next 10 seconds!") && Utils.isInKuudra()) {
            if (send.value() && !message.value().isEmpty()) {
                Utils.sendMessage(message.value());
            }
        }
    }
}
