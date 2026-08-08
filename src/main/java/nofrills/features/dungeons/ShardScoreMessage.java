package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.config.SettingString;
import nofrills.events.ChatMsgEvent;
import nofrills.events.EventListener;
import nofrills.misc.Utils;

@EventListener
public class ShardScoreMessage {
    public static final Feature instance = new Feature("shardScoreMessage");

    public static final SettingString msgPrince = new SettingString("/pc Prince Killed!", "msg", instance);
    public static final SettingString msgBat = new SettingString("/pc Bat Killed!", "msg", instance);

    @EventHandler
    private static void onMsg(ChatMsgEvent event) {
        if (event.messagePlain.equals("A Prince falls. +1 Bonus Score") && Utils.isInDungeons()) {
            if (instance.isActive()) {
                Utils.sendMessage(msgPrince.value());
            }
            if (ScoreCalculator.instance.isActive()) {
                ScoreCalculator.setPrinceKilled();
            }
        }
        if (event.messagePlain.equals("A Bat has been slain. +1 Bonus Score") && Utils.isInDungeons()) {
            if (instance.isActive()) {
                Utils.sendMessage(msgBat.value());
            }
            if (ScoreCalculator.instance.isActive()) {
                ScoreCalculator.setBatKilled();
            }
        }
    }
}