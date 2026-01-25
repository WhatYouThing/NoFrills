package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.config.SettingString;
import nofrills.events.ChatMsgEvent;
import nofrills.hud.HudManager;
import nofrills.misc.Utils;

public class PrinceMessage {
    public static final Feature instance = new Feature("princeMessage");

    public static final SettingString msg = new SettingString("/pc Prince Killed!", "msg", instance);

    @EventHandler
    private static void onMsg(ChatMsgEvent event) {
        if (event.messagePlain.equals("A Prince falls. +1 Bonus Score") && Utils.isInDungeons()) {
            if (instance.isActive()) {
                Utils.sendMessage(msg.value());
            }
            if (HudManager.dungeonScore.isActive()) {
                HudManager.dungeonScore.setPrinceKilled();
            }
        }
    }
}