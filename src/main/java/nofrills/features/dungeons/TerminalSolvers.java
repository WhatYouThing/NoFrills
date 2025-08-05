package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingString;
import nofrills.events.ScreenOpenEvent;
import nofrills.events.ScreenSlotUpdateEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.SlotOptions;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TerminalSolvers {
    public static final Feature instance = new Feature("terminalSolvers");

    public static final SettingBool melody = new SettingBool(false, "melody", instance.key());
    public static final SettingString melodyMsg = new SettingString("/pc MELODY", "melodyMsg", instance.key());

    private static final List<Item> colorsOrder = List.of(
            Items.GREEN_STAINED_GLASS_PANE,
            Items.YELLOW_STAINED_GLASS_PANE,
            Items.ORANGE_STAINED_GLASS_PANE,
            Items.RED_STAINED_GLASS_PANE,
            Items.BLUE_STAINED_GLASS_PANE
    );
    public static boolean isInTerminal = false;
    private static boolean isTerminalBuilt = false;
    private static int melodyTicks = 0;

    public static boolean shouldHideTooltips() {
        return instance.isActive() && isInTerminal;
    }

    private static boolean checkStackColor(ItemStack stack, DyeColor color, String colorName) {
        Item item = stack.getItem();
        if (Formatting.strip(stack.getName().getString()).trim().isEmpty()) {
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
        ItemStack stack = count > 0 ? Items.LIME_CONCRETE.getDefaultStack() : Items.BLUE_CONCRETE.getDefaultStack();
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.of(" "));
        stack.setCount(Math.abs(count));
        return stack;
    }

    @EventHandler
    private static void onScreenOpen(ScreenOpenEvent event) {
        isTerminalBuilt = false;
        isInTerminal = false;

        if (instance.isActive() && Utils.isInDungeons() && event.screen.getTitle().getString().equals("Click the button on time!")) {
            isInTerminal = true;
            if (melody.value()) {
                if (melodyTicks == 0 && !melodyMsg.value().isEmpty()) {
                    Utils.sendMessage(melodyMsg.value());
                    melodyTicks = 100;
                }
            }
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && Utils.isOnDungeonFloor("7")) {
            if (melodyTicks > 0) {
                melodyTicks--;
            }
        }
    }

    @EventHandler
    private static void onSlotUpdate(ScreenSlotUpdateEvent event) {
        if (instance.isActive() && Utils.isInDungeons() && !isTerminalBuilt) {
            isTerminalBuilt = event.isFinal;
            List<Slot> orderSlots = new ArrayList<>();
            List<Slot> colorSlots = new ArrayList<>();
            for (Slot slot : event.handler.slots) {
                ItemStack stack = event.inventory.getStack(slot.id);
                if (!stack.isEmpty()) {
                    if (event.title.startsWith("Correct all the panes!")) {
                        isInTerminal = true;
                        if (stack.getItem().equals(Items.RED_STAINED_GLASS_PANE)) {
                            Utils.setSpoofed(slot, SlotOptions.first);
                            Utils.setDisabled(slot, false);
                        } else {
                            Utils.setSpoofed(slot, SlotOptions.background);
                            Utils.setDisabled(slot, true);
                        }
                    }
                    if (event.title.startsWith("Click in order!")) {
                        isInTerminal = true;
                        if (stack.getItem().equals(Items.RED_STAINED_GLASS_PANE) && event.isFinal) {
                            orderSlots.add(slot);
                        } else {
                            Utils.setSpoofed(slot, SlotOptions.background);
                            Utils.setDisabled(slot, true);
                        }
                    }
                    if (event.title.startsWith("What starts with:") && event.title.endsWith("?")) {
                        isInTerminal = true;
                        String character = String.valueOf(event.title.charAt(event.title.indexOf("'") + 1)).toLowerCase();
                        String name = Formatting.strip(stack.getName().getString()).toLowerCase().trim();
                        if (!name.isEmpty() && name.startsWith(character) && !Utils.hasGlint(stack)) {
                            Utils.setSpoofed(slot, SlotOptions.first);
                            Utils.setDisabled(slot, false);
                        } else {
                            Utils.setSpoofed(slot, SlotOptions.background);
                            Utils.setDisabled(slot, true);
                        }
                    }
                    if (event.title.startsWith("Select all the") && event.title.endsWith("items!")) {
                        isInTerminal = true;
                        String color = event.title.replace("Select all the", "").replace("items!", "").trim();
                        String colorName = color.equals("SILVER") ? "light_gray" : color.toLowerCase().replace(" ", "_");
                        for (DyeColor dye : DyeColor.values()) {
                            if (dye.getId().equals(colorName)) {
                                if (!Utils.hasGlint(stack) && checkStackColor(stack, dye, colorName)) {
                                    Utils.setSpoofed(slot, SlotOptions.first);
                                    Utils.setDisabled(slot, false);
                                } else {
                                    Utils.setSpoofed(slot, SlotOptions.background);
                                    Utils.setDisabled(slot, true);
                                }
                                break;
                            }
                        }
                    }
                    if (event.title.startsWith("Change all to same color!")) {
                        isInTerminal = true;
                        if (colorsOrder.contains(stack.getItem()) && !colorSlots.contains(slot)) {
                            colorSlots.add(slot);
                        }
                        Utils.setSpoofed(slot, SlotOptions.background);
                        Utils.setDisabled(slot, true);
                    }
                }
            }
            if (!orderSlots.isEmpty()) {
                orderSlots.sort(Comparator.comparingInt(slot -> slot.getStack().getCount()));
                Slot first = orderSlots.getFirst();
                Utils.setSpoofed(first, SlotOptions.first);
                Utils.setDisabled(first, false);
                if (orderSlots.size() > 1) {
                    Slot second = orderSlots.get(1);
                    Utils.setSpoofed(second, SlotOptions.second);
                    Utils.setDisabled(second, true);
                }
            }
            if (!colorSlots.isEmpty() && colorSlots.size() >= 9) {
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
                        Utils.setSpoofed(slot, SlotOptions.background);
                        Utils.setDisabled(slot, true);
                    } else {
                        Utils.setSpoofed(slot, stackWithCount(target));
                        Utils.setDisabled(slot, false);
                    }
                }
            }
        }
    }
}
