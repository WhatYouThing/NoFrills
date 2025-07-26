package nofrills.features.general;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingInt;
import nofrills.events.DrawItemTooltip;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.HashMap;
import java.util.Set;

import static nofrills.misc.NoFrillsAPI.*;

public class PriceTooltips {
    public static final Feature instance = new Feature("priceTooltips");

    public static final SettingBool auction = new SettingBool(false, "auction", instance.key());
    public static final SettingBool bazaar = new SettingBool(false, "bazaar", instance.key());
    public static final SettingBool npc = new SettingBool(false, "npc", instance.key());
    public static final SettingBool mote = new SettingBool(false, "mote", instance.key());
    public static final SettingInt burgers = new SettingInt(0, "burgers", instance.key());

    private static String parseItemId(ItemStack stack, NbtCompound data, String title) {
        String id = Utils.getSkyblockId(data);
        if (id.isEmpty()) {
            if (title.equals("Hunting Box")) {
                return correctShardId(getShardId(stack));
            }
            if (title.equals("Attribute Menu")) {
                for (String line : Utils.getLoreLines(stack)) {
                    if (line.startsWith("Source: ")) {
                        return correctShardId(line.substring(line.indexOf(":") + 2, line.indexOf("Shard") - 1).toUpperCase().replaceAll(" ", "_"));
                    }
                }
            }
        }
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
            case "ATTRIBUTE_SHARD" -> {
                return correctShardId(getShardId(stack));
            }
        }
        return id;
    }

    private static String getShardId(ItemStack stack) {
        return Formatting.strip(stack.getName().getString()).replaceAll(" ", "_").toUpperCase();
    }

    private static String correctShardId(String id) {
        return switch (id) {
            case "CINDERBAT" -> "SHARD_CINDER_BAT";
            case "ABYSSAL_LANTERNFISH" -> "SHARD_ABYSSAL_LANTERN";
            case "STRIDERSURFER" -> "SHARD_STRIDER_SURFER";
            default -> Utils.format("SHARD_{}", id);
        };
    }

    private static int getStackQuantity(ItemStack stack, String title) {
        if (title.endsWith("Sack")) {
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
        if (title.equals("Hunting Box")) {
            for (String line : Utils.getLoreLines(stack)) {
                if (line.startsWith("Owned: ")) {
                    int start = line.indexOf(":") + 2;
                    int end = line.indexOf(" ", start);
                    try {
                        int countInt = Integer.parseInt(line.substring(start, end).replaceAll(",", ""));
                        return countInt > 0 ? countInt : 1;
                    } catch (NumberFormatException ignored) {
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
    private static void onTooltip(DrawItemTooltip event) {
        if (instance.isActive()) {
            String itemId = parseItemId(event.stack, event.customData, event.title);
            int quantity = getStackQuantity(event.stack, event.title);
            if (itemId.isEmpty()) {
                return;
            }
            if (mote.value() && npcPricing.containsKey(itemId) && SkyblockData.getArea().equals("The Rift")) {
                HashMap<String, Double> prices = npcPricing.get(itemId);
                if (prices.containsKey("mote")) {
                    double burgerBonus = 1 + 0.05 * burgers.value();
                    event.addLine(buildLine("§dMotes Price", prices.get("mote") * burgerBonus, quantity, "§8({}x {})"));
                }
            }
            if (npc.value() && npcPricing.containsKey(itemId)) {
                HashMap<String, Double> prices = npcPricing.get(itemId);
                if (prices.containsKey("coin")) {
                    event.addLine(buildLine("§eNPC Price", prices.get("coin"), quantity, "§8({}x {})"));
                }
            }
            if (auction.value() && auctionPricing.containsKey(itemId)) {
                event.addLine(buildLine("§eLowest BIN", auctionPricing.get(itemId), quantity, "§8({}x {})"));
            }
            if (bazaar.value() && bazaarPricing.containsKey(itemId)) {
                HashMap<String, Double> prices = bazaarPricing.get(itemId);
                event.addLine(buildLine("§eBazaar Buy", prices.get("buy"), quantity, "§8({}x {})"));
                event.addLine(buildLine("§eBazaar Sell", prices.get("sell"), quantity, "§8({}x {})"));
            }
        }
    }
}
