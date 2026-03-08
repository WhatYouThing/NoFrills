package nofrills.misc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.events.WorldTickEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.*;

import static nofrills.Main.LOGGER;
import static nofrills.Main.mc;

public class NoFrillsAPI {
    public static final List<Feature> pricingFeatures = new ArrayList<>();
    public static final List<Feature> perksFeatures = new ArrayList<>();
    public static HashMap<String, Long> auctionPricing = new HashMap<>();
    public static HashMap<String, HashMap<String, Double>> bazaarPricing = new HashMap<>();
    public static HashMap<String, HashMap<String, Double>> npcPricing = new HashMap<>();
    public static HashSet<String> electionPerks = new HashSet<>();
    private static int refreshTicks = 0;

    private static boolean shouldRefreshPricing() {
        for (Feature feature : pricingFeatures) {
            if (feature.isActive()) {
                return true;
            }
        }
        return false;
    }

    private static boolean shouldRefreshPerks() {
        for (Feature feature : perksFeatures) {
            if (feature.isActive()) {
                return true;
            }
        }
        return false;
    }

    private static void refreshItemPricing() {
        Thread.startVirtualThread(() -> {
            try {
                InputStream connection = URI.create("https://whatyouth.ing/api/nofrills/v2/economy/get-item-pricing/").toURL().openStream();
                JsonObject json = JsonParser.parseReader(new InputStreamReader(connection)).getAsJsonObject();
                HashMap<String, Long> auction = new HashMap<>();
                for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("auction").asMap().entrySet()) {
                    auction.put(entry.getKey(), entry.getValue().getAsLong());
                }
                auctionPricing = auction;
                HashMap<String, HashMap<String, Double>> bazaar = new HashMap<>();
                for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("bazaar").asMap().entrySet()) {
                    JsonObject object = entry.getValue().getAsJsonObject();
                    HashMap<String, Double> pricing = new HashMap<>();
                    pricing.put("buy", object.get("buy").getAsDouble());
                    pricing.put("sell", object.get("sell").getAsDouble());
                    bazaar.put(entry.getKey(), pricing);
                }
                bazaarPricing = bazaar;
                HashMap<String, HashMap<String, Double>> npc = new HashMap<>();
                for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("npc").asMap().entrySet()) {
                    JsonObject object = entry.getValue().getAsJsonObject();
                    HashMap<String, Double> pricing = new HashMap<>();
                    if (object.has("coin")) {
                        pricing.put("coin", object.get("coin").getAsDouble());
                    }
                    if (object.has("mote")) {
                        pricing.put("mote", object.get("mote").getAsDouble());
                    }
                    if (!pricing.isEmpty()) {
                        npc.put(entry.getKey(), pricing);
                    }
                }
                npcPricing = npc;
            } catch (Exception exception) {
                LOGGER.error("Failed to refresh item pricing from NoFrills API.", exception);
            }
        });
    }

    private static void refreshElectionPerks() {
        Thread.startVirtualThread(() -> {
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
        });
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (Utils.isInSkyblock()) {
            if (refreshTicks == 0) {
                if (mc.isWindowFocused()) { // prevent refreshing while afk to not send unnecessary requests
                    if (shouldRefreshPricing()) {
                        refreshItemPricing();
                    }
                    if (shouldRefreshPerks()) {
                        refreshElectionPerks();
                    }
                    refreshTicks = 1200;
                }
            } else {
                refreshTicks--;
            }
        }
    }
}
