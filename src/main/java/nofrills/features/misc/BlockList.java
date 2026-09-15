package nofrills.features.misc;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import nofrills.config.*;
import nofrills.events.ChatMsgEvent;
import nofrills.events.EventListener;
import nofrills.events.PlayerJoinedEvent;
import nofrills.misc.Utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static nofrills.Main.LOGGER;
import static nofrills.Main.mc;

@EventListener
public class BlockList {
    public static final Feature instance = new Feature("blockList");

    public static final SettingBool autoKick = new SettingBool(true, "autoKick", instance);
    public static final SettingBool sendKick = new SettingBool(false, "sendKick", instance);
    public static final SettingString kickMsg = new SettingString("/pc Kicked {name} for: Player blocked with reason \"{reason}\"", "kickMsg", instance);
    public static final SettingBool joinAlert = new SettingBool(false, "joinAlert", instance);

    private static final DataFile data = Config.getDataFile("BlockList.json");
    private static final Cache<String, FetchResult> resultCache = CacheBuilder
            .newBuilder()
            .expireAfterAccess(Duration.ofMinutes(30L))
            .maximumSize(1000L)
            .build();
    private static final Pattern validNameRegex = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");

    public static List<JsonObject> getEntries() {
        return data.get().asMap().values().stream()
                .map(JsonElement::getAsJsonObject)
                .sorted(Comparator.comparingLong(object -> object.get("timestamp").getAsLong()))
                .collect(Collectors.toList());
    }

    public static void addPlayer(String name, String reason) {
        fetchUuid(name, (res) -> {
            if (res.isEmpty()) {
                Utils.infoRaw(Utils.formatText("Could not find UUID for player {}, not adding to the block list.", name).withStyle(ChatFormatting.RED));
                return;
            }
            FetchResult result = res.get();
            if (data.get().has(result.uuid())) {
                JsonObject object = data.get().get(result.uuid()).getAsJsonObject();
                Utils.infoRaw(Utils.formatText("{} is already blocked, updating entry. Previous name: {}. Previous block reason: \"{}\".",
                        result.name(),
                        object.get("name").getAsString(),
                        object.get("reason").getAsString()
                ).withStyle(ChatFormatting.GREEN));
                object.addProperty("name", result.name());
                object.addProperty("reason", reason);
            } else {
                JsonObject object = new JsonObject();
                object.addProperty("name", result.name());
                object.addProperty("reason", reason);
                object.addProperty("timestamp", Utils.getTimestamp());
                data.get().add(result.uuid(), object);
                Utils.infoRaw(Utils.formatText("Successfully added {} to the block list.", result.name()).withStyle(ChatFormatting.GREEN));
            }
            data.save();
        });
    }

    public static void removePlayer(String name) {
        Optional<Map.Entry<String, JsonElement>> entry = data.get().entrySet().stream()
                .filter(e -> e.getValue().getAsJsonObject().get("name").getAsString().equalsIgnoreCase(name))
                .findFirst();
        if (entry.isPresent()) {
            JsonObject object = entry.get().getValue().getAsJsonObject();
            Utils.infoRaw(Utils.formatText("Successfully removed {} from the block list.", object.get("name").getAsString())
                    .withStyle(ChatFormatting.GREEN)
            );
            data.get().remove(entry.get().getKey());
            data.save();
        } else {
            Utils.infoRaw(Utils.formatText("§c{} is not on the block list.", name).withStyle(ChatFormatting.RED));
        }
    }

    public static List<JsonObject> searchPlayer(String name) {
        return getEntries().stream().filter(object -> Utils.toLower(object.get("name").getAsString()).contains(Utils.toLower(name))).toList();
    }

    public static void importFromClipboard(String defaultReason) {
        String clipboard = mc.keyboardHandler.getClipboard();
        try {
            List<String> addedPlayers = new ArrayList<>();
            JsonObject json = JsonParser.parseString(clipboard).getAsJsonObject();
            for (Map.Entry<String, JsonElement> e : json.entrySet()) {
                String uuid = e.getKey().replaceAll("-", "");
                JsonObject entry = e.getValue().getAsJsonObject();
                if (data.get().has(uuid)) {
                    continue;
                }
                String entryName = entry.has("name") ? entry.get("name").getAsString() : "";
                JsonObject object = new JsonObject();
                object.addProperty("name", !entryName.isEmpty() && validNameRegex.matcher(entryName).matches() ? entryName : "N/A");
                object.addProperty("reason", entry.has("reason") ? entry.get("reason").getAsString() : defaultReason);
                object.addProperty("timestamp", Utils.getTimestamp());
                data.get().add(uuid, object);
                addedPlayers.add(uuid);
            }
            Thread.startVirtualThread(() -> {
                Utils.infoRaw(Utils.formatText("Importing {} ({} unresolved) new block list entries out of {} total...",
                        addedPlayers.size(),
                        addedPlayers.stream().filter(u -> data.get().get(u).getAsJsonObject().get("name").getAsString().equals("N/A")).count(),
                        json.size()
                ).withStyle(ChatFormatting.GRAY));
                for (String uuid : addedPlayers) {
                    if (data.get().get(uuid).getAsJsonObject().get("name").getAsString().equals("N/A")) {
                        try {
                            String url = Utils.format("https://api.minecraftservices.com/minecraft/profile/lookup/{}", uuid);
                            InputStream connection = URI.create(url).toURL().openStream();
                            JsonObject obj = JsonParser.parseReader(new InputStreamReader(connection)).getAsJsonObject();
                            if (obj.has("id") && obj.has("name")) {
                                data.get().get(uuid).getAsJsonObject().addProperty("name", obj.get("name").getAsString());
                            }
                            Thread.sleep(1000); // need small delay between requests to not get rate limited
                        } catch (Exception exception) {
                            LOGGER.error("Failed to fetch player name for NoFrills Block List import.", exception);
                        }
                    }
                }
                Utils.infoRaw(Utils.formatText("Successfully imported {} block list entries.", addedPlayers.size()).withStyle(ChatFormatting.GREEN));
            });
        } catch (Exception exception) {
            Utils.infoRaw(Component.literal("Unable to parse JSON data from clipboard, not importing players.").withStyle(ChatFormatting.RED));
        }
    }

