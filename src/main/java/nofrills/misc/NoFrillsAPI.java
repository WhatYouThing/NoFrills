package nofrills.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import nofrills.config.Config;
import nofrills.events.DrawItemTooltip;
import nofrills.events.WorldTickEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static nofrills.Main.LOGGER;
import static nofrills.misc.Utils.info;

public class NoFrillsAPI {
    private static final String[] kuudraPieceTypes = new String[]{"HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"};
    private static final String[] kuudraPieceNames = new String[]{"CRIMSON", "TERROR", "AURORA", "HOLLOW", "FERVOR"};
    private static final String[] kuudraPieceTiers = new String[]{"HOT", "BURNING", "FIERY", "INFERNAL"};
    private static JsonObject auctionPrices = null;
    private static JsonObject bazaarPrices = null;
    private static JsonObject attributePrices = null;
    private static int pricingRefreshTicks = 0;

    private static String getKuudraPieceType(String itemId) {
        for (String type : kuudraPieceTypes) {
            for (String name : kuudraPieceNames) {
                if (itemId.endsWith(name + "_" + type)) {
                    return type;
                }
            }
        }
        return "";
    }

    /**
     * Returns a copy of the Auction House pricing data from memory, otherwise null if the data hasn't been pulled yet.
     */
    public static JsonObject getAuctionPrices() {
        return auctionPrices;
    }

    /**
     * Returns a copy of the Bazaar pricing data from memory, otherwise null if the data hasn't been pulled yet.
     */
    public static JsonObject getBazaarPrices() {
        return bazaarPrices;
    }

    /**
     * Returns a copy of the Attribute pricing data from memory, otherwise null if the data hasn't been pulled yet.
     */
    public static JsonObject getAttributePrices() {
        return attributePrices;
    }

    private static void refreshItemPricing() {
        new Thread(() -> {
            try {
                InputStream connection = URI.create("https://whatyouth.ing/api/nofrills/v1/economy/get-item-pricing/").toURL().openStream();
                InputStreamReader reader = new InputStreamReader(connection);
                JsonObject responseJson = JsonParser.parseReader(reader).getAsJsonObject();
                auctionPrices = JsonParser.parseString(responseJson.get("auction").getAsString()).getAsJsonObject();
                bazaarPrices = JsonParser.parseString(responseJson.get("bazaar").getAsString()).getAsJsonObject();
                attributePrices = JsonParser.parseString(responseJson.get("attribute").getAsString()).getAsJsonObject();
            } catch (IOException e) {
                info("§cAn error occurred while refreshing the item pricing. Additional information can be found in the log.");
                StringBuilder trace = new StringBuilder();
                for (StackTraceElement element : e.getStackTrace()) {
                    trace.append("\n\tat ").append(element.toString());
                }
                LOGGER.error("{}{}", e.getMessage(), trace);
            }
        }).start();
    }

    @EventHandler
    public static void onTick(WorldTickEvent event) {
        if (Config.priceTooltips) {
            if (pricingRefreshTicks == 0) {
                refreshItemPricing();
                pricingRefreshTicks = 1200;
            } else {
                pricingRefreshTicks--;
            }
        }
    }

