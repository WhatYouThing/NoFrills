package nofrills.features.misc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import nofrills.config.DataFile;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.ChatMsgEvent;
import nofrills.events.GameShutdownEvent;
import nofrills.events.PlayerJoinedEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.misc.Utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static nofrills.Main.LOGGER;
import static nofrills.Main.mc;

public class BlockList {
    public static final Feature instance = new Feature("blockList");

    public static final SettingBool autoKick = new SettingBool(true, "autoKick", instance);
    public static final SettingBool joinAlert = new SettingBool(false, "joinAlert", instance);

    private static final DataFile data = new DataFile("BlockList.json");
    private static final ConcurrentHashMap<String, CachedResult> resultCache = new ConcurrentHashMap<>();

    public static List<JsonObject> getEntries() {
        return data.get().asMap().values().stream()
                .map(JsonElement::getAsJsonObject)
                .sorted(Comparator.comparingLong(object -> object.get("timestamp").getAsLong()))
                .collect(Collectors.toList());
    }

    private static void fetchUuid(String name, Consumer<Optional<FetchResult>> callback) {
        if (resultCache.containsKey(name)) {
            CachedResult cached = resultCache.get(name);
            if (!cached.isExpired()) {
                callback.accept(Optional.of(cached.get()));
                return;
            }
        }
        Thread.startVirtualThread(() -> {
            try {
                String url = Utils.format("https://api.minecraftservices.com/minecraft/profile/lookup/name/{}", name);
                InputStream connection = URI.create(url).toURL().openStream();
                JsonObject json = JsonParser.parseReader(new InputStreamReader(connection)).getAsJsonObject();
                if (json.has("id") && json.has("name")) {
                    String resultUuid = json.get("id").getAsString();
                    String resultName = json.get("name").getAsString();
                    FetchResult result = new FetchResult(resultUuid, resultName);
                    resultCache.put(name, new CachedResult(result));
                    callback.accept(Optional.of(result));
                } else {
                    callback.accept(Optional.empty());
                }
            } catch (Exception exception) {
                LOGGER.error("Failed to fetch player UUID for NoFrills Block List feature.", exception);
                callback.accept(Optional.empty());
            }
        });
    }

    public static void addPlayer(String name, String reason) {
        fetchUuid(name, (res) -> {
            if (res.isEmpty()) {
                Utils.infoFormat("§cCould not find UUID for player {}, not adding to the block list.", name);
                return;
            }
            FetchResult result = res.get();
            if (data.get().has(result.uuid())) {
                JsonObject object = data.get().get(result.uuid()).getAsJsonObject();
                object.addProperty("name", result.name());
                object.addProperty("reason", reason);
                Utils.infoFormat("§a{} is already blocked, updated the block reason.", result.name());
            } else {
                JsonObject object = new JsonObject();
                object.addProperty("name", result.name());
                object.addProperty("reason", reason);
                object.addProperty("timestamp", Utils.getTimestamp());
                data.get().add(result.uuid(), object);
                Utils.infoFormat("§aSuccessfully added {} to the block list.", result.name());
            }
            data.save();
        });
    }

    public static void removePlayer(String name) {
        if (data.get().entrySet().removeIf(entry -> entry.getValue().getAsJsonObject().get("name").getAsString().equalsIgnoreCase(name))) {
            Utils.infoFormat("§aSuccessfully removed {} from the block list.", name);
            data.save();
        } else {
            Utils.infoFormat("§c{} is not on the block list.", name);
        }
    }

    public static List<JsonObject> searchPlayer(String name) {
        return getEntries().stream().filter(object -> Utils.toLower(object.get("name").getAsString()).contains(Utils.toLower(name))).toList();
    }

    public static MutableComponent buildEntryLine(JsonObject entry, MutableComponent text) {
        MutableComponent tooltip = Component.literal(Utils.format("§7Block reason: {}\n§7Blocked date: {}\n§7Last known username: {}",
                entry.get("reason").getAsString(),
                Utils.parseDate(entry.get("timestamp").getAsLong()),
                entry.get("name").getAsString()
        ));
        return text.setStyle(Style.EMPTY.withHoverEvent(new HoverEvent.ShowText(tooltip)));
    }

