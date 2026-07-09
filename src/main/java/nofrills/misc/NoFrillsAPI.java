package nofrills.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.events.EventListener;
import nofrills.events.WorldTickEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static nofrills.Main.LOGGER;
import static nofrills.Main.mc;

@EventListener
public class NoFrillsAPI {
    public static HashMap<String, Long> auctionPricing = new HashMap<>();
    public static HashMap<String, BazaarPrice> bazaarPricing = new HashMap<>();
    public static HashMap<String, NPCPrice> npcPricing = new HashMap<>();
    public static HashSet<String> electionPerks = new HashSet<>();
    public static HashSet<String> nonPlaceableItems = new HashSet<>();
    public static HashMap<String, MuseumData> museumData = new HashMap<>();
    public static HashMap<String, ItemTexture> itemTextures = new HashMap<>();
    private static final List<RefreshTask> tasks = List.of(
            new RefreshTask(1200, Feature.Flags.UsePricingAPI, () -> Thread.startVirtualThread(() -> {
                try {
                    InputStream connection = URI.create("https://whatyouth.ing/api/nofrills/v2/economy/get-item-pricing/").toURL().openStream();
                    JsonObject json = JsonParser.parseReader(new InputStreamReader(connection)).getAsJsonObject();
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
            new RefreshTask(2400, Feature.Flags.UseElectionAPI, () -> Thread.startVirtualThread(() -> {
                try {
                    InputStream connection = URI.create("https://whatyouth.ing/api/nofrills/v1/election/get-active-perks").toURL().openStream();
                    JsonObject json = JsonParser.parseReader(new InputStreamReader(connection)).getAsJsonObject();
                    HashSet<String> perks = new HashSet<>();
                    for (JsonElement element : json.getAsJsonArray("perks")) {
                        perks.add(element.getAsString());
                    }
                    electionPerks = perks;
                } catch (IOException exception) {
                    LOGGER.error("Failed to refresh election perks from NoFrills API.", exception);
                }
            })),
            new RefreshTask(12000, Feature.Flags.UseNonPlaceableAPI, () -> Thread.startVirtualThread(() -> {
                try {
                    InputStream connection = URI.create("https://whatyouth.ing/api/nofrills/v1/items/get-non-placeable/").toURL().openStream();
                    JsonArray json = JsonParser.parseReader(new InputStreamReader(connection)).getAsJsonArray();
                    HashSet<String> items = new HashSet<>();
                    for (JsonElement element : json) {
                        items.add(element.getAsString());
                    }
                    nonPlaceableItems = items;
                } catch (Exception exception) {
                    LOGGER.error("Failed to refresh non-placeable items from NoFrills API.", exception);
                }
            })),
            new RefreshTask(12000, Feature.Flags.UseMuseumAPI, () -> Thread.startVirtualThread(() -> {
                try {
                    InputStream connection = URI.create("https://whatyouth.ing/api/nofrills/v1/items/get-museum-data/").toURL().openStream();
                    JsonObject json = JsonParser.parseReader(new InputStreamReader(connection)).getAsJsonObject();
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
                }
            })),
            new RefreshTask(12000, Feature.Flags.UseItemTexturesAPI, () -> Thread.startVirtualThread(() -> {
                try {
                    InputStream connection = URI.create("https://whatyouth.ing/api/nofrills/v1/items/get-item-textures/").toURL().openStream();
                    JsonObject json = JsonParser.parseReader(new InputStreamReader(connection)).getAsJsonObject();
                    HashMap<String, ItemTexture> textures = new HashMap<>();
                    for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                        JsonObject object = entry.getValue().getAsJsonObject();
                        textures.put(entry.getKey(), new ItemTexture(
                                object.get("model").getAsString(),
                                object.has("textures") ? object.get("textures").getAsString() : ""
                        ));
                    }
                    itemTextures = textures;
                } catch (Exception exception) {
                    LOGGER.error("Failed to refresh item texture data from NoFrills API.", exception);
                }
            }))
    );

    public static boolean usingFlag(Feature.Flags flag) {
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

    public record ItemTexture(String model, String textures) {
    }

    private static class RefreshTask {
        public final Runnable task;
        public final Feature.Flags requiredFlag;
        public final int ticks;
        public int currentTicks;

        public RefreshTask(int ticks, Feature.Flags requiredFlag, Runnable task) {
            this.task = task;
            this.requiredFlag = requiredFlag;
            this.ticks = ticks;
            this.currentTicks = 0;
        }

        public void tick() {
            if (this.currentTicks > 0) {
                this.currentTicks--;
            }
            if (this.currentTicks == 0 && mc.isWindowActive() && NoFrillsAPI.usingFlag(this.requiredFlag)) {
                this.task.run();
                this.currentTicks = this.ticks;
            }
        }
    }

    public record BazaarPrice(double buy, double sell) {
    }

    public record NPCPrice(double coin, double mote) {
    }

    public record MuseumData(String category) {
    }
}
