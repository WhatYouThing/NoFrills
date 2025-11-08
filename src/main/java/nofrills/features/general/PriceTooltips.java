package nofrills.features.general;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingInt;
import nofrills.events.TooltipRenderEvent;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.HashMap;
import java.util.Optional;

import static nofrills.misc.NoFrillsAPI.*;

public class PriceTooltips {
    public static final Feature instance = new Feature("priceTooltips");

    public static final SettingBool auction = new SettingBool(false, "auction", instance.key());
    public static final SettingBool bazaar = new SettingBool(false, "bazaar", instance.key());
    public static final SettingBool npc = new SettingBool(false, "npc", instance.key());
    public static final SettingBool mote = new SettingBool(false, "mote", instance.key());
    public static final SettingInt burgers = new SettingInt(0, "burgers", instance.key());

    public static String parseItemId(ItemStack stack) {
        return Utils.getMarketId(stack);
    }

    public static int getStackQuantity(ItemStack stack, String title) {
        if (title.endsWith("Sack")) {
            for (String line : Utils.getLoreLines(stack)) {
                if (line.startsWith("Stored: ") && line.contains("/")) {
                    String count = line.substring(line.indexOf(":") + 1, line.indexOf("/")).trim();
                    Optional<Integer> countInt = Utils.parseInt(count.replaceAll(",", ""));
                    if (countInt.isPresent()) {
                        return Math.max(1, countInt.get());
                    }
                }
            }
        }
        if (title.equals("Hunting Box")) {
            for (String line : Utils.getLoreLines(stack)) {
                if (line.startsWith("Owned: ")) {
                    int start = line.indexOf(":") + 2;
                    int end = line.indexOf(" ", start);
                    Optional<Integer> countInt = Utils.parseInt(line.substring(start, end).replaceAll(",", ""));
                    if (countInt.isPresent()) {
                        return Math.max(1, countInt.get());
                    }
                }
            }
        }
        if (title.equals("Attribute Menu")) {
            for (String line : Utils.getLoreLines(stack)) {
                if (line.startsWith("Syphon") && line.endsWith("more to level up!")) {
                    String replaced = line.replace("Syphon", "").trim();
                    Optional<Integer> countInt = Utils.parseInt(replaced.substring(0, replaced.indexOf(" ")).replaceAll(",", ""));
                    if (countInt.isPresent()) {
                        return Math.max(1, countInt.get());
                    }
                }
            }
        }
        return stack.getCount();
    }

    private static Text buildLine(String name, double price, int quantity) {
        String line = Utils.format(
                "{}: §6{} {}",
                name,
                Utils.formatSeparator(price * quantity),
                quantity > 1 ? Utils.format("§8({}x {})", Utils.formatSeparator(quantity), Utils.formatSeparator(price)) : ""
        ).trim();
        return Utils.getShortTag().append(Text.literal(line).withColor(0xffffff));
    }

    private static Text buildLine(String name, long price, int quantity) {
        String line = Utils.format(
                "{}: §6{} {}",
                name,
                Utils.formatSeparator(price * quantity),
                quantity > 1 ? Utils.format("§8({}x {})", Utils.formatSeparator(quantity), Utils.formatSeparator(price)) : ""
        ).trim();
        return Utils.getShortTag().append(Text.literal(line).withColor(0xffffff));
    }

    @EventHandler
    private static void onTooltip(TooltipRenderEvent event) {
        if (instance.isActive()) {
            String itemId = parseItemId(event.stack);
            if (itemId.isEmpty()) return;
            int quantity = getStackQuantity(event.stack, event.title);
            if (mote.value() && npcPricing.containsKey(itemId) && SkyblockData.getArea().equals("The Rift")) {
                HashMap<String, Double> prices = npcPricing.get(itemId);
                if (prices.containsKey("mote")) {
                    double burgerBonus = 1 + 0.05 * burgers.value();
                    event.addLine(buildLine("§dMotes Price", prices.get("mote") * burgerBonus, quantity));
                }
            }
            if (npc.value() && npcPricing.containsKey(itemId)) {
                HashMap<String, Double> prices = npcPricing.get(itemId);
                if (prices.containsKey("coin")) {
                    event.addLine(buildLine("§eNPC Price", prices.get("coin"), quantity));
                }
            }
            if (auction.value() && auctionPricing.containsKey(itemId)) {
                event.addLine(buildLine("§eLowest BIN", auctionPricing.get(itemId), quantity));
            }
            if (bazaar.value() && bazaarPricing.containsKey(itemId)) {
                HashMap<String, Double> prices = bazaarPricing.get(itemId);
                event.addLine(buildLine("§eBazaar Buy", prices.get("buy"), quantity));
                event.addLine(buildLine("§eBazaar Sell", prices.get("sell"), quantity));
            }
        }
    }
}
