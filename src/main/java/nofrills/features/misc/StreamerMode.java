package nofrills.features.misc;

import com.google.common.collect.Sets;
import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.events.ChatMsgEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static nofrills.Main.mc;

public class StreamerMode {
    public static final Feature instance = new Feature("streamerMode");

    private static final List<String> lobbyPrefixes = List.of(
            "mini",
            "mega",
            "m",
            "M"
    );
    private static final HashSet<String> instancedIslands = Sets.newHashSet(
            "Catacombs",
            "Kuudra",
            "Mineshaft"
    );
    private static final String sessionName = mc.getSession().getUsername();
    private static String playerName = "";
    private static String lobbyID = "";

    private static String parseLobbyID(String msg) {
        if (msg.startsWith("Request join for Hub ") || msg.startsWith("Sending to server ")) {
            if (msg.contains("#")) {
                return msg.substring(msg.indexOf("(") + 1, msg.indexOf(")"));
            }
            return msg.substring(msg.lastIndexOf(" ") + 1).replace("...", "");
        }
        return "";
    }

    public static boolean isActive() {
        if (instance.isActive()) {
            return !playerName.isEmpty() && !sessionName.equals(playerName) && !instancedIslands.contains(SkyblockData.getArea());
        }
        return false;
    }

    public static Optional<String> replaceIfNeeded(String text) {
        if (text.isEmpty()) {
            return Optional.empty();
        }
        if (Utils.toLower(text).contains(Utils.toLower(StreamerMode.playerName))) {
            return Optional.of(text.replaceAll("(?i)" + StreamerMode.playerName, StreamerMode.sessionName));
        }
        if (!lobbyID.isEmpty()) {
            for (String prefix : lobbyPrefixes) {
                String id = prefix + lobbyID;
                if (text.contains(id)) {
                    return Optional.of(text.replaceAll(id, "[REDACTED]"));
                }
            }
        }
        return Optional.empty();
    }

    @EventHandler
    private static void onChat(ChatMsgEvent event) {
        if (instance.isActive()) {
            String id = parseLobbyID(event.messagePlain);
            if (id.isEmpty()) return;
            for (String prefix : lobbyPrefixes) {
                if (id.startsWith(prefix)) {
                    id = id.replace(prefix, "");
                    break;
                }
            }
            lobbyID = id;
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        if (instance.isActive()) {
            playerName = mc.player.getName().getString();
        }
    }
}
