package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.events.ScreenOpenEvent;
import nofrills.events.SlotUpdateEvent;
import nofrills.events.TooltipRenderEvent;
import nofrills.misc.DungeonUtil;
import nofrills.misc.RenderColor;
import nofrills.misc.SlotOptions;
import nofrills.misc.Utils;

import java.util.*;

import static nofrills.misc.NoFrillsAPI.auctionPricing;
import static nofrills.misc.NoFrillsAPI.bazaarPricing;

public class CroesusSolver {
    public static final Feature instance = new Feature("croesusSolver").requiresPricingAPI();

    public static final SettingColor profitColor = new SettingColor(RenderColor.fromHex(0x55FF55), "profitColor", instance);
    public static final SettingColor profitSecondaryColor = new SettingColor(RenderColor.fromHex(0xFFFF55), "profitSecondaryColor", instance);
    public static final SettingColor profitKeyColor = new SettingColor(RenderColor.fromHex(0x55FFFF), "profitKeyColor", instance);
    public static final SettingColor unopenedColor = new SettingColor(RenderColor.fromHex(0x55FF55), "unopenedColor", instance);
    public static final SettingColor rerolledColor = new SettingColor(RenderColor.fromHex(0x55FFFF), "rerolledColor", instance);
    public static final SettingColor openedColor = new SettingColor(RenderColor.fromHex(0xFF5555), "openedColor", instance);
    public static final SettingColor openedKeyColor = new SettingColor(RenderColor.fromHex(0x555555), "openedKeyColor", instance);
    public static final SettingBool valueTooltip = new SettingBool(true, "valueTooltip", instance);
    public static final SettingBool floorLabel = new SettingBool(true, "floorLabel", instance);

    private static final HashMap<Slot, Double> chestValues = new HashMap<>();

    private static LootState getLootState(ItemStack stack) {
        for (String string : Utils.getLoreLines(stack)) {
            if (string.equals("No chests opened yet!")) {
                for (Component text : Utils.getLoreText(stack)) {
                    Optional<Style> style = Utils.getStyle(text, line -> line.endsWith("Kismet Feather"));
                    if (style.isPresent() && style.get().isStrikethrough()) {
                        return LootState.Rerolled;
                    }
                }
                return LootState.Unopened;
            }
            if (string.startsWith("Opened Chest: ")) return LootState.Opened;
            if (string.equals("No more chests to open!")) return LootState.OpenedKey;
        }
        return LootState.Unknown;
    }

    private static void highlightLoot(ItemStack stack, Slot slot) {
        String name = Utils.toPlain(stack.getHoverName());
        if (!name.endsWith("The Catacombs")) return;
        RenderColor color = switch (getLootState(stack)) {
            case Unopened -> unopenedColor.value();
            case Rerolled -> rerolledColor.value();
            case Opened -> openedColor.value();
            case OpenedKey -> openedKeyColor.value();
            case Unknown -> null;
        };
        if (color == null) return;
        SlotOptions.setBackground(slot, color);
        if (floorLabel.value()) {
            String prefix = name.startsWith("Master Mode") ? "M" : "F";
            String floorLine = Utils.getLoreLines(stack).getFirst();
            int floor = Utils.parseRoman(floorLine.substring(floorLine.lastIndexOf(" ") + 1));
            SlotOptions.setCount(slot, prefix + floor);
        }
    }

    private static void highlightChest(ItemStack stack, Slot slot) {
        String name = Utils.toPlain(stack.getHoverName());
        if (!DungeonUtil.getChestNames().contains(name)) return;
        List<Component> lore = Utils.getLoreText(stack);
        double value = 0;
        double cost = 0;
        int costIndex = -1;
        for (int i = 0; i < lore.size(); i++) {
            Component text = lore.get(i);
            String line = Utils.toPlain(text);
            if (line.isEmpty() || line.equals("Contents") || line.equals("Cost")) {
                if (line.equals("Cost")) costIndex = i;
                if (line.isEmpty() && costIndex != -1) break;
                continue;
            }
            if (costIndex == -1) {
                String id = Utils.getMarketId(text);
                int quantity = Utils.hasItemQuantity(line) ? Utils.parseInt(line.substring(line.lastIndexOf("x") + 1)).orElse(0) : 1;
                if (bazaarPricing.containsKey(id)) {
                    value += bazaarPricing.get(id).sell() * quantity;
                } else if (auctionPricing.containsKey(id)) {
                    value += auctionPricing.get(id) * quantity;
                }
            } else {
                if (line.endsWith(" Coins")) {
                    cost += Utils.parseInt(line.substring(0, line.indexOf(" ")).replaceAll(",", "")).orElse(0);
                }
            }
        }
        chestValues.put(slot, value - cost);
        List<Map.Entry<Slot, Double>> chests = new ArrayList<>(chestValues.entrySet());
        chests.sort(Comparator.comparingDouble(Map.Entry::getValue));
        chests = chests.reversed();
        if (!chests.isEmpty()) {
            Map.Entry<Slot, Double> entry = chests.getFirst();
            if (entry.getValue() > 0) {
                SlotOptions.clearBackground();
                SlotOptions.setBackground(entry.getKey(), profitColor.value());
            }
        }
        if (chests.size() >= 2) {
            Map.Entry<Slot, Double> entry = chests.get(1);
            if (bazaarPricing.containsKey("DUNGEON_CHEST_KEY") && entry.getValue() - bazaarPricing.get("DUNGEON_CHEST_KEY").buy() > 0) {
                SlotOptions.setBackground(entry.getKey(), profitKeyColor.value());
            } else if (entry.getValue() > 0) {
                SlotOptions.setBackground(entry.getKey(), profitSecondaryColor.value());
            }
        }
    }

    @EventHandler
    private static void onSlotUpdate(SlotUpdateEvent event) {
        if (instance.isActive() && !event.stack.isEmpty() && Utils.isInLootArea()) {
            if (event.title.equals("Croesus")) {
                highlightLoot(event.stack, event.slot);
            }
            if (event.title.startsWith("Catacombs - Floor") || event.title.startsWith("Master Catacombs - Floor")) {
                highlightChest(event.stack, event.slot);
            }
        }
    }

    @EventHandler
    private static void onTooltip(TooltipRenderEvent event) {
        if (instance.isActive() && valueTooltip.value() && Utils.isInLootArea()) {
            Slot slot = Utils.getFocusedSlot();
            if (slot != null && chestValues.containsKey(slot)) {
                double value = chestValues.get(slot);
                MutableComponent valueText = Component.literal(Utils.formatSeparator(value)).withColor(value > 0 ? RenderColor.green.argb : RenderColor.red.argb);
                event.addLine(Utils.getShortTag().append("§bChest Value: §r").append(valueText));
            }
        }
    }

    @EventHandler
    private static void onScreen(ScreenOpenEvent event) {
        chestValues.clear();
    }

    public enum LootState {
        Unopened,
        Rerolled,
        Opened,
        OpenedKey,
        Unknown
    }
}