    @EventHandler
    public static void onTooltip(DrawItemTooltip event) {
        String itemId = event.customData.getString("id");
        if (itemId == null || itemId.isEmpty()) {
            return;
        }
        if (itemId.equals("PET")) {
            JsonObject petData = JsonParser.parseString(event.customData.getString("petInfo")).getAsJsonObject();
            itemId = petData.get("type").getAsString() + "_PET_" + petData.get("tier").getAsString();
        }
        if (itemId.equals("RUNE")) {
            NbtCompound runeData = event.customData.getCompound("runes");
            String runeId = (String) runeData.getKeys().toArray()[0];
            int runeLevel = runeData.getInt(runeId);
            itemId = runeId + "_" + runeLevel + "_RUNE";
        }
        if (itemId.equals("ENCHANTED_BOOK")) {
            NbtCompound enchantData = event.customData.getCompound("enchantments");
            Set<String> enchants = enchantData.getKeys();
            if (enchants.size() == 1) {
                String enchantId = (String) enchantData.getKeys().toArray()[0];
                int enchantLevel = enchantData.getInt(enchantId);
                itemId = "ENCHANTMENT_" + enchantId.toUpperCase() + "_" + enchantLevel;
            }
        }
        JsonObject auctionPrices = NoFrillsAPI.getAuctionPrices();
        if (auctionPrices != null && auctionPrices.has(itemId)) {
            int quantity = event.stack.getCount();
            long lbin = auctionPrices.get(itemId).getAsLong();
            if (!itemId.equals("ATTRIBUTE_SHARD")) {
                String msg = "§c[NF] §eLowest BIN: §6" + String.format("%,d", lbin * quantity);
                if (quantity > 1) {
                    msg += " §8(" + quantity + "x " + String.format("%,d", lbin) + ")";
                }
                event.addLine(Text.of(msg));
            }
            JsonObject attributePrices = NoFrillsAPI.getAttributePrices();
            if (event.customData.contains("attributes") && attributePrices != null) {
                NbtCompound attributeData = event.customData.getCompound("attributes");
                Set<String> attributes = attributeData.getKeys();
                for (String attribute : attributes) {
                    int level = attributeData.getInt(attribute);
                    for (int i = level; i >= 1; i--) {
                        if (attributePrices.has(attribute + i)) {
                            JsonObject prices = attributePrices.getAsJsonObject(attribute + i);
                            List<Long> foundPrices = new ArrayList<>();
                            String pieceType = getKuudraPieceType(itemId);
                            if (!pieceType.isEmpty()) {
                                for (String name : kuudraPieceNames) {
                                    if (prices.has(name + "_" + pieceType)) {
                                        foundPrices.add(prices.get(name + "_" + pieceType).getAsLong());
                                    }
                                }
                            } else {
                                if (prices.has(itemId)) {
                                    foundPrices.add(prices.get(itemId).getAsLong());
                                }
                            }
                            foundPrices.sort(Comparator.comparingLong(price -> price));
                            String attributeLabel = Utils.uppercaseFirst(attribute.startsWith("mending") ? "vitality" : attribute, true);
                            String attributeMsg = "§c[NF] §ePrice for §b" + attributeLabel + " " + level + "§e: ";
                            if (!foundPrices.isEmpty()) {
                                if (i == level) {
                                    attributeMsg += "§6" + String.format("%,d", foundPrices.getFirst());
                                } else {
                                    int difference = (int) Math.pow(2, level - i);
                                    attributeMsg += "§6" + String.format("%,d", foundPrices.getFirst() * difference) + " §8(" + difference + "x Level " + i + ")";
                                }
                            } else {
                                if (i > 1) {
                                    continue;
                                } else {
                                    attributeMsg += "§cUnknown";
                                }
                            }
                            event.addLine(Text.of(attributeMsg));
                            break;
                        }
                    }
                }
                if (attributes.size() == 2) {
                    String first = (String) attributes.toArray()[0];
                    String second = (String) attributes.toArray()[1];
                    String rollMsg = "§c[NF] §ePrice for §aRoll§e: ";
                    for (String combo : new String[]{first + " " + second, second + " " + first}) {
                        if (attributePrices.has(combo)) {
                            JsonObject comboPrices = attributePrices.getAsJsonObject(combo);
                            String id = itemId;
                            for (String tier : kuudraPieceTiers) {
                                if (id.startsWith(tier)) {
                                    id = id.replace(tier + "_", "");
                                    break;
                                }
                            }
                            if (comboPrices.has(id)) {
                                rollMsg += "§6" + String.format("%,d", comboPrices.get(id).getAsLong());
                            } else {
                                rollMsg += "§cUnknown";
                            }
                            break;
                        }
                    }
                    event.addLine(Text.of(rollMsg));
                }
            }
        }
        JsonObject bazaarPrices = NoFrillsAPI.getBazaarPrices();
        if (auctionPrices != null && bazaarPrices.has(itemId)) {
            JsonArray bzPrices = bazaarPrices.get(itemId).getAsJsonArray();
            long buyPrice = bzPrices.get(0).getAsLong();
            long sellPrice = bzPrices.get(1).getAsLong();
            int quantity = event.stack.getCount();
            String buyMsg = "§c[NF] §eBZ Insta-buy: §6" + String.format("%,d", buyPrice * quantity);
            String sellMsg = "§c[NF] §eBZ Insta-sell: §6" + String.format("%,d", sellPrice * quantity);
            if (quantity > 1) {
                buyMsg += " §8(" + quantity + "x " + String.format("%,d", buyPrice) + ")";
                sellMsg += " §8(" + quantity + "x " + String.format("%,d", sellPrice) + ")";
            }
            event.addLine(Text.of(buyMsg));
            event.addLine(Text.of(sellMsg));
        }
    }
}
