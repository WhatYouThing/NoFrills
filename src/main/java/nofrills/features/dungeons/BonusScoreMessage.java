package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingString;
import nofrills.events.ChatMsgEvent;
import nofrills.events.EventListener;
import nofrills.misc.DungeonUtil;
import nofrills.misc.Utils;

@EventListener
public class BonusScoreMessage {
    public static final Feature instance = new Feature("bonusScoreMessage");

    public static final SettingBool sendPrince = new SettingBool(false, "sendPrince", instance);
    public static final SettingString msgPrince = new SettingString("/pc Prince Killed!", "msgPrince", instance);
    public static final SettingBool sendBat = new SettingBool(false, "sendBat", instance);
    public static final SettingString msgBat = new SettingString("/pc Bat Killed!", "msgBat", instance);

    @EventHandler
    private static void onMsg(ChatMsgEvent event) {
        if (instance.isActive() && Utils.isInDungeons()) {
            if (sendPrince.value() && DungeonUtil.isPrinceScoreMessage(event.msg())) {
                Utils.sendMessage(msgPrince.value());
            }
            if (sendBat.value() && DungeonUtil.isBatScoreMessage(event.msg())) {
                Utils.sendMessage(msgBat.value());
            }
        }
    }
}