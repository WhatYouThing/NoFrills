package nofrills.features.general;

import com.google.gson.JsonObject;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingInt;
import nofrills.config.SettingJson;
import nofrills.events.SlotClickEvent;
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
    public static final SettingBool pricePaid = new SettingBool(false, "pricePaid", instance);
    public static final SettingJson paidData = new SettingJson(new JsonObject(), "paidData", instance);

    public static int getStackQuantity(ItemStack stack) {
        for (String line : Utils.getLoreLines(stack)) {
            if (line.startsWith("Stored: ") && line.contains("/")) {
                String count = line.substring(line.indexOf(":") + 1, line.indexOf("/")).trim();
                Optional<Integer> countInt = Utils.parseInt(count.replaceAll(",", ""));
                if (countInt.isPresent()) {
                    return Math.max(1, countInt.get());
                }
            }
            if (line.startsWith("Compost Available: ")) {
                Optional<Integer> countInt = Utils.parseInt(line.substring(line.indexOf(":") + 2).replaceAll(",", ""));
                if (countInt.isPresent()) {
                    return countInt.get();
                }
            }
            if (line.startsWith("Syphon") && line.endsWith("more to level up!")) {
                String replaced = line.replace("Syphon", "").trim();
                Optional<Integer> countInt = Utils.parseInt(replaced.substring(0, replaced.indexOf(" ")).replaceAll(",", ""));
                if (countInt.isPresent()) {
                    return Math.max(1, countInt.get());
                }
            }
            if (line.startsWith("Owned: ")) {
                int start = line.indexOf(":") + 2;
                int end = line.indexOf(" ", start);
                Optional<Integer> countInt = Utils.parseInt(line.substring(start, end).replaceAll(",", ""));
                if (countInt.isPresent()) {
                    return Math.max(1, countInt.get());
                }
            }
        }
        return stack.getCount();
    }

    private static long getPurchasedCost(ItemStack stack) {
        for (String line : Utils.getLoreLines(stack)) {
            if (line.startsWith("Cost: ")) {
                String cost = Utils.toLower(line.substring(line.indexOf(":") + 2));
                return Utils.parseLong(cost.replaceAll(",", "").replace("coins", "").trim()).orElse(0L);
            }
        }
        return 0L;
    }

    private static String getPurchasedUUID(ScreenHandler handler) {
        if (handler instanceof GenericContainerScreenHandler containerHandler) {
            for (Slot slot : Utils.getContainerSlots(containerHandler)) {
                NbtCompound data = Utils.getCustomData(slot.getStack());
                if (data != null && data.contains("uuid")) {
                    return data.getString("uuid", "");
                }
            }
        }
        return "";
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
            String itemId = Utils.getMarketId(event.stack);
            if (itemId.isEmpty()) return;
            int quantity = getStackQuantity(event.stack);
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
            if (pricePaid.value() && event.customData != null && event.customData.contains("uuid") && paidData.value().has("prices")) {
                String uuid = event.customData.getString("uuid", "");
                JsonObject data = paidData.value().get("prices").getAsJsonObject();
                if (data.has(uuid)) {
                    event.addLine(buildLine("§ePrice Paid", data.get(uuid).getAsLong(), 1));
                }
            }
        }
    }

    @EventHandler
    private static void onSlotClick(SlotClickEvent event) {
        if (instance.isActive() && pricePaid.value() && event.slot != null && event.title.equals("Confirm Purchase")) {
            ItemStack stack = event.slot.getStack();
            if (!stack.getItem().equals(Items.GREEN_TERRACOTTA)) {
                return;
            }
            long cost = getPurchasedCost(stack);
            String uuid = getPurchasedUUID(event.handler);
            if (cost > 0L && !uuid.isEmpty()) {
                if (!paidData.value().has("prices")) {
                    paidData.edit(object -> object.add("prices", new JsonObject()));
                }
                paidData.edit(object -> object.get("prices").getAsJsonObject().addProperty(uuid, cost));
            }
        }
    }
}
