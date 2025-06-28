package nofrills.features;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import nofrills.config.Config;
import nofrills.events.DrawItemTooltip;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.HashMap;
import java.util.Set;

import static nofrills.Main.mc;
import static nofrills.misc.NoFrillsAPI.*;

public class PriceTooltips {
    private static String parseItemId(ItemStack stack) {
        NbtCompound data = stack.get(DataComponentTypes.CUSTOM_DATA).getNbt();
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
            case "ATTRIBUTE_SHARD" -> {
                String shardId = Formatting.strip(stack.getName().getString()).replaceAll(" ", "_").toUpperCase();
                if (shardId.equals("CINDERBAT")) {
                    shardId = "CINDER_BAT";
                }
                if (shardId.equals("ABYSSAL_LANTERNFISH")) {
                    shardId = "ABYSSAL_LANTERN";
                }
                if (shardId.equals("STRIDERSURFER")) {
                    shardId = "STRIDER_SURFER";
                }
                return Utils.format("SHARD_{}", shardId);
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
        if (Config.fetchPricing && event.customData != null) {
            String itemId = parseItemId(event.stack);
            int quantity = getStackQuantity(event.stack);
            if (itemId.isEmpty()) {
                return;
            }
            if (Config.pricingMote && npcPricing.containsKey(itemId) && SkyblockData.getArea().equals("The Rift")) {
                HashMap<String, Double> prices = npcPricing.get(itemId);
                if (prices.containsKey("mote")) {
                    double burgerBonus = 1 + 0.05 * Config.pricingMoteStacks;
                    event.addLine(buildLine("§dMotes Price", prices.get("mote") * burgerBonus, quantity, "§8({}x {})"));
                }
            }
            if (Config.pricingNPC && npcPricing.containsKey(itemId)) {
                HashMap<String, Double> prices = npcPricing.get(itemId);
                if (prices.containsKey("coin")) {
                    event.addLine(buildLine("§eNPC Price", prices.get("coin"), quantity, "§8({}x {})"));
                }
            }
            if (Config.pricingAuction && auctionPricing.containsKey(itemId)) {
                event.addLine(buildLine("§eLowest BIN", auctionPricing.get(itemId), quantity, "§8({}x {})"));
            }
            if (Config.pricingBazaar && bazaarPricing.containsKey(itemId)) {
                HashMap<String, Double> prices = bazaarPricing.get(itemId);
                event.addLine(buildLine("§eBazaar Buy", prices.get("buy"), quantity, "§8({}x {})"));
                event.addLine(buildLine("§eBazaar Sell", prices.get("sell"), quantity, "§8({}x {})"));
            }
        }
    }
}
