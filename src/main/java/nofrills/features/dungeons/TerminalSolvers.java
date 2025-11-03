package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.ScreenRenderEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.SlotClickEvent;
import nofrills.events.SlotUpdateEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.SlotOptions;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

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

    private static final List<Item> colorsOrder = List.of(
            Items.GREEN_STAINED_GLASS_PANE,
            Items.YELLOW_STAINED_GLASS_PANE,
            Items.ORANGE_STAINED_GLASS_PANE,
            Items.RED_STAINED_GLASS_PANE,
            Items.BLUE_STAINED_GLASS_PANE
    );
    private static final List<ItemStack> optionStacks = List.of(SlotOptions.FIRST, SlotOptions.SECOND, SlotOptions.THIRD);
    private static final RenderColor colorFirst = RenderColor.fromArgb(0xff5ca0bf);
    private static final RenderColor colorSecond = new RenderColor(colorFirst.r * 0.75f, colorFirst.g * 0.75f, colorFirst.b * 0.75f, colorFirst.a);
    private static final RenderColor colorThird = new RenderColor(colorFirst.r * 0.5f, colorFirst.g * 0.5f, colorFirst.b * 0.5f, colorFirst.a);
    private static int lastSyncId = -1;

    public static TerminalType getTerminalType(String title) {
        if (title.startsWith("Correct all the panes!")) return TerminalType.Panes;
        if (title.startsWith("Click in order!")) return TerminalType.InOrder;
        if (title.startsWith("What starts with:") && title.endsWith("?")) return TerminalType.StartsWith;
        if (title.startsWith("Select all the") && title.endsWith("items!")) return TerminalType.Select;
        if (title.startsWith("Change all to same color!")) return TerminalType.Colors;
        if (title.equals("Click the button on time!")) return TerminalType.Melody;
        return TerminalType.None;
    }

    public static boolean isTypeEnabled(TerminalType type) {
        return switch (type) {
            case Panes -> panes.value();
            case InOrder -> inOrder.value();
            case StartsWith -> startsWith.value();
            case Select -> select.value();
            case Colors -> colors.value();
            case None, Melody -> false;
        };
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

    private static void solveSlot(Slot slot, boolean enabled) {
        solveSlot(slot, enabled ? SlotOptions.FIRST : SlotOptions.BACKGROUND, enabled);
    }

    private static void solveSlot(Slot slot, ItemStack stack, boolean enabled) {
        SlotOptions.setSpoofed(slot, stack);
        SlotOptions.setDisabled(slot, !enabled);
        SlotOptions.setCount(slot, "");
    }

    @EventHandler
    private static void onSlotUpdate(SlotUpdateEvent event) {
        if (instance.isActive() && Utils.isOnDungeonFloor("7") && !event.isInventory && event.slot != null) {
            TerminalType type = getTerminalType(event.title);
            if (!isTypeEnabled(type)) return;
            if (type.equals(TerminalType.Panes)) {
                solveSlot(event.slot, event.stack.getItem().equals(Items.RED_STAINED_GLASS_PANE));
            }
            if (type.equals(TerminalType.StartsWith)) {
                String character = Utils.toLower(String.valueOf(event.title.charAt(event.title.indexOf("'") + 1)));
                String name = Utils.toLower(Utils.toPlain(event.stack.getName())).trim();
                solveSlot(event.slot, !name.isEmpty() && name.startsWith(character) && !Utils.hasGlint(event.stack));
            }
            if (type.equals(TerminalType.Select)) {
                String color = event.title.replace("Select all the", "").replace("items!", "").trim();
                String colorName = color.equals("SILVER") ? "light_gray" : Utils.toLower(color).replace(" ", "_");
                for (DyeColor dye : DyeColor.values()) {
                    if (dye.getId().equals(colorName)) {
                        solveSlot(event.slot, !Utils.hasGlint(event.stack) && checkStackColor(event.stack, dye, colorName));
                        break;
                    }
                }
            }
            if (type.equals(TerminalType.InOrder)) {
                List<Slot> orderSlots = new ArrayList<>();
                for (Slot slot : Utils.getContainerSlots(event.handler)) {
                    Item item = slot.getStack().getItem();
                    SlotOptions.setDisabled(event.slot, true);
                    if (item.equals(Items.RED_STAINED_GLASS_PANE) || item.equals(Items.LIME_STAINED_GLASS_PANE)) {
                        orderSlots.add(slot);
                    } else {
                        solveSlot(event.slot, false);
                    }
                }
                if (orderSlots.size() == 14) { // scuffed way to ensure every slot is sent in by the server
                    orderSlots.removeIf(slot -> slot.getStack().getItem().equals(Items.LIME_STAINED_GLASS_PANE));
                    if (orderSlots.isEmpty()) {
                        return;
                    }
                    orderSlots.sort(Comparator.comparingInt(slot -> slot.getStack().getCount()));
                    for (int i = 0; i < optionStacks.size(); i++) {
                        if (orderSlots.size() > i) {
                            Slot slot = orderSlots.get(i);
                            solveSlot(slot, SlotOptions.stackWithCount(optionStacks.get(i), slot.getStack().getCount()), i == 0);
                        }
                    }
                }
            }
            if (type.equals(TerminalType.Colors)) {
                List<Slot> colorSlots = new ArrayList<>();
                for (Slot slot : Utils.getContainerSlots(event.handler)) {
                    solveSlot(event.slot, false);
                    if (colorsOrder.contains(slot.getStack().getItem()) && !colorSlots.contains(slot)) {
                        colorSlots.add(slot);
                    }
                }
                if (colorSlots.size() == 9) {
                    int[] colorCounts = {0, 0, 0, 0, 0};
                    for (Slot slot : colorSlots) {
                        colorCounts[colorsOrder.indexOf(slot.getStack().getItem())] += 1;
                    }
                    int mostCommon = -1, highestCommon = 0;
                    for (int i = 0; i < 5; i++) {
                        if (colorCounts[i] > highestCommon) {
                            highestCommon = colorCounts[i];
                            mostCommon = i;
                        }
                    }
                    for (Slot slot : colorSlots) {
                        int target = Math.negateExact(mostCommon - colorsOrder.indexOf(slot.getStack().getItem()));
                        if (Math.abs(target) > 2) {
                            int offset = Math.abs(target) == 4 ? 3 : 1;
                            target = Math.negateExact(target) + (target > 0 ? offset : -offset);
                        }
                        if (target == 0) {
                            solveSlot(slot, false);
                        } else {
                            solveSlot(slot, SlotOptions.stackWithCount(target > 0 ? SlotOptions.FIRST : SlotOptions.SECOND, Math.abs(target)), true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onSlotClick(SlotClickEvent event) {
        if (instance.isActive() && Utils.isOnDungeonFloor("7") && event.slot != null && event.handler.syncId != lastSyncId) {
            TerminalType type = getTerminalType(event.title);
            if (!isTypeEnabled(type)) return;
            lastSyncId = event.handler.syncId;
            switch (type) {
                case Panes, StartsWith, Select -> solveSlot(event.slot, false);
                case InOrder -> {
                    ItemStack spoofed = SlotOptions.getSpoofed(event.slot);
                    int count = spoofed.getCount();
                    for (Slot slot : Utils.getContainerSlots((GenericContainerScreenHandler) event.handler)) {
                        ItemStack slotStack = slot.getStack();
                        int slotCount = slotStack.getCount();
                        if (slotStack.getItem().equals(Items.RED_STAINED_GLASS_PANE) && slotCount > count) {
                            for (int i = 0; i < optionStacks.size(); i++) {
                                if (slotCount == (count + i + 1)) {
                                    solveSlot(slot, SlotOptions.stackWithCount(optionStacks.get(i), slotCount), i == 0);
                                }
                            }
                        }
                    }
                    solveSlot(event.slot, false);
                }
                case Colors -> {
                    ItemStack spoofed = SlotOptions.getSpoofed(event.slot);
                    Item item = spoofed.getItem();
                    int count = spoofed.getCount();
                    boolean isFirst = item.equals(SlotOptions.FIRST.getItem());
                    boolean isSecond = item.equals(SlotOptions.SECOND.getItem());
                    boolean isRight = event.button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
                    if ((isFirst && isRight) || (isSecond && !isRight)) { // scuffed handling for the wrong button
                        if (count == 2) {
                            solveSlot(event.slot, SlotOptions.stackWithCount(isFirst ? SlotOptions.SECOND : SlotOptions.FIRST, 2), true);
                        } else {
                            solveSlot(event.slot, spoofed.copyWithCount(count + 1), true);
                        }
                    } else {
                        if (count == 1) {
                            solveSlot(event.slot, false);
                        } else {
                            solveSlot(event.slot, spoofed.copyWithCount(count - 1), true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onScreenRender(ScreenRenderEvent.After event) {
        if (instance.isActive() && Utils.isOnDungeonFloor("7")) {
            TerminalType type = getTerminalType(event.title);
            if (!isTypeEnabled(type)) return;
            switch (type) {
                case Panes, StartsWith, Select -> {
                    for (Slot slot : Utils.getContainerSlots((GenericContainerScreenHandler) event.handler)) {
                        if (SlotOptions.isSpoofed(slot) && !SlotOptions.isDisabled(slot)) {
                            event.drawFill(slot.id, colorFirst);
                        }
                    }
                }
                case InOrder -> {
                    for (Slot slot : Utils.getContainerSlots((GenericContainerScreenHandler) event.handler)) {
                        if (SlotOptions.isSpoofed(slot)) {
                            ItemStack spoofed = SlotOptions.getSpoofed(slot);
                            if (spoofed.getItem().equals(SlotOptions.BACKGROUND.getItem())) continue;
                            Item item = spoofed.getItem();
                            for (int i = 0; i < optionStacks.size(); i++) {
                                if (item.equals(optionStacks.get(i).getItem())) {
                                    event.drawFill(slot.id, switch (i) {
                                        case 0 -> colorFirst;
                                        case 1 -> colorSecond;
                                        default -> colorThird;
                                    });
                                }
                            }
                            event.drawLabel(slot.id, Text.literal(String.valueOf(spoofed.getCount())));
                        }
                    }
                }
                case Colors -> {
                    for (Slot slot : Utils.getContainerSlots((GenericContainerScreenHandler) event.handler)) {
                        if (SlotOptions.isSpoofed(slot) && !SlotOptions.isDisabled(slot)) {
                            ItemStack spoofed = SlotOptions.getSpoofed(slot);
                            String count = String.valueOf(spoofed.getCount());
                            if (spoofed.getItem().equals(SlotOptions.FIRST.getItem())) {
                                event.drawFill(slot.id, colorFirst);
                                event.drawLabel(slot.id, Text.literal(count));
                            } else {
                                event.drawFill(slot.id, colorSecond);
                                event.drawLabel(slot.id, Text.literal("-" + count));
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        if (instance.isActive()) {
            lastSyncId = -1;
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