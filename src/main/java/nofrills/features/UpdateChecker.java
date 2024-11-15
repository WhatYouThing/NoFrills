package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Config;
import nofrills.events.ChatMsgEvent;
import nofrills.misc.Utils;

import java.util.regex.Pattern;

import static nofrills.Main.eventBus;

public class UpdateChecker {
    private static final Pattern joinedSkyblockRegex = Pattern.compile("Latest update: SkyBlock .* CLICK");

    @EventHandler
    public static void onChatMsg(ChatMsgEvent event) {
        if (Config.updateChecker) {
            String msg = event.getPlainMessage();
            if (msg.startsWith("Latest update") && joinedSkyblockRegex.matcher(msg).matches()) {
                Utils.checkUpdate(false);
                eventBus.unsubscribe(UpdateChecker.class);
            }
        }
    }
}
