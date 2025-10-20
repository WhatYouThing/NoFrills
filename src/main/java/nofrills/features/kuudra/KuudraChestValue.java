package nofrills.features.kuudra;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import nofrills.config.Feature;
import nofrills.config.SettingInt;
import nofrills.events.ScreenOpenEvent;
import nofrills.events.ScreenRenderEvent;
import nofrills.events.SlotUpdateEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.Optional;

import static nofrills.Main.mc;
import static nofrills.misc.NoFrillsAPI.auctionPricing;
import static nofrills.misc.NoFrillsAPI.bazaarPricing;

public class KuudraChestValue {
    public static final Feature instance = new Feature("kuudraChestValue");

    public static final SettingInt petBonus = new SettingInt(0, "petBonus", instance.key());
    public static final RenderColor background = RenderColor.fromHex(0x202020, 0.75f);
    private static double currentValue = 0.0;

    private static int getLootQuantity(ItemStack stack, String name) {
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
        return Utils.getMarketId(stack);
    }

    @EventHandler
    private static void onSlot(SlotUpdateEvent event) {
        if (instance.isActive() && Utils.isInKuudra() && event.title.endsWith("Chest")) {
            if (event.isInventory || event.stack.getItem().equals(Items.BLACK_STAINED_GLASS_PANE)) {
                return;
            }
            String name = Utils.toPlain(event.stack.getName());
            String id = getLootID(event.stack, name);
            if (id.isEmpty()) return;
            int quantity = getLootQuantity(event.stack, name);
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
            event.context.fill((int) Math.floor(baseX - 2 - width * 0.5), baseY - 6, (int) Math.ceil(baseX + 2 + width * 0.5), baseY + 6, background.argb);
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
