package nofrills.misc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import meteordevelopment.orbit.EventHandler;
import nofrills.config.Config;
import nofrills.events.WorldTickEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static nofrills.Main.LOGGER;
import static nofrills.Main.mc;

public class NoFrillsAPI {
    public static final ConcurrentHashMap<String, Long> auctionPricing = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, HashMap<String, Double>> bazaarPricing = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, HashMap<String, Long>> attributePricing = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, HashMap<String, Double>> npcPricing = new ConcurrentHashMap<>();
    private static int refreshTicks = 0;

    private static void refreshItemPricing() {
        new Thread(() -> {
            try {
                InputStream connection = URI.create("https://whatyouth.ing/api/nofrills/v2/economy/get-item-pricing/").toURL().openStream();
                JsonObject json = JsonParser.parseReader(new InputStreamReader(connection)).getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("auction").asMap().entrySet()) {
                    auctionPricing.put(entry.getKey(), entry.getValue().getAsLong());
                }
                for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("bazaar").asMap().entrySet()) {
                    JsonObject object = entry.getValue().getAsJsonObject();
                    HashMap<String, Double> pricing = new HashMap<>();
                    pricing.put("buy", object.get("buy").getAsDouble());
                    pricing.put("sell", object.get("sell").getAsDouble());
                    bazaarPricing.put(entry.getKey(), pricing);
                }
                if (json.has("attribute")) {
                    for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("attribute").asMap().entrySet()) {
                        JsonObject object = entry.getValue().getAsJsonObject();
                        HashMap<String, Long> pricing = new HashMap<>();
                        for (Map.Entry<String, JsonElement> entryPrices : object.asMap().entrySet()) {
                            pricing.put(entryPrices.getKey(), entryPrices.getValue().getAsLong());
                        }
                        attributePricing.put(entry.getKey(), pricing);
                    }
                }
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
                        npcPricing.put(entry.getKey(), pricing);
                    }
                }
            } catch (IOException exception) {
                StringBuilder trace = new StringBuilder();
                for (StackTraceElement element : exception.getStackTrace()) {
                    trace.append("\n\tat ").append(element.toString());
                }
                LOGGER.error("{}{}", exception.getMessage(), trace);
            }
        }).start();
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (Config.fetchPricing && Utils.isInSkyblock()) {
            if (refreshTicks == 0) {
                if (mc.isWindowFocused()) { // prevent refreshing while afk to not send unnecessary requests
                    refreshItemPricing();
                    refreshTicks = 1200;
                }
            } else {
                refreshTicks--;
            }
        }
    }
}
