package nofrills.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.SharedConstants;
import nofrills.config.APIFlags;
import nofrills.config.Feature;
import nofrills.events.EventListener;
import nofrills.events.WorldTickEvent;
import nofrills.features.tweaks.LegacyTextures;
import nofrills.features.tweaks.MiddleClickOverride;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static nofrills.Main.LOGGER;
import static nofrills.Main.mc;

@EventListener
public class NoFrillsAPI {
    private static final HttpClient httpClient = HttpClient.newBuilder().build();
    private static final List<RefreshTask> tasks = List.of(
            new RefreshTask(1200, APIFlags.UsePricingAPI, _ -> Thread.startVirtualThread(() -> {
                try {
                    JsonObject json = makeRequestObject("v2/economy/get-item-pricing/");
                    HashMap<String, Long> auction = new HashMap<>();
                    for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("auction").asMap().entrySet()) {
                        auction.put(entry.getKey(), entry.getValue().getAsLong());
                    }
                    auctionPricing = auction;
                    HashMap<String, BazaarPrice> bazaar = new HashMap<>();
                    for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("bazaar").asMap().entrySet()) {
                        JsonObject object = entry.getValue().getAsJsonObject();
                        bazaar.put(entry.getKey(), new BazaarPrice(object.get("buy").getAsDouble(), object.get("sell").getAsDouble()));
                    }
                    bazaarPricing = bazaar;
                    HashMap<String, NPCPrice> npc = new HashMap<>();
                    for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("npc").asMap().entrySet()) {
                        JsonObject object = entry.getValue().getAsJsonObject();
                        double coin = object.has("coin") ? object.get("coin").getAsDouble() : 0.0;
                        double mote = object.has("mote") ? object.get("mote").getAsDouble() : 0.0;
                        npc.put(entry.getKey(), new NPCPrice(coin, mote));
                    }
                    npcPricing = npc;
                } catch (Exception exception) {
                    LOGGER.error("Failed to refresh item pricing from NoFrills API.", exception);
                }
            })),
            new RefreshTask(2400, APIFlags.UseElectionAPI, task -> Thread.startVirtualThread(() -> {
                try {
                    JsonObject json = makeRequestObject("v1/election/get-active-perks");
                    HashSet<String> perks = new HashSet<>();
                    for (JsonElement element : json.getAsJsonArray("perks")) {
                        perks.add(element.getAsString());
                    }
                    electionPerks = perks;
                } catch (Exception exception) {
                    LOGGER.error("Failed to refresh election perks from NoFrills API.", exception);
                    task.currentTicks = 1200;
                }
            })),
            new RefreshTask(12000, APIFlags.UseNonPlaceableAPI, task -> Thread.startVirtualThread(() -> {
                try {
                    JsonArray json = makeRequestArray("v1/items/get-non-placeable/");
                    HashSet<String> items = new HashSet<>();
                    for (JsonElement element : json) {
                        items.add(element.getAsString());
                    }
                    nonPlaceableItems = items;
                } catch (Exception exception) {
                    LOGGER.error("Failed to refresh non-placeable items from NoFrills API.", exception);
                    task.currentTicks = 1200;
                }
            })),
            new RefreshTask(12000, APIFlags.UseMuseumAPI, task -> Thread.startVirtualThread(() -> {
                try {
                    JsonObject json = makeRequestObject("v1/items/get-museum-data/");
                    HashMap<String, MuseumData> data = new HashMap<>();
                    for (Map.Entry<String, JsonElement> entry : json.asMap().entrySet()) {
                        JsonObject object = entry.getValue().getAsJsonObject();
                        data.put(entry.getKey(), new MuseumData(
                                object.get("category").getAsString()
                        ));
                    }
                    museumData = data;
                } catch (Exception exception) {
                    LOGGER.error("Failed to refresh museum data from NoFrills API.", exception);
                    task.currentTicks = 1200;
                }
            })),
            new RefreshTask(72000, APIFlags.UseItemTexturesAPI, task -> Thread.startVirtualThread(() -> {
                try {
                    LegacyTextures.getTextures().set(NoFrillsAPI.makeRequestObject("v1/items/get-item-textures/"));
                } catch (Exception exception) {
                    LOGGER.error("Failed to refresh item textures from NoFrills API.", exception);
                    task.currentTicks = 1200;
                }
            })),
            new RefreshTask(72000, APIFlags.UseMiddleClickListsAPI, task -> Thread.startVirtualThread(() -> {
                try {
                    MiddleClickOverride.getLists().set(NoFrillsAPI.makeRequestObject("v1/data/middle-click-list"));
                } catch (Exception exception) {
                    LOGGER.error("Failed to refresh Middle Click Override lists from NoFrills API.", exception);
                    task.currentTicks = 1200;
                }
            }))
    );
    public static HashMap<String, Long> auctionPricing = new HashMap<>();
    public static HashMap<String, BazaarPrice> bazaarPricing = new HashMap<>();
    public static HashMap<String, NPCPrice> npcPricing = new HashMap<>();
    public static HashSet<String> electionPerks = new HashSet<>();
    public static HashSet<String> nonPlaceableItems = new HashSet<>();
    public static HashMap<String, MuseumData> museumData = new HashMap<>();

    public static String makeRequest(String endpoint) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://whatyouth.ing/api/nofrills/" + endpoint))
                .header("X-NoFrills-ModVer", Utils.getModContainer().getMetadata().getVersion().getFriendlyString())
                .header("X-NoFrills-GameVer", SharedConstants.getCurrentVersion().name())
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException(Utils.format("API request to {} failed with status code: {}", response.uri().toString(), response.statusCode()));
        }
        return response.body();
    }

    public static JsonObject makeRequestObject(String endpoint) throws Exception {
        String response = makeRequest(endpoint);
        return JsonParser.parseString(response).getAsJsonObject();
    }

    public static JsonArray makeRequestArray(String endpoint) throws Exception {
        String response = makeRequest(endpoint);
        return JsonParser.parseString(response).getAsJsonArray();
    }

    public static boolean usingFlag(APIFlags flag) {
        for (Feature feature : Feature.withFlags) {
            if (feature.hasFlag(flag) && feature.isActive()) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (Utils.isInSkyblock()) {
            for (RefreshTask task : tasks) {
                task.tick();
            }
        }
    }

    private static class RefreshTask {
        public final Consumer<RefreshTask> task;
        public final APIFlags requiredFlag;
        public final int ticks;
        public int currentTicks;

        public RefreshTask(int ticks, APIFlags requiredFlag, Consumer<RefreshTask> task) {
            this.task = task;
            this.requiredFlag = requiredFlag;
            this.ticks = ticks;
            this.currentTicks = 0;
        }

        public void tick() {
            if (this.currentTicks > 0) {
                this.currentTicks--;
            }
            if (this.currentTicks == 0 && mc.isWindowActive() && usingFlag(this.requiredFlag)) {
                this.task.accept(this);
                this.currentTicks = this.ticks;
            }
        }
    }

    public record BazaarPrice(double buy, double sell) {
        public static final BazaarPrice ZERO = new BazaarPrice(0.0, 0.0);
    }

    public record NPCPrice(double coin, double mote) {
    }

    public record MuseumData(String category) {
    }
}
