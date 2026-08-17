package nofrills.features.kuudra;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.config.SettingDouble;
import nofrills.events.EventListener;
import nofrills.events.ScreenOpenEvent;
import nofrills.events.SlotUpdateEvent;
import nofrills.events.TooltipRenderEvent;
import nofrills.misc.NoFrillsAPI;
import nofrills.misc.RenderColor;
import nofrills.misc.SlotOptions;
import nofrills.misc.Utils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static nofrills.misc.NoFrillsAPI.auctionPricing;
import static nofrills.misc.NoFrillsAPI.bazaarPricing;

@EventListener
public class VesuviusSolver {
    public static final Feature instance = new Feature("vesuviusSolver", Feature.Flags.UsePricingAPI);

    public static final SettingColor profitColor = new SettingColor(RenderColor.fromFormat(ChatFormatting.GREEN), "profitColor", instance);
    public static final SettingColor profitHighColor = new SettingColor(RenderColor.fromFormat(ChatFormatting.LIGHT_PURPLE), "profitHighColor", instance);
    public static final SettingDouble profitHighThreshold = new SettingDouble(5_000_000.0, "profitHighThreshold", instance);
    public static final SettingColor unopenedColor = new SettingColor(RenderColor.fromFormat(ChatFormatting.GREEN), "unopenedColor", instance);
    public static final SettingColor rerolledColor = new SettingColor(RenderColor.fromFormat(ChatFormatting.AQUA), "rerolledColor", instance);
    public static final SettingColor openedColor = new SettingColor(RenderColor.fromFormat(ChatFormatting.RED), "openedColor", instance);
    public static final SettingBool valueTooltip = new SettingBool(true, "valueTooltip", instance);
    public static final SettingBool tierLabel = new SettingBool(true, "tierLabel", instance);

    private static final HashMap<Slot, Double> chestValues = new HashMap<>();

    private static LootState getLootState(ItemStack stack) {
        for (String string : Utils.getLoreLines(stack)) {
            if (string.startsWith("Chests expire ")) {
                for (Component text : Utils.getLoreText(stack)) {
                    Optional<Style> style = Utils.getStyle(text, line -> line.endsWith("Kismet Feather"));
                    if (style.isPresent() && style.get().isStrikethrough()) {
                        return LootState.Rerolled;
                    }
                }
                return LootState.Unopened;
            }
            if (string.equals("No more chests to open!")) return LootState.Opened;
        }
        return LootState.Unknown;
    }

    private static void highlightLoot(ItemStack stack, Slot slot) {
        String name = Utils.toPlain(stack.getHoverName());
        if (!name.equals("Kuudra's Hollow")) return;
        RenderColor color = switch (getLootState(stack)) {
            case Unopened -> unopenedColor.value();
            case Rerolled -> rerolledColor.value();
            case Opened -> openedColor.value();
            case Unknown -> null;
        };
        if (color == null) return;
        SlotOptions.setBackground(slot, color);
        if (tierLabel.value()) {
            String tierLine = Utils.getLoreLines(stack).getFirst();
            SlotOptions.setCount(slot, switch (tierLine) {
                case "Hot Tier" -> "T2";
                case "Burning Tier" -> "T3";
                case "Fiery Tier" -> "T4";
                case "Infernal Tier" -> "T5";
                default -> "T1";
            });
        }
    }

    private static void highlightChest(ItemStack stack, Slot slot) {
        String name = Utils.toPlain(stack.getHoverName());
        if (!KuudraChestValue.isLootChest(name)) return;
        List<Component> lore = Utils.getLoreText(stack);
        double value = 0;
        double cost = 0;
        int costIndex = -1;
        boolean hasDye = false;
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
                if (id.startsWith("DYE_")) {
                    hasDye = true;
                }
                int quantity = Utils.hasItemQuantity(line) ? Utils.parseInt(line.substring(line.lastIndexOf("x") + 1)).orElse(0) : 1;
                if (id.equals("ESSENCE_CRIMSON")) {
                    quantity = (int) Math.floor(quantity * KuudraChestValue.getEssenceMultiplier());
                }
                if (KuudraChestValue.salvageValue.value() && KuudraChestValue.salvageAmounts.containsKey(id)) {
                    AtomicInteger stars = new AtomicInteger();
                    text.visit((textStyle, textString) -> {
                        int count = (int) Pattern.compile(Utils.Symbols.dungeonStar).matcher(textString).results().count();
                        if (Utils.hasColor(textStyle, ChatFormatting.LIGHT_PURPLE)) {
                            stars.addAndGet(count * 2);
                        } else {
                            stars.addAndGet(count);
                        }
                        return Optional.empty();
                    }, Style.EMPTY);
                    int essenceAmount = (int) Math.floor(KuudraChestValue.salvageAmounts.get(id) + KuudraChestValue.getStarCost(stars.get()) * 0.6);
                    value += bazaarPricing.getOrDefault("ESSENCE_CRIMSON", NoFrillsAPI.BazaarPrice.ZERO).sell() * essenceAmount;
                } else {
                    if (auctionPricing.containsKey(id)) {
                        value += auctionPricing.get(id) * quantity;
                    } else if (bazaarPricing.containsKey(id)) {
                        value += bazaarPricing.get(id).sell() * quantity;
                    }
                }
            } else {
                if (line.endsWith(" Kuudra Key")) {
                    cost += KuudraChestValue.getKeyPrice(KuudraChestValue.getKeyTier(line));
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
                SlotOptions.setBackground(entry.getKey(), hasDye || entry.getValue() >= profitHighThreshold.value()
                        ? profitHighColor.value()
                        : profitColor.value());
            }
        }
    }

    @EventHandler
    private static void onSlotUpdate(SlotUpdateEvent event) {
        if (instance.isActive() && !event.stack.isEmpty() && !event.isInventory && Utils.isInLootArea()) {
            if (Utils.isPaginatedMenu(event.title, "Croesus") || Utils.isPaginatedMenu(event.title, "Vesuvius")) {
                highlightLoot(event.stack, event.slot);
            } else if (event.title.startsWith("Kuudra - ")) {
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
                MutableComponent valueText = Component.literal(Utils.formatSeparator(value)).withColor(value > 0 ? RenderColor.GREEN.argb : RenderColor.RED.argb);
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
        Unknown
    }
}
