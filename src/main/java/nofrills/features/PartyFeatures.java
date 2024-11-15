package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Config;
import nofrills.events.ChatMsgEvent;
import nofrills.events.PartyChatMsgEvent;
import nofrills.misc.Utils;

import java.util.regex.Pattern;

import static nofrills.Main.mc;

public class PartyFeatures {
    private static final Pattern partyFinderPattern = Pattern.compile("Party Finder > .* joined .*");

    @EventHandler
    public static void onMessage(ChatMsgEvent event) {
        String msg = event.getPlainMessage();
        if (Config.partyQuickKick && msg.startsWith("Party Finder >") && partyFinderPattern.matcher(msg).matches()) {
            String name = msg.replace("Party Finder >", "").trim().split(" ", 2)[0].toLowerCase();
            if (name.equalsIgnoreCase(mc.getSession().getUsername())) {
                return;
            }
            Utils.infoButton("§6Click here to kick \"" + name + "\" from the party.", "/party kick " + name);
        }
    }

    @EventHandler
    public static void onPartyMessage(PartyChatMsgEvent event) {
        if (!Config.partyPrefixes.isEmpty() && !event.self) {
            String msg = event.message.toLowerCase();
            String author = event.sender.toLowerCase();
            for (String prefix : Config.partyPrefixes.split(" ")) {
                if (msg.startsWith(prefix.toLowerCase())) {
                    Config.partyBehaviorList privilege = Config.partyBehavior;
                    if (Config.partyWhitelist.contains(author)) {
                        privilege = Config.partyBehaviorList.Automatic;
                    }
                    if (Config.partyBlacklist.contains(author) || privilege == Config.partyBehaviorList.Ignore) {
                        return;
                    }
                    String command = msg.replace(prefix, "");
                    if (Config.partyCmdWarp && command.startsWith("warp")) {
                        if (privilege == Config.partyBehaviorList.Manual) {
                            Utils.infoButton("§6Click here to warp your party.", "/party warp");
                        } else {
                            Utils.sendMessage("/party warp");
                        }
                    }
                    if (Config.partyCmdTransfer && command.startsWith("ptme")) {
                        if (privilege == Config.partyBehaviorList.Manual) {
                            Utils.infoButton("§6Click here to promote " + event.sender + " as leader.", "/party transfer " + author);
                        } else {
                            Utils.sendMessage("/party transfer " + author);
                        }
                    }
                    if (Config.partyCmdTransfer && command.startsWith("allinv")) {
                        if (privilege == Config.partyBehaviorList.Manual) {
                            Utils.infoButton("§6Click here to toggle all invite.", "/party settings allinvite");
                        } else {
                            Utils.sendMessage("/party settings allinvite");
                        }
                    }
                }
            }
        }
    }
}
