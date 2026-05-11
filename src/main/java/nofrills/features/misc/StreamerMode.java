package nofrills.features.misc;

import com.google.common.collect.Sets;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.math.random.Random;
import nofrills.config.Feature;
import nofrills.config.SettingString;
import nofrills.events.ChatMsgEvent;
import nofrills.events.PlayerJoinedEvent;
import nofrills.events.PlayerLeftEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static nofrills.Main.mc;

public class StreamerMode {
    public static final Feature instance = new Feature("streamerMode");

    public static final SettingString baseName = new SettingString("nostrils-{}{}{}{}", "baseName", instance);

    public static final Supplier<SkinTextures> skinSupplier = mc.getSkinProvider().supplySkinTextures(
            mc.getGameProfile(),
            false
    );
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

    private static String generateNick() {
        String nick = baseName.value().trim();
        Pattern pattern = Pattern.compile("\\{}");
        int count = (int) pattern.matcher(nick).results().count();
        if (count == 0) return nick;
        String digits = String.valueOf(random.nextInt((int) Math.pow(10, count)));
        if (digits.length() < count) {
            digits = "0".repeat(count - digits.length()) + digits;
        }
        return Utils.format(nick, (Object[]) digits.split(""));
    }

    public static boolean isActive() {
        if (instance.isActive()) {
            return !playerName.isEmpty() && !sessionName.equals(playerName) && !instancedIslands.contains(SkyblockData.getArea());
        }
        return false;
    }

    public static Optional<String> replaceIfNeeded(String text) {
        if (!text.isEmpty()) {
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
        }
        return Optional.empty();
    }

    @EventHandler
    private static void onPlayerJoined(PlayerJoinedEvent event) {
        if (instance.isActive() && event.isRealPlayer()) {
            String name = event.entry.getProfile().name();
            if (name.equals(playerName)) return;
            if (playerName.isEmpty()) {
                playerName = name;
                return;
            }
            String lower = Utils.toLower(name);
            if (!playerToNick.containsKey(lower)) {
                playerToNick.put(lower, generateNick());
            }
        }
    }

    @EventHandler
    private static void onPlayerLeft(PlayerLeftEvent event) {
        if (instance.isActive() && event.isRealPlayer()) {
            playerToNick.remove(Utils.toLower(event.entry.getProfile().name()));
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
        playerToNick.clear();
        playerName = "";
    }
}
