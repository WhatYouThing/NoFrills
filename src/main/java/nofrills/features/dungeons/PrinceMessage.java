package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.config.SettingString;
import nofrills.events.ChatMsgEvent;
import nofrills.misc.Utils;

public class PrinceMessage {
    public static final Feature instance = new Feature("princeMessage");

    public static final SettingString msg = new SettingString("/pc Prince Shard activated, +1 score!", "msg", instance);

    @EventHandler
    private static void onMsg(ChatMsgEvent event) {
        if (instance.isActive() && Utils.isInDungeons() && event.messagePlain.equals("A Prince falls. +1 Bonus Score")) {
            Utils.sendMessage(msg.value());
        }
    }
}