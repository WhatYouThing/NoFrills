package nofrills.features.kuudra;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Formatting;
import nofrills.config.Feature;
import nofrills.config.SettingInt;
import nofrills.events.ScreenOpenEvent;
import nofrills.events.ScreenSlotUpdateEvent;
import nofrills.features.general.PriceTooltips;
import nofrills.misc.Utils;

import static nofrills.misc.NoFrillsAPI.auctionPricing;
import static nofrills.misc.NoFrillsAPI.bazaarPricing;

public class KuudraChestValue {
    public static final Feature instance = new Feature("kuudraChestValue");

    public static final SettingInt petBonus = new SettingInt(0, "petBonus", instance.key());

    public static double currentValue = 0.0;

    private static int getLootQuantity(ItemStack stack, String name) {
        String[] parts = name.split(" ");
        String last = parts[parts.length - 1];
        if (last.startsWith("x")) {
            try {
                int quantity = Integer.parseInt(last.replaceAll("x", "").replaceAll(",", ""));
                if (name.startsWith("Crimson Essence")) {
                    quantity = (int) Math.floor(quantity * (1 + petBonus.value() * 0.01));
                }
                return quantity;
            } catch (NumberFormatException ignored) {
            }
        }
        return stack.getCount();
    }

    private static String getLootID(ItemStack stack, String name) {
        NbtCompound data = Utils.getCustomData(stack);
        String id = Utils.getSkyblockId(data);
        if (id.isEmpty()) {
            if (name.startsWith("Crimson Essence")) {
                return "ESSENCE_CRIMSON";
            }
            if (name.contains(" Shard")) {
                return PriceTooltips.correctShardId(name.substring(0, name.indexOf("Shard")).trim().replaceAll(" ", "_").toUpperCase());
            }
        }
        return PriceTooltips.parseItemId(stack, data, "");
    }

    @EventHandler
    private static void onSlot(ScreenSlotUpdateEvent event) {
        if (instance.isActive() && Utils.isInKuudra() && event.title.endsWith("Chest")) {
            if (event.isInventory || event.stack.getItem().equals(Items.BLACK_STAINED_GLASS_PANE)) {
                return;
            }
            String name = Formatting.strip(event.stack.getName().getString());
            String id = getLootID(event.stack, name);
            int quantity = getLootQuantity(event.stack, name);
            if (auctionPricing.containsKey(id)) {
                currentValue += auctionPricing.get(id) * quantity;
            } else if (bazaarPricing.containsKey(id)) {
                currentValue += bazaarPricing.get(id).get("sell") * quantity;
            }
        }
    }

    @EventHandler
    private static void onScreen(ScreenOpenEvent event) {
        currentValue = 0.0;
    }
}