    public static void printEntries(int page) {
        List<JsonObject> entries = BlockList.getEntries();
        int maxPage = (int) Math.ceil(entries.size() / 10.0);
        if (page > maxPage) {
            Utils.infoFormat("§7Provided page ({}) exceeds the existing number of pages ({}).", page, maxPage);
            return;
        }
        if (!entries.isEmpty()) {
            int start = Math.min(10 * (page - 1), entries.size() - 1);
            int end = Math.min((10 * page - 1) + 1, entries.size());
            List<JsonObject> sublist = entries.subList(start, end);
            MutableComponent message = Component.literal(Utils.format("§aBlock List (page {} out of {})", page, maxPage));
            for (int i = start; i < end; i++) {
                JsonObject entry = sublist.get(i - 10 * (page - 1));
                message.append(buildEntryLine(entry, Component.literal(Utils.format("\n §f{}. {}", i + 1, entry.get("name").getAsString()))));
            }
            Utils.infoRaw(message);
        } else {
            Utils.infoFormat("§7The block list is currently empty.");
        }
    }

    public static void forEntry(String name, Consumer<Optional<JsonObject>> callback) {
        fetchUuid(name, (res) -> {
            if (res.isPresent()) {
                FetchResult result = res.get();
                if (data.get().has(result.uuid())) {
                    callback.accept(Optional.of(data.get().get(result.uuid()).getAsJsonObject()));
                } else {
                    callback.accept(Optional.empty());
                }
            } else {
                callback.accept(Optional.empty());
            }
        });
    }

    @EventHandler
    private static void onMessage(ChatMsgEvent event) {
        if (instance.isActive() && autoKick.value() && event.msg().startsWith("Party Finder >") && event.msg().contains("joined")) {
            String name = event.msg().replace("Party Finder >", "").trim().split(" ", 2)[0];
            if (name.equalsIgnoreCase(mc.player.getName().getString())) {
                return;
            }
            forEntry(name, (obj) -> {
                Optional<Style> style = Utils.getStyle(event.message, (string) -> string.trim().startsWith(name));
                if (obj.isPresent()) {
                    Style nameColor = style.orElse(Style.EMPTY.applyFormat(ChatFormatting.GRAY));
                    Utils.infoRaw(buildEntryLine(obj.get(), Component.literal("§c§lAutomatically kicking blocked player §r")
                            .append(Component.literal(name).setStyle(nameColor)).append("§c§l."))
                    );
                    Utils.sendMessage("/party kick " + name);
                }
            });
        }
    }

    @EventHandler
    private static void onPlayerJoin(PlayerJoinedEvent event) {
        if (instance.isActive() && joinAlert.value() && event.isRealPlayer()) {
            String uuid = event.uuid.toString().replaceAll("-", "");
            if (data.get().has(uuid)) {
                Utils.infoRaw(buildEntryLine(data.get().get(uuid).getAsJsonObject(),
                        Component.literal(Utils.format("§c§lDetected a blocked player in this lobby: §r§c{}§r§c§l.", event.entry.getProfile().name())))
                );
            }
        }
    }

    @EventHandler
    private static void onShutdown(GameShutdownEvent event) {
        if (instance.isActive()) {
            data.saveBlocking();
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        resultCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    public record FetchResult(String uuid, String name) {
    }

    public static class CachedResult {
        private final FetchResult result;
        private long lastAccess;

        public CachedResult(FetchResult result) {
            this.result = result;
            this.lastAccess = Utils.getMeasuringTime();
        }

        public FetchResult get() {
            this.lastAccess = Utils.getMeasuringTime();
            return this.result;
        }

        public boolean isExpired() {
            return this.lastAccess + 1800000L <= Utils.getMeasuringTime();
        }
    }
}
