package nofrills.features.misc;

import com.google.common.collect.Sets;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.random.Random;
import nofrills.config.Feature;
import nofrills.config.SettingString;
import nofrills.events.ChatMsgEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import static nofrills.Main.mc;

public class StreamerMode {
    public static final Feature instance = new Feature("streamerMode");

    public static final SettingString baseName = new SettingString("nostrils-{}{}{}{}", "baseName", instance);

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
    private static final ConcurrentHashMap<String, String> playerToNick = new ConcurrentHashMap<>();
    private static final Random random = Random.createLocal();
    private static final CopyOnWriteArrayList<String> lobbyIDs = new CopyOnWriteArrayList<>();
    private static final String sessionName = mc.getSession().getUsername();
    private static String playerName = "";

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
        String lower = Utils.toLower(text);
        if (lower.contains(Utils.toLower(StreamerMode.playerName))) {
            return Optional.of(text.replaceAll("(?i)" + StreamerMode.playerName, StreamerMode.sessionName));
        }
        for (Map.Entry<String, String> entry : playerToNick.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return Optional.of(text.replaceAll("(?i)" + entry.getKey(), entry.getValue()));
            }
        }
        for (String lobbyID : lobbyIDs) {
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
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive()) {
            List<String> list = Utils.getTabListLines();
            List<String> names = new ArrayList<>();
            for (String line : list) {
                if (line.equals("Info")) break;
                if (list.isEmpty() || !Pattern.matches("\\[[0-9]*] .*", line)) continue;
                int nameStart = line.lastIndexOf("]") + 2;
                int nameEnd = line.indexOf(" ", nameStart);
                String name = Utils.toLower(line.substring(nameStart, nameEnd != -1 ? nameEnd : line.length())).trim();
                if (name.equalsIgnoreCase(sessionName)) continue;
                if (!playerToNick.containsKey(name)) {
                    String nick = baseName.value().trim();
                    int count = 0;
                    int lastIndex = -2;
                    while (lastIndex != -1) {
                        int index = nick.indexOf("{}", lastIndex + 2);
                        if (index != -1) {
                            count++;
                        }
                        lastIndex = index;
                    }
                    String digits = String.valueOf(random.nextInt((int) Math.pow(10, count)));
                    if (digits.length() < count) {
                        digits = "0".repeat(count - digits.length()) + digits;
                    }
                    playerToNick.put(name, Utils.format(nick, (Object[]) digits.split("")));
                }
                names.add(name);
            }
            if (!playerToNick.isEmpty()) {
                playerToNick.entrySet().removeIf(entry -> !names.contains(entry.getKey()));
            }
        }
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
            if (lobbyIDs.size() == 5) {
                lobbyIDs.removeLast();
            }
            lobbyIDs.addFirst(id);
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        if (instance.isActive()) {
            playerName = mc.player.getName().getString();
        }
    }
}
