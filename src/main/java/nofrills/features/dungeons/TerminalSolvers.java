package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.DyeColor;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.SlotClickEvent;
import nofrills.events.SlotUpdateEvent;
import nofrills.misc.SlotOptions;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TerminalSolvers {
    public static final Feature instance = new Feature("terminalSolvers");

    public static final SettingBool panes = new SettingBool(false, "panes", instance.key());
    public static final SettingBool startsWith = new SettingBool(false, "startsWith", instance.key());
    public static final SettingBool select = new SettingBool(false, "select", instance.key());
    public static final SettingBool inOrder = new SettingBool(false, "inOrder", instance.key());
    public static final SettingBool colors = new SettingBool(false, "colors", instance.key());
    public static final SettingBool instant = new SettingBool(false, "instant", instance.key());

    private static final List<Item> colorsOrder = List.of(
            Items.GREEN_STAINED_GLASS_PANE,
            Items.YELLOW_STAINED_GLASS_PANE,
            Items.ORANGE_STAINED_GLASS_PANE,
            Items.RED_STAINED_GLASS_PANE,
            Items.BLUE_STAINED_GLASS_PANE
    );

    public static TerminalType getTerminalType(String title) {
        if (title.startsWith("Correct all the panes!")) return TerminalType.Panes;
        if (title.startsWith("Click in order!")) return TerminalType.InOrder;
        if (title.startsWith("What starts with:") && title.endsWith("?")) return TerminalType.StartsWith;
        if (title.startsWith("Select all the") && title.endsWith("items!")) return TerminalType.Select;
        if (title.startsWith("Change all to same color!")) return TerminalType.Colors;
        if (title.equals("Click the button on time!")) return TerminalType.Melody;
        return TerminalType.None;
    }

    public static boolean isInTerminal(String title) {
        return !getTerminalType(title).equals(TerminalType.None);
    }

    public static boolean shouldHideTooltips(String title) {
        return instance.isActive() && isInTerminal(title);
    }

    private static boolean checkStackColor(ItemStack stack, DyeColor color, String colorName) {
        Item item = stack.getItem();
        if (Utils.toPlain(stack.getName()).trim().isEmpty()) {
            return false;
        }
        if (stack.getItem().toString().startsWith("minecraft:" + colorName)) {
            return true;
        }
        return switch (color) {
            case BLACK -> item.equals(Items.INK_SAC);
            case BLUE -> item.equals(Items.LAPIS_LAZULI);
            case BROWN -> item.equals(Items.COCOA_BEANS);
            case WHITE -> item.equals(Items.BONE_MEAL);
            default -> false;
        };
    }

    private static ItemStack stackWithCount(int count) {
        return SlotOptions.stackWithQuantity(count > 0 ? SlotOptions.first : SlotOptions.second, Math.abs(count));
    }

    @EventHandler
    private static void onSlotUpdate(SlotUpdateEvent event) {
        if (instance.isActive() && Utils.isOnDungeonFloor("7")) {
            TerminalType type = getTerminalType(event.title);
            if (event.isInventory || event.slot == null || type.equals(TerminalType.None)) {
                return;
            }
            if (type.equals(TerminalType.Panes) && panes.value()) {
                if (event.stack.getItem().equals(Items.RED_STAINED_GLASS_PANE)) {
                    SlotOptions.spoofSlot(event.slot, SlotOptions.first);
                    SlotOptions.disableSlot(event.slot, false);
                } else {
                    SlotOptions.spoofSlot(event.slot, SlotOptions.background);
                    SlotOptions.disableSlot(event.slot, true);
                }
            }
            if (type.equals(TerminalType.StartsWith) && startsWith.value()) {
                String character = Utils.toLower(String.valueOf(event.title.charAt(event.title.indexOf("'") + 1)));
                String name = Utils.toLower(Utils.toPlain(event.stack.getName())).trim();
                if (!name.isEmpty() && name.startsWith(character) && !Utils.hasGlint(event.stack)) {
                    SlotOptions.spoofSlot(event.slot, SlotOptions.first);
                    SlotOptions.disableSlot(event.slot, false);
                } else {
                    SlotOptions.spoofSlot(event.slot, SlotOptions.background);
                    SlotOptions.disableSlot(event.slot, true);
                }
            }
            if (type.equals(TerminalType.Select) && select.value()) {
                String color = event.title.replace("Select all the", "").replace("items!", "").trim();
                String colorName = color.equals("SILVER") ? "light_gray" : Utils.toLower(color).replace(" ", "_");
                for (DyeColor dye : DyeColor.values()) {
                    if (dye.getId().equals(colorName)) {
                        if (!Utils.hasGlint(event.stack) && checkStackColor(event.stack, dye, colorName)) {
                            SlotOptions.spoofSlot(event.slot, SlotOptions.first);
                            SlotOptions.disableSlot(event.slot, false);
                        } else {
                            SlotOptions.spoofSlot(event.slot, SlotOptions.background);
                            SlotOptions.disableSlot(event.slot, true);
                        }
                        break;
                    }
                }
            }
            if (type.equals(TerminalType.InOrder) && inOrder.value()) {
                List<Slot> orderSlots = new ArrayList<>();
                for (Slot slot : event.handler.slots) {
                    ItemStack stack = event.inventory.getStack(slot.id);
                    if (!stack.isEmpty()) {
                        Item item = stack.getItem();
                        SlotOptions.spoofSlot(event.slot, SlotOptions.background);
                        SlotOptions.disableSlot(event.slot, true);
                        if (item.equals(Items.RED_STAINED_GLASS_PANE) || item.equals(Items.LIME_STAINED_GLASS_PANE)) {
                            orderSlots.add(slot);
                        }
                    }
                }
                if (orderSlots.size() == 14) { // scuffed way to ensure every slot is sent in by the server
                    orderSlots.removeIf(slot -> slot.getStack().getItem().equals(Items.LIME_STAINED_GLASS_PANE));
                    if (orderSlots.isEmpty()) {
                        return;
                    }
                    orderSlots.sort(Comparator.comparingInt(slot -> slot.getStack().getCount()));
                    Slot first = orderSlots.getFirst();
                    SlotOptions.spoofSlot(first, SlotOptions.stackWithQuantity(SlotOptions.first, first.getStack().getCount()));
                    SlotOptions.disableSlot(first, false);
                    if (orderSlots.size() > 1) {
                        Slot second = orderSlots.get(1);
                        SlotOptions.spoofSlot(second, SlotOptions.stackWithQuantity(SlotOptions.second, second.getStack().getCount()));
                        SlotOptions.disableSlot(second, true);
                    }
                    if (orderSlots.size() > 2) {
                        Slot third = orderSlots.get(2);
                        SlotOptions.spoofSlot(third, SlotOptions.stackWithQuantity(SlotOptions.third, third.getStack().getCount()));
                        SlotOptions.disableSlot(third, true);
                    }
                }
            }
            if (type.equals(TerminalType.Colors) && colors.value()) {
                List<Slot> colorSlots = new ArrayList<>();
                for (Slot slot : event.handler.slots) {
                    ItemStack stack = event.inventory.getStack(slot.id);
                    if (!stack.isEmpty()) {
                        SlotOptions.spoofSlot(event.slot, SlotOptions.background);
                        SlotOptions.disableSlot(event.slot, true);
                        if (colorsOrder.contains(stack.getItem()) && !colorSlots.contains(slot)) {
                            colorSlots.add(slot);
                        }
                    }
                }
                if (colorSlots.size() == 9) {
                    int[] colorCounts = {0, 0, 0, 0, 0};
                    for (Slot slot : colorSlots) {
                        int index = colorsOrder.indexOf(slot.getStack().getItem());
                        colorCounts[index] += 1;
                    }
                    int mostCommon = -1, highestCommon = 0;
                    for (int i = 0; i < 5; i++) {
                        if (colorCounts[i] > highestCommon) {
                            highestCommon = colorCounts[i];
                            mostCommon = i;
                        }
                    }
                    for (Slot slot : colorSlots) {
                        int index = colorsOrder.indexOf(slot.getStack().getItem());
                        int target = Math.negateExact(mostCommon - index);
                        if (Math.abs(target) > 2) {
                            int offset = Math.abs(target) == 4 ? 3 : 1;
                            target = Math.negateExact(target) + (target > 0 ? offset : -offset);
                        }
                        if (target == 0) {
                            SlotOptions.spoofSlot(slot, SlotOptions.background);
                            SlotOptions.disableSlot(slot, true);
                        } else {
                            SlotOptions.spoofSlot(slot, stackWithCount(target));
                            SlotOptions.disableSlot(slot, false);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onSlotClick(SlotClickEvent event) {
        if (instance.isActive() && Utils.isOnDungeonFloor("7") && event.slot != null && instant.value()) {
            TerminalType type = getTerminalType(event.title);
            if (type.equals(TerminalType.None) || type.equals(TerminalType.Melody) || type.equals(TerminalType.Colors)) {
                return;
            }
            SlotOptions.spoofSlot(event.slot, SlotOptions.background);
            SlotOptions.disableSlot(event.slot, true);
        }
    }

    public enum TerminalType {
        Panes,
        InOrder,
        StartsWith,
        Select,
        Colors,
        Melody,
        None
    }
}