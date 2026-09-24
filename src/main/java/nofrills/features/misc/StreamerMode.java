package nofrills.features.misc;

import com.google.common.base.Splitter;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.world.entity.player.PlayerSkin;
import nofrills.config.Feature;
import nofrills.config.SettingString;
import nofrills.events.*;
import nofrills.events.EventListener;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static nofrills.Main.mc;

@EventListener
public class StreamerMode {
    public static final Feature instance = new Feature("streamerMode");

    public static final SettingString baseName = new SettingString("nostrils-{}{}{}{}", "baseName", instance);
    public static final SettingString currentNick = new SettingString("", "currentNick", instance);

    public static final Supplier<Supplier<PlayerSkin>> skinSupplier = () -> mc.getSkinManager().createLookup(
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
    private static final Cache<String, Optional<String>> replacementCache = CacheBuilder.newBuilder()
            .weakKeys()
            .maximumSize(10000L)
            .build();
    private static final Cache<String, Object> lobbyCache = CacheBuilder.newBuilder()
            .maximumSize(10L)
            .build();
    private static final ConcurrentHashMap<String, String> playerToNick = new ConcurrentHashMap<>();
    private static final Random random = new Random();

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
            return !currentNick.value().isEmpty() && Utils.isInSkyblock() && !instancedIslands.contains(SkyblockData.getArea());
        }
        return false;
    }

    public static Optional<String> replaceIfNeeded(String text) {
        try {
            return replacementCache.get(text, () -> {
                String lower = Utils.toLower(text);
                if (lower.contains(Utils.toLower(currentNick.value()))) {
                    return Optional.of(text.replaceAll("(?i)" + currentNick.value(), mc.getUser().getName()));
                }
                for (Map.Entry<String, String> entry : playerToNick.entrySet()) {
                    if (lower.contains(entry.getKey())) {
                        return Optional.of(text.replaceAll("(?i)" + entry.getKey(), entry.getValue()));
                    }
                }
                for (String lobbyID : lobbyCache.asMap().keySet()) {
                    for (String prefix : lobbyPrefixes) {
                        String id = prefix + lobbyID;
                        if (text.contains(id)) {
                            lobbyCache.put(lobbyID, new Object()); // refresh entry expiry time
                            return Optional.of(text.replaceAll(id, "[REDACTED]"));
                        }
                    }
                }
                return Optional.empty();
            });
        } catch (Exception _) {
            return Optional.empty();
        }
    }

    @EventHandler
    private static void onPlayerJoined(PlayerJoinedEvent event) {
        if (instance.isActive() && event.isRealPlayer()) {
            String name = event.entry.getProfile().name();
            if (name.equals(currentNick.value())) return;
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
            if (event.messagePlain.equals("Your nick has been reset!")) {
                currentNick.set("");
            } else {
                String id = parseLobbyID(event.messagePlain);
                if (id.isEmpty()) return;
                for (String prefix : lobbyPrefixes) {
                    if (id.startsWith(prefix)) {
                        id = id.replace(prefix, "");
                        break;
                    }
                }
                lobbyCache.put(id, new Object());
            }
        }
    }

    @EventHandler
    private static void onSendPacket(SendPacketEvent event) {
        if (instance.isActive() && event.packet instanceof ServerboundChatCommandPacket(String command)) {
            List<String> parts = Splitter.on(" ").splitToList(command);
            if (parts.size() < 3) return;
            if (parts.get(0).equalsIgnoreCase("nick") && parts.get(1).equalsIgnoreCase("actuallyset")) {
                currentNick.set(parts.get(2));
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        playerToNick.clear();
    }
}
