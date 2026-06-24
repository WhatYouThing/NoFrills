package nofrills.features.general;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import nofrills.config.DataFile;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingInt;
import nofrills.events.EventListener;
import nofrills.events.GameShutdownEvent;
import nofrills.events.SlotClickEvent;
import nofrills.events.TooltipRenderEvent;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.Optional;

import static nofrills.misc.NoFrillsAPI.*;

@EventListener
public class PriceTooltips {
    public static final Feature instance = new Feature("priceTooltips", Feature.Flags.UsePricingAPI);

    public static final SettingBool auction = new SettingBool(false, "auction", instance.key());
    public static final SettingBool bazaar = new SettingBool(false, "bazaar", instance.key());
    public static final SettingBool npc = new SettingBool(false, "npc", instance.key());
    public static final SettingBool mote = new SettingBool(false, "mote", instance.key());
    public static final SettingInt burgers = new SettingInt(0, "burgers", instance.key());
    public static final SettingBool pricePaid = new SettingBool(false, "pricePaid", instance);

    private static final DataFile data = new DataFile("PricePaid.json");

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

    private static String getPurchasedUUID(AbstractContainerMenu handler) {
        for (Slot slot : Utils.getContainerSlots(handler)) {
            CompoundTag data = Utils.getCustomData(slot.getItem());
            if (data != null && data.contains("uuid")) {
                return data.getStringOr("uuid", "");
            }
        }
        return "";
    }

    private static Component buildLine(String name, double price, int quantity) {
        String line = Utils.format(
                "{}: §6{} {}",
                name,
                Utils.formatSeparator(price * quantity),
                quantity > 1 ? Utils.format("§8({}x {})", Utils.formatSeparator(quantity), Utils.formatSeparator(price)) : ""
        ).trim();
        return Utils.getShortTag().append(Component.literal(line).withColor(0xffffff));
    }

    private static Component buildLine(String name, long price, int quantity) {
        String line = Utils.format(
                "{}: §6{} {}",
                name,
                Utils.formatSeparator(price * quantity),
                quantity > 1 ? Utils.format("§8({}x {})", Utils.formatSeparator(quantity), Utils.formatSeparator(price)) : ""
        ).trim();
        return Utils.getShortTag().append(Component.literal(line).withColor(0xffffff));
    }

    @EventHandler
    private static void onTooltip(TooltipRenderEvent event) {
        if (instance.isActive()) {
            String itemId = Utils.getMarketId(event.stack);
            if (itemId.isEmpty()) return;
            int quantity = getStackQuantity(event.stack);
            if (mote.value() && npcPricing.containsKey(itemId) && SkyblockData.getArea().equals("The Rift")) {
                NPCPrice prices = npcPricing.get(itemId);
                if (prices.mote() != 0.0) {
                    double burgerBonus = 1 + 0.05 * burgers.value();
                    event.addLine(buildLine("§dMotes Price", prices.mote() * burgerBonus, quantity));
                }
            }
            if (npc.value() && npcPricing.containsKey(itemId)) {
                NPCPrice prices = npcPricing.get(itemId);
                if (prices.coin() != 0.0) {
                    event.addLine(buildLine("§eNPC Price", prices.coin(), quantity));
                }
            }
            if (auction.value() && auctionPricing.containsKey(itemId)) {
                event.addLine(buildLine("§eLowest BIN", auctionPricing.get(itemId), quantity));
            }
            if (bazaar.value() && bazaarPricing.containsKey(itemId)) {
                BazaarPrice prices = bazaarPricing.get(itemId);
                event.addLine(buildLine("§eBazaar Buy", prices.buy(), quantity));
                event.addLine(buildLine("§eBazaar Sell", prices.sell(), quantity));
            }
            if (pricePaid.value() && event.customData != null && event.customData.contains("uuid")) {
                String uuid = event.customData.getStringOr("uuid", "");
                if (!uuid.isEmpty() && data.get().has(uuid)) {
                    event.addLine(buildLine("§ePrice Paid", data.get().get(uuid).getAsLong(), 1));
                }
            }
        }
    }

    @EventHandler
    private static void onSlotClick(SlotClickEvent event) {
        if (instance.isActive() && pricePaid.value() && event.slot != null && event.title.equals("Confirm Purchase")) {
            ItemStack stack = event.slot.getItem();
            if (!stack.getItem().equals(Items.GREEN_TERRACOTTA)) {
                return;
            }
            long cost = getPurchasedCost(stack);
            String uuid = getPurchasedUUID(event.handler);
            if (cost > 0L && !uuid.isEmpty()) {
                data.get().addProperty(uuid, cost);
                data.save();
            }
        }
    }

    @EventHandler
    private static void onShutdown(GameShutdownEvent event) {
        if (instance.isActive()) {
            data.saveBlocking();
        }
    }
}