    public static MutableComponent buildEntryLine(JsonObject entry, MutableComponent text) {
        MutableComponent tooltip = Component.literal(Utils.format("§7Block reason: {}\n§7Blocked date: {}\n§7Last known username: {}",
                entry.get("reason").getAsString(),
                Utils.parseDate(entry.get("timestamp").getAsLong()),
                entry.get("name").getAsString()
        ));
        return text.setStyle(text.getStyle().withHoverEvent(new HoverEvent.ShowText(tooltip)));
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

    private static void fetchUuid(String name, Consumer<Optional<FetchResult>> callback) {
        Thread.startVirtualThread(() -> {
            try {
                FetchResult result = resultCache.get(Utils.toLower(name), () -> {
                    String url = Utils.format("https://api.minecraftservices.com/minecraft/profile/lookup/name/{}", name);
                    InputStream connection = URI.create(url).toURL().openStream();
                    JsonObject json = JsonParser.parseReader(new InputStreamReader(connection)).getAsJsonObject();
                    return new FetchResult(json.get("id").getAsString(), json.get("name").getAsString());
                });
                callback.accept(Optional.of(result));
            } catch (Exception exception) {
                LOGGER.error("Failed to fetch player UUID for NoFrills Block List feature.", exception);
                callback.accept(Optional.empty());
            }
        });
    }

    private static void kickPlayer(String name, long delay) {
        Thread.startVirtualThread(() -> {
            try {
                Thread.sleep(delay);
                mc.execute(() -> Utils.sendMessage("/party kick " + name));
            } catch (Exception _) {
            }
        });
    }

    @EventHandler
    private static void onMessage(ChatMsgEvent event) {
        if (instance.isActive() && event.isPartyFinderJoin()) {
            String name = event.getPartyFinderJoinName();
            if (name.equalsIgnoreCase(mc.player.getName().getString())) {
                return;
            }
            Optional<Style> style = Utils.getStyle(event.message, (string) -> string.trim().startsWith(name));
            Style nameColor = style.orElse(Style.EMPTY.applyFormat(ChatFormatting.GRAY));
            fetchUuid(name, (res) -> {
                if (res.isPresent()) {
                    FetchResult result = res.get();
                    if (data.get().has(result.uuid())) {
                        JsonObject obj = data.get().get(result.uuid()).getAsJsonObject();
                        MutableComponent msg = Component.literal(name).setStyle(nameColor)
                                .append(Component.literal(" is a blocked player.").withStyle(ChatFormatting.RED));
                        if (autoKick.value()) {
                            msg.append(Component.literal(" Kicking automatically.").withStyle(ChatFormatting.RED));
                            if (sendKick.value()) {
                                Utils.sendMessage(kickMsg.value()
                                        .replaceAll("\\{name}", name)
                                        .replaceAll("\\{reason}", obj.get("reason").getAsString())
                                );
                                kickPlayer(name, 500L);
                            } else {
                                kickPlayer(name, 0L);
                            }
                        }
                        Utils.infoRaw(buildEntryLine(obj, msg));
                    }
                } else {
                    Utils.infoRaw(Component.literal("Failed to fetch UUID for ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(name).setStyle(nameColor))
                            .append(Component.literal(", unable to check blocklist status.").withStyle(ChatFormatting.GRAY)));
                }
            });
        }
    }

    @EventHandler
    private static void onPlayerJoin(PlayerJoinedEvent event) {
        if (instance.isActive() && joinAlert.value() && event.isRealPlayer()) {
            String uuid = event.uuid.toString().replaceAll("-", "");
            if (data.get().has(uuid)) {
                Utils.infoRaw(buildEntryLine(
                        data.get().get(uuid).getAsJsonObject(),
                        Utils.formatText("Blocked player found: {}.", event.entry.getProfile().name())
                                .withStyle(ChatFormatting.RED)
                ));
            }
        }
    }

    public record FetchResult(String uuid, String name) {
    }
}
