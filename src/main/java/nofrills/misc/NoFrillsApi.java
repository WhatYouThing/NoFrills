package nofrills.misc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import static nofrills.Main.LOGGER;
import static nofrills.misc.Utils.info;

public class NoFrillsApi {
    private static JsonObject itemPricing = null;

    /**
     * Returns a copy of the item pricing data from memory, otherwise null if the data hasn't been pulled yet.
     */
    public static JsonObject getItemPricing() {
        return itemPricing;
    }

    /**
     * Asynchronously pull the most recent pricing data from the API.
     */
    public static void refreshItemPricing() {
        new Thread(() -> {
            try {
                InputStream connection = URI.create("https://whatyouth.ing/api/nofrills/v1/economy/get-item-pricing/").toURL().openStream();
                InputStreamReader reader = new InputStreamReader(connection);
                itemPricing = JsonParser.parseReader(reader).getAsJsonObject();
            } catch (IOException e) {
                info("§cAn error occurred while fetching the item pricing. Additional information can be found in the log.");
                StringBuilder trace = new StringBuilder();
                for (StackTraceElement element : e.getStackTrace()) {
                    trace.append("\n\tat ").append(element.toString());
                }
                LOGGER.error("{}{}", e.getMessage(), trace);
            }
        }).start();
    }
}
