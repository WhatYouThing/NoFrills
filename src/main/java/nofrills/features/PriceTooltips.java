package nofrills.features;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import nofrills.events.DrawItemTooltip;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.*;

import static nofrills.Main.Config;
import static nofrills.Main.mc;
import static nofrills.misc.NoFrillsAPI.*;

public class PriceTooltips {
    private static final List<String> kuudraPieceTypes = List.of("HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS");
    private static final List<String> kuudraPieceNames = List.of("CRIMSON", "TERROR", "AURORA", "HOLLOW", "FERVOR");
    private static final List<String> kuudraPieceTiers = List.of("HOT", "BURNING", "FIERY", "INFERNAL");

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
        String id = Utils.getSkyblockId(data);
        switch (id) {
            case "PET" -> {
                if (data.contains("petInfo")) {
                    JsonObject petData = JsonParser.parseString(data.getString("petInfo").orElse("")).getAsJsonObject();
                    return Utils.format("{}_PET_{}", petData.get("type").getAsString(), petData.get("tier").getAsString());
                } else {
                    return "UNKNOWN_PET";
                }
            }
            case "RUNE" -> {
                if (data.contains("runes")) {
                    NbtCompound runeData = data.getCompound("runes").orElse(null);
                    String runeId = (String) runeData.getKeys().toArray()[0];
                    return Utils.format("{}_{}_RUNE", runeId, runeData.getInt(runeId));
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
                        return Utils.format("ENCHANTMENT_{}_{}", enchantId.toUpperCase(), enchantLevel);
                    }
                } else {
                    return "ENCHANTMENT_UNKNOWN";
                }
            }
        }
        return id;
    }

    private static int getStackQuantity(ItemStack stack) {
        if (mc.currentScreen instanceof GenericContainerScreen container) {
            if (container.getTitle().getString().endsWith("Sack")) {
                for (String line : Utils.getLoreLines(stack)) {
                    if (line.startsWith("Stored: ") && line.contains("/")) {
                        String count = line.substring(line.indexOf(":") + 1, line.indexOf("/")).trim();
                        try {
                            int countInt = Integer.parseInt(count.replaceAll(",", ""));
                            return countInt > 0 ? countInt : 1;
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
        }
        return stack.getCount();
    }

    private static Text buildLine(String name, double price, int quantity, String extra) {
        String line = Utils.format(
                "§c[NF] {}: §6{} {}",
                name,
                String.format("%,.1f", price * quantity),
                quantity > 1 ? Utils.format(extra, String.format("%,d", quantity), String.format("%,.1f", price)) : ""
        ).trim();
        return Text.of(line);
    }

    private static Text buildLine(String name, long price, int quantity, String extra) {
        String line = Utils.format(
                "§c[NF] {}: §6{} {}",
                name,
                String.format("%,d", price * quantity),
                quantity > 1 ? Utils.format(extra, String.format("%,d", quantity), String.format("%,d", price)) : ""
        ).trim();
        return Text.of(line);
    }

    @EventHandler
    public static void onTooltip(DrawItemTooltip event) {
        if (Config.fetchPricing() && event.customData != null) {
            NbtCompound customData = event.customData.copyNbt();
            String itemId = parseItemId(customData);
            int quantity = getStackQuantity(event.stack);
            if (itemId.isEmpty()) {
                return;
            }
            if (Config.pricingMote() && npcPricing.containsKey(itemId) && SkyblockData.getArea().equals("The Rift")) {
                HashMap<String, Double> prices = npcPricing.get(itemId);
                if (prices.containsKey("mote")) {
                    double burgerBonus = 1 + 0.05 * Config.pricingMoteStacks();
                    event.addLine(buildLine("§dMotes Price", prices.get("mote") * burgerBonus, quantity, "§8({}x {})"));
                }
            }
            if (Config.pricingNPC() && npcPricing.containsKey(itemId)) {
                HashMap<String, Double> prices = npcPricing.get(itemId);
                if (prices.containsKey("coin")) {
                    event.addLine(buildLine("§eNPC Price", prices.get("coin"), quantity, "§8({}x {})"));
                }
            }
            if (Config.pricingAuction() && auctionPricing.containsKey(itemId)) {
                long lbin = auctionPricing.get(itemId);
                if (!itemId.equals("ATTRIBUTE_SHARD")) {
                    event.addLine(buildLine("§eLowest BIN", lbin, quantity, "§8({}x {})"));
                }
            }
            if (Config.pricingAttribute() && !attributePricing.isEmpty() && event.customData.contains("attributes")) {
                NbtCompound attributeData = customData.getCompound("attributes").orElse(null);
                Set<String> attributes = attributeData.getKeys();
                for (String attribute : attributes) {
                    int level = attributeData.getInt(attribute).orElse(0);
                    for (int i = level; i >= 1; i--) {
                        if (attributePricing.containsKey(attribute + i)) {
                            HashMap<String, Long> prices = attributePricing.get(attribute + i);
                            List<Long> foundPrices = new ArrayList<>();
                            String pieceType = getKuudraPieceType(itemId);
                            if (!pieceType.isEmpty()) {
                                for (String name : kuudraPieceNames) {
                                    if (prices.containsKey(name + "_" + pieceType)) {
                                        foundPrices.add(prices.get(name + "_" + pieceType));
                                    }
                                }
                            } else {
                                if (prices.containsKey(itemId)) {
                                    foundPrices.add(prices.get(itemId));
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
                            } else { // not rewriting this spaghetti since its getting deleted sooner or later
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
                        if (attributePricing.containsKey(combo)) {
                            HashMap<String, Long> comboPrices = attributePricing.get(combo);
                            String id = itemId;
                            for (String tier : kuudraPieceTiers) {
                                if (id.startsWith(tier)) {
                                    id = id.replace(tier + "_", "");
                                    break;
                                }
                            }
                            if (comboPrices.containsKey(id)) {
                                rollMsg += "§6" + String.format("%,d", comboPrices.get(id));
                            } else {
                                rollMsg += "§cUnknown";
                            }
                            break;
                        }
                    }
                    event.addLine(Text.of(rollMsg));
                }
            }
            if (Config.pricingBazaar() && bazaarPricing.containsKey(itemId)) {
                HashMap<String, Double> prices = bazaarPricing.get(itemId);
                event.addLine(buildLine("§eBazaar Buy", prices.get("buy"), quantity, "§8({}x {})"));
                event.addLine(buildLine("§eBazaar Sell", prices.get("sell"), quantity, "§8({}x {})"));
            }
        }
    }
}
