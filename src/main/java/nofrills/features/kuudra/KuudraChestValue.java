package nofrills.features.kuudra;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.config.SettingInt;
import nofrills.events.ScreenOpenEvent;
import nofrills.events.ScreenRenderEvent;
import nofrills.events.SlotUpdateEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.HashMap;
import java.util.Optional;

import static nofrills.Main.mc;
import static nofrills.misc.NoFrillsAPI.auctionPricing;
import static nofrills.misc.NoFrillsAPI.bazaarPricing;

public class KuudraChestValue {
    public static final Feature instance = new Feature("kuudraChestValue");

    public static final SettingInt petBonus = new SettingInt(0, "petBonus", instance);
    public static final SettingBool salvageValue = new SettingBool(false, "salvageValue", instance);
    public static final SettingColor background = new SettingColor(RenderColor.fromHex(0x202020, 0.8f), "background", instance);

    private static final HashMap<String, Integer> salvageAmounts = buildSalvageAmounts();
    private static double currentValue = 0.0;

    private static HashMap<String, Integer> buildSalvageAmounts() {
        HashMap<String, Integer> map = new HashMap<>();
        for (String equipment : new String[]{"NECKLACE", "CLOAK", "BELT", "BRACELET"}) {
            map.put("MOLTEN_" + equipment, 600);
        }
        for (String armor : new String[]{"AURORA", "CRIMSON", "TERROR", "FERVOR", "HOLLOW"}) {
            for (String piece : new String[]{"HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"}) {
                map.put(armor + "_" + piece, 120);
            }
        }
        map.put("RUNIC_STAFF", 600);
        map.put("HOLLOW_WAND", 600);
        return map;
    }

    private static boolean isLootChest(String title) {
        return (title.startsWith("Free ") || title.startsWith("Paid ")) && title.endsWith(" Chest");
    }

    private static int getLootQuantity(ItemStack stack, String name, String id) {
        if (salvageValue.value() && salvageAmounts.containsKey(id)) {
            int amount = salvageAmounts.get(id);
            if (amount != 120) return amount;
            NbtCompound data = Utils.getCustomData(stack);
            int stars = data != null ? data.getInt("upgrade_level", 0) : 0;
            int starCost = 0;
            for (int i = 1; i <= stars; i++) {
                starCost += i > 7 ? i * 10 - 10 : i * 5 + 25; // simple formula for the price of each star on a basic tier piece
            }
            return (int) Math.floor(amount + (starCost * 0.6));
        }
        String[] parts = name.split(" ");
        String last = parts[parts.length - 1];
        if (last.startsWith("x")) {
            Optional<Integer> quantity = Utils.parseInt(last.replaceAll("x", "").replaceAll(",", ""));
            if (quantity.isPresent()) {
                if (name.startsWith("Crimson Essence")) {
                    return (int) Math.floor(quantity.get() * (1 + petBonus.value() * 0.01));
                }
                return quantity.get();
            }
        }
        return stack.getCount();
    }

    private static String getLootID(ItemStack stack, String name) {
        if (name.startsWith("Crimson Essence")) {
            return "ESSENCE_CRIMSON";
        }
        String id = Utils.getMarketId(stack);
        if (salvageValue.value() && salvageAmounts.containsKey(id)) {
            return "ESSENCE_CRIMSON";
        }
        return id;
    }

    @EventHandler
    private static void onSlot(SlotUpdateEvent event) {
        if (instance.isActive() && isLootChest(event.title) && Utils.isInLootArea()) {
            if (event.isInventory || event.stack.getItem().equals(Items.BLACK_STAINED_GLASS_PANE)) {
                return;
            }
            String name = Utils.toPlain(event.stack.getName());
            String id = getLootID(event.stack, name);
            if (id.isEmpty()) return;
            int quantity = getLootQuantity(event.stack, name, id);
            if (auctionPricing.containsKey(id)) {
                currentValue += auctionPricing.get(id) * quantity;
            } else if (bazaarPricing.containsKey(id)) {
                currentValue += bazaarPricing.get(id).get("sell") * quantity;
            }
        }
    }

    @EventHandler
    private static void onRender(ScreenRenderEvent.After event) {
        if (instance.isActive() && currentValue > 0.0) {
            Slot targetSlot = event.handler.getSlot(4);
            String value = Utils.format("Chest Value: {}", Utils.formatSeparator(currentValue));
            int width = mc.textRenderer.getWidth(value);
            int baseX = targetSlot.x + 8;
            int baseY = targetSlot.y + 8;
            event.context.fill((int) Math.floor(baseX - 2 - width * 0.5), baseY - 6, (int) Math.ceil(baseX + 2 + width * 0.5), baseY + 6, background.value().argb);
            event.context.drawCenteredTextWithShadow(mc.textRenderer, value, baseX, baseY - 4, RenderColor.green.argb);
        }
    }

    @EventHandler
    private static void onScreen(ScreenOpenEvent event) {
        if (instance.isActive() && currentValue > 0.0) {
            currentValue = 0.0;
        }
    }
}
