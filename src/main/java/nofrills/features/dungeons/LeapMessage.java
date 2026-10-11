package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.config.SettingString;
import nofrills.events.ChatMsgEvent;
import nofrills.events.EventListener;
import nofrills.misc.DungeonUtil;
import nofrills.misc.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EventListener
public class LeapMessage {
    public static final Feature instance = new Feature("leapMessage");

    public static final SettingString message = new SettingString("/pc Leaped to {name}!", "message", instance);

    private static final Pattern pattern = Pattern.compile("You have teleported to (?<name>.*)!");

    @EventHandler
    private static void onMsg(ChatMsgEvent event) {
        if (instance.isActive() && Utils.isInDungeons() && !DungeonUtil.isGhost()) {
            Matcher matcher = pattern.matcher(event.msg());
            if (!matcher.matches()) return;
            Utils.sendMessage(message.value().replaceAll("\\{name}", matcher.group("name")));
        }
    }
}
