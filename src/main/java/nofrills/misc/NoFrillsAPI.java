package nofrills.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
import static nofrills.Main.mc;

public class NoFrillsAPI {
    private static final String[] kuudraPieceTypes = new String[]{"HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"};
    private static final String[] kuudraPieceNames = new String[]{"CRIMSON", "TERROR", "AURORA", "HOLLOW", "FERVOR"};
    private static final String[] kuudraPieceTiers = new String[]{"HOT", "BURNING", "FIERY", "INFERNAL"};
    private static JsonObject pendingPricing = null;
    private static JsonObject auctionPrices = null;
    private static JsonObject bazaarPrices = null;
    private static JsonObject attributePrices = null;
    private static JsonObject npcPrices = null;
    private static int refreshTicks = 0;

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

    private static String parseItemId(NbtCompound data) {
        String itemId = data.getString("id").orElse("");
        if (itemId.isEmpty()) {
            return "";
        }
        switch (itemId) {
            case "PET" -> {
                if (data.contains("petInfo")) {
                    JsonObject petData = JsonParser.parseString(data.getString("petInfo").orElse("")).getAsJsonObject();
                    return petData.get("type").getAsString() + "_PET_" + petData.get("tier").getAsString();
                } else {
                    return "UNKNOWN_PET";
                }
            }
            case "RUNE" -> {
                if (data.contains("runes")) {
                    NbtCompound runeData = data.getCompound("runes").orElse(null);
                    String runeId = (String) runeData.getKeys().toArray()[0];
                    return runeId + "_" + runeData.getInt(runeId) + "_RUNE";
                } else {
                    return "EMPTY_RUNE";
                }
            }
            case "ENCHANTED_BOOK" -> {
                if (data.contains("enchantments")) {
                    NbtCompound enchantData = data.getCompound("enchantments").orElse(null);
                    Set<String> enchants = enchantData.getKeys();
                    if (enchants.size() == 1) {
                        String enchantId = (String) enchantData.getKeys().toArray()[0];
                        int enchantLevel = enchantData.getInt(enchantId).orElse(0);
                        itemId = "ENCHANTMENT_" + enchantId.toUpperCase() + "_" + enchantLevel;
                    }
                } else {
                    return "ENCHANTMENT_UNKNOWN";
                }
            }
        }
        return itemId;
    }

    private static int getStackQuantity(ItemStack stack) {
        if (mc.currentScreen instanceof GenericContainerScreen container) {
            if (container.getTitle().getString().endsWith("Sack")) {
                LoreComponent lore = stack.getComponents().get(DataComponentTypes.LORE);
                if (lore != null) {
                    for (Text line : lore.lines()) {
                        String clear = Formatting.strip(line.getString());
                        if (clear.startsWith("Stored: ") && clear.contains("/")) {
                            String count = clear.substring(clear.indexOf(":") + 1, clear.indexOf("/")).trim();
                            try {
                                int countInt = Integer.parseInt(count.replaceAll(",", ""));
                                return countInt > 0 ? countInt : 1;
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                }
            }
        }
        return stack.getCount();
    }

    private static JsonObject sendRequest(String url) {
        try {
            InputStream connection = URI.create(url).toURL().openStream();
            InputStreamReader reader = new InputStreamReader(connection);
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException exception) {
            StringBuilder trace = new StringBuilder();
            for (StackTraceElement element : exception.getStackTrace()) {
                trace.append("\n\tat ").append(element.toString());
            }
            LOGGER.error("{}{}", exception.getMessage(), trace);
            return null;
        }
    }

    private static void refreshItemPricing() {
        new Thread(() -> {
            JsonObject response = sendRequest("https://whatyouth.ing/api/nofrills/v1/economy/get-item-pricing/");
            if (response != null) {
                pendingPricing = response;
            }
        }).start();
    }

    @EventHandler
    public static void onTick(WorldTickEvent event) {
        if (Config.priceTooltips && Utils.isInSkyblock()) {
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

    @EventHandler
    public static void onTooltip(DrawItemTooltip event) {
        if (Config.priceTooltips && event.customData != null) {
            String itemId = parseItemId(event.customData);
            if (itemId.isEmpty()) {
                return;
            }
            if (npcPrices != null && npcPrices.has(itemId)) {
                JsonObject prices = npcPrices.get(itemId).getAsJsonObject();
                int quantity = getStackQuantity(event.stack);
                if (SkyblockData.getArea().equals("The Rift") && prices.has("mote")) {
                    double motePrice = prices.get("mote").getAsDouble();
                    String motesMsg = "§c[NF] §dMotes Price: §6" + String.format("%,.1f", motePrice * quantity);
                    if (quantity > 1) {
                        motesMsg += " §8(" + quantity + "x " + String.format("%,.1f", motePrice) + ")";
                    }
                    event.addLine(Text.of(motesMsg));
                } else if (prices.has("coin")) {
                    double coinPrice = prices.get("coin").getAsDouble();
                    String coinsMsg = "§c[NF] §eNPC Price: §6" + String.format("%,.1f", coinPrice * quantity);
                    if (quantity > 1) {
                        coinsMsg += " §8(" + quantity + "x " + String.format("%,.1f", coinPrice) + ")";
                    }
                    event.addLine(Text.of(coinsMsg));
                }
            }
            if (auctionPrices != null && auctionPrices.has(itemId)) {
                int quantity = getStackQuantity(event.stack);
                long lbin = auctionPrices.get(itemId).getAsLong();
                if (!itemId.equals("ATTRIBUTE_SHARD")) {
                    String msg = "§c[NF] §eLowest BIN: §6" + String.format("%,d", lbin * quantity);
                    if (quantity > 1) {
                        msg += " §8(" + quantity + "x " + String.format("%,d", lbin) + ")";
                    }
                    event.addLine(Text.of(msg));
                }
                if (event.customData.contains("attributes") && attributePrices != null) {
                    NbtCompound attributeData = event.customData.getCompound("attributes").orElse(null);
                    Set<String> attributes = attributeData.getKeys();
                    for (String attribute : attributes) {
                        int level = attributeData.getInt(attribute).orElse(0);
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
            if (bazaarPrices != null && bazaarPrices.has(itemId)) {
                JsonArray bzPrices = bazaarPrices.get(itemId).getAsJsonArray();
                double buyPrice = bzPrices.get(0).getAsDouble();
                double sellPrice = bzPrices.get(1).getAsDouble();
                int quantity = getStackQuantity(event.stack);
                String buyMsg = "§c[NF] §eBazaar Buy: §6" + String.format("%,.1f", buyPrice * quantity);
                String sellMsg = "§c[NF] §eBazaar Sell: §6" + String.format("%,.1f", sellPrice * quantity);
                if (quantity > 1) {
                    buyMsg += " §8(" + quantity + "x " + String.format("%,.1f", buyPrice) + ")";
                    sellMsg += " §8(" + quantity + "x " + String.format("%,.1f", sellPrice) + ")";
                }
                event.addLine(Text.of(buyMsg));
                event.addLine(Text.of(sellMsg));
            }
            if (pendingPricing != null) {
                auctionPrices = JsonParser.parseString(pendingPricing.get("auction").getAsString()).getAsJsonObject();
                bazaarPrices = JsonParser.parseString(pendingPricing.get("bazaar").getAsString()).getAsJsonObject();
                attributePrices = JsonParser.parseString(pendingPricing.get("attribute").getAsString()).getAsJsonObject();
                npcPrices = JsonParser.parseString(pendingPricing.get("npc").getAsString()).getAsJsonObject();
                pendingPricing = null;
            }
        }
    }
}
