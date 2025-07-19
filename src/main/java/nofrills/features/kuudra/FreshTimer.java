package nofrills.features.kuudra;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingString;
import nofrills.events.ChatMsgEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.ServerTickEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.KuudraUtil;
import nofrills.misc.Utils;

public class FreshTimer {
    public static final Feature instance = new Feature("freshTimer");

    public static final SettingBool send = new SettingBool(false, "send", instance.key());
    public static final SettingString message = new SettingString("/pc FRESH!", "message", instance.key());

    private static int freshTicks = 0;

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && Utils.isInKuudra()) {
            if (freshTicks > 0 && KuudraUtil.getCurrentPhase() == KuudraUtil.phase.Build) {
                Utils.showTitleCustom(Utils.format("FRESH: {}s", Utils.formatDecimal(freshTicks / 20f)), 1, 25, 2.5f, 0x55ff55);
            }
        }
    }

    @EventHandler
    private static void onChatMsg(ChatMsgEvent event) {
        if (instance.isActive() && Utils.isInKuudra() && event.messagePlain.equals("Your Fresh Tools Perk bonus doubles your building speed for the next 10 seconds!")) {
            freshTicks = 200;
            if (send.value() && !message.value().isEmpty()) {
                Utils.sendMessage(message.value());
            }
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (instance.isActive() && Utils.isInKuudra()) {
            if (freshTicks > 0) {
                freshTicks--;
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        freshTicks = 0;
    }
}
