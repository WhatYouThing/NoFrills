package nofrills.features.dungeons;

import com.google.common.collect.Sets;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.ScreenOpenEvent;
import nofrills.events.ScreenRenderEvent;
import nofrills.events.SlotUpdateEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.HashSet;
import java.util.Optional;

import static nofrills.Main.mc;
import static nofrills.misc.NoFrillsAPI.auctionPricing;
import static nofrills.misc.NoFrillsAPI.bazaarPricing;

public class DungeonChestValue {
    public static final Feature instance = new Feature("dungeonChestValue");

    public static final SettingColor background = new SettingColor(RenderColor.fromHex(0x202020, 0.8f), "background", instance);

    private static final HashSet<String> chestNames = Sets.newHashSet(
            "Wood",
            "Gold",
            "Diamond",
            "Emerald",
            "Obsidian",
            "Bedrock"
    );
    private static double currentValue = 0.0;

    private static boolean isChest(String title) {
        for (String name : chestNames) {
            if (title.equals(name) || (title.startsWith(name) && title.endsWith("Chest"))) {
                return true;
            }
        }
        return false;
    }

    private static int getLootQuantity(ItemStack stack, String name) {
        String[] parts = name.split(" ");
        String last = parts[parts.length - 1];
        if (last.startsWith("x")) {
            return Utils.parseInt(last.replaceAll("x", "").replaceAll(",", "")).orElse(stack.getCount());
        }
        return stack.getCount();
    }

    private static String getLootID(ItemStack stack, String name) {
        if (name.startsWith("Wither Essence")) {
            return "ESSENCE_WITHER";
        }
        if (name.startsWith("Undead Essence")) {
            return "ESSENCE_UNDEAD";
        }
        return Utils.getMarketId(stack);
    }

    @EventHandler
    private static void onSlot(SlotUpdateEvent event) {
        if (instance.isActive() && isChest(event.title) && Utils.isInLootArea()) {
            if (event.isInventory || event.stack.getItem().equals(Items.BLACK_STAINED_GLASS_PANE)) {
                return;
            }
            String name = Utils.toPlain(event.stack.getName());
            String id = getLootID(event.stack, name);
            if (id.isEmpty()) {
                if (name.equals("Open Reward Chest")) {
                    Optional<String> cost = Utils.getLoreLines(event.stack).stream().filter(line -> line.endsWith(" Coins")).findFirst();
                    if (cost.isPresent()) {
                        String value = cost.get().replace(" Coins", "").replaceAll(",", "");
                        currentValue -= Utils.parseInt(value).orElse(0);
                    }
                }
                return;
            }
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
        if (instance.isActive() && currentValue != 0.0) {
            Slot targetSlot = event.handler.getSlot(4);
            String value = Utils.format("Chest Value: {}", Utils.formatSeparator(currentValue));
            int width = mc.textRenderer.getWidth(value);
            int baseX = targetSlot.x + 8;
            int baseY = targetSlot.y + 8;
            event.context.fill((int) Math.floor(baseX - 2 - width * 0.5), baseY - 6, (int) Math.ceil(baseX + 2 + width * 0.5), baseY + 6, background.value().argb);
            event.context.drawCenteredTextWithShadow(mc.textRenderer, value, baseX, baseY - 4, currentValue > 0 ? RenderColor.green.argb : RenderColor.red.argb);
        }
    }

    @EventHandler
    private static void onScreen(ScreenOpenEvent event) {
        if (instance.isActive() && currentValue != 0.0) {
            currentValue = 0.0;
        }
    }
}
