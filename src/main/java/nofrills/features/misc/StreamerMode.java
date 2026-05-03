package nofrills.features.misc;

import com.google.common.collect.Sets;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.util.math.random.Random;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingString;
import nofrills.events.ChatMsgEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static nofrills.Main.mc;

public class StreamerMode {
    public static final Feature instance = new Feature("streamerMode");

    public static final SettingString baseName = new SettingString("nostrils-{}{}{}{}", "baseName", instance);
    public static final SettingBool debug = new SettingBool(false, "debug", instance);

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
    private static final ConcurrentHashMap<UUID, String> uuidToPlayer = new ConcurrentHashMap<>();
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
        return Utils.format(nick, (Object[]) digits.split(""));
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

    public static void onPlayerListUpdate(PlayerListS2CPacket packet) {
        if (instance.isActive()) {
            for (PlayerListS2CPacket.Entry entry : packet.getEntries()) {
                if (entry.displayName() != null || entry.profile() == null) continue;
                String name = entry.profile().name();
                if (name.trim().isEmpty() || name.contains(" ") || name.startsWith("!") || name.trim().equals(playerName))
                    continue;
                String lower = Utils.toLower(name);
                if (!playerToNick.containsKey(lower)) {
                    playerToNick.put(lower, generateNick());
                    uuidToPlayer.put(entry.profileId(), lower);
                }
            }
        }
    }

    public static void onPlayerListRemove(PlayerRemoveS2CPacket packet) {
        if (instance.isActive()) {
            for (UUID uuid : packet.profileIds()) {
                if (uuidToPlayer.containsKey(uuid)) {
                    String name = uuidToPlayer.get(uuid);
                    playerToNick.remove(name);
                    uuidToPlayer.remove(uuid);
                }
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
            playerToNick.clear();
            uuidToPlayer.clear();
            playerName = mc.player.getName().getString();
            if (debug.value()) {
                Utils.infoFormat("player name: {}, session name: {}", playerName, sessionName);
            }
        }
    }
}
