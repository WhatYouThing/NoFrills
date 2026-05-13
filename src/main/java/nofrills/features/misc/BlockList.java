package nofrills.features.misc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import nofrills.config.Config;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.ChatMsgEvent;
import nofrills.events.PlayerJoinedEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.misc.Utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private static final Path listPath = Config.getFolderPath().resolve("BlockList.json");
    private static final JsonObject data = loadData();
    private static final ConcurrentHashMap<String, CachedResult> resultCache = new ConcurrentHashMap<>();

    private static JsonObject loadData() {
        if (Files.exists(listPath)) {
            try {
                return JsonParser.parseString(Files.readString(listPath)).getAsJsonObject();
            } catch (Exception exception) {
                LOGGER.error("Unable to load NoFrills Block List file!", exception);
            }
        }
        return new JsonObject();
    }

    private static void saveData() {
        Thread.startVirtualThread(() -> {
            try {
                Utils.atomicWrite(listPath, data);
            } catch (Exception exception) {
                LOGGER.error("Unable to save NoFrills Block List file!", exception);
            }
        });
    }

    public static List<JsonObject> getEntries() {
        return data.asMap().values().stream()
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
            if (data.has(result.uuid())) {
                JsonObject object = data.get(result.uuid()).getAsJsonObject();
                object.addProperty("name", result.name());
                object.addProperty("reason", reason);
                Utils.infoFormat("§a{} is already blocked, updated the block reason.", result.name());
            } else {
                JsonObject object = new JsonObject();
                object.addProperty("name", result.name());
                object.addProperty("reason", reason);
                object.addProperty("timestamp", Utils.getTimestamp());
                data.add(result.uuid(), object);
                Utils.infoFormat("§aSuccessfully added {} to the block list.", result.name());
            }
            saveData();
        });
    }

    public static void removePlayer(String name) {
        if (data.entrySet().removeIf(entry -> entry.getValue().getAsJsonObject().get("name").getAsString().equalsIgnoreCase(name))) {
            Utils.infoFormat("§aSuccessfully removed {} from the block list.", name);
            saveData();
        } else {
            Utils.infoFormat("§c{} is not on the block list.", name);
        }
    }

    public static List<JsonObject> searchPlayer(String name) {
        return getEntries().stream().filter(object -> Utils.toLower(object.get("name").getAsString()).contains(Utils.toLower(name))).toList();
    }

    public static MutableText buildEntryLine(JsonObject entry, MutableText text) {
        MutableText tooltip = Text.literal(Utils.format("§7Block reason: {}\n§7Blocked date: {}\n§7Last known username: {}",
                entry.get("reason").getAsString(),
                Utils.parseDate(entry.get("timestamp").getAsLong()),
                entry.get("name").getAsString()
        ));
        return text.setStyle(text.getStyle().withHoverEvent(new HoverEvent.ShowText(tooltip)));
    }

    public static MutableText buildEntryLine(JsonObject entry, String text) {
        return buildEntryLine(entry, Text.literal(text));
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
            MutableText message = Text.literal(Utils.format("§aBlock List (page {} out of {})", page, maxPage));
            for (int i = start; i < end; i++) {
                JsonObject entry = sublist.get(i - 10 * (page - 1));
                message.append(buildEntryLine(entry, Utils.format("\n §f{}. {}", i + 1, entry.get("name").getAsString())));
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
                if (data.has(result.uuid())) {
                    callback.accept(Optional.of(data.get(result.uuid()).getAsJsonObject()));
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
                    Style nameColor = style.orElse(Style.EMPTY.withFormatting(Formatting.GRAY));
                    Utils.infoRaw(buildEntryLine(obj.get(), Text.literal("§c§lAutomatically kicking blocked player §r")
                            .append(Text.literal(name).setStyle(nameColor)).append("§c§l."))
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
            if (data.has(uuid)) {
                Utils.infoRaw(buildEntryLine(data.get(uuid).getAsJsonObject(),
                        Utils.format("§c§lDetected a blocked player in this lobby: §r§c{}§r§c§l.", event.entry.getProfile().name()))
                );
            }
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
