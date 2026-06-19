package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.events.*;
import nofrills.misc.RenderColor;
import nofrills.misc.SlotOptions;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TerminalSolvers {
    public static final Feature instance = new Feature("terminalSolvers");

    public static final SettingBool panes = new SettingBool(false, "panes", instance);
    public static final SettingColor panesColor = new SettingColor(RenderColor.fromArgb(0xff5ca0bf), "panesColor", instance);
    public static final SettingBool startsWith = new SettingBool(false, "startsWith", instance);
    public static final SettingColor startsWithColor = new SettingColor(RenderColor.fromArgb(0xff5ca0bf), "startsWithColor", instance);
    public static final SettingBool select = new SettingBool(false, "select", instance);
    public static final SettingColor selectColor = new SettingColor(RenderColor.fromArgb(0xff5ca0bf), "selectColor", instance);
    public static final SettingBool inOrder = new SettingBool(false, "inOrder", instance);
    public static final SettingColor inOrderColorFirst = new SettingColor(RenderColor.fromArgb(0xff5ca0bf), "inOrderColorFirst", instance);
    public static final SettingColor inOrderColorSecond = new SettingColor(RenderColor.fromArgb(0xff45788f), "inOrderColorSecond", instance);
    public static final SettingColor inOrderColorThird = new SettingColor(RenderColor.fromArgb(0xff2e505f), "inOrderColorThird", instance);
    public static final SettingBool colors = new SettingBool(false, "colors", instance);
    public static final SettingColor colorsColorFirst = new SettingColor(RenderColor.fromArgb(0xff5ca0bf), "colorsColorFirst", instance);
    public static final SettingColor colorsColorSecond = new SettingColor(RenderColor.fromArgb(0xff45788f), "colorsColorSecond", instance);

    private static final List<Item> colorsOrder = List.of(
            Items.GREEN_STAINED_GLASS_PANE,
            Items.YELLOW_STAINED_GLASS_PANE,
            Items.ORANGE_STAINED_GLASS_PANE,
            Items.RED_STAINED_GLASS_PANE,
            Items.BLUE_STAINED_GLASS_PANE
    );
    private static final List<ItemStack> optionStacks = List.of(
            SlotOptions.FIRST,
            SlotOptions.SECOND,
            SlotOptions.THIRD
    );
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

    private static boolean checkStackColor(ItemStack stack, DyeColor color, String colorName) {
        Item item = stack.getItem();
        if (Utils.toPlain(stack.getHoverName()).trim().isEmpty()) {
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
        if (instance.isActive() && !event.isInventory && event.slot != null && Utils.isOnDungeonFloor("7")) {
            TerminalType type = getTerminalType(event.title);
            if (!isTypeEnabled(type)) return;
            if (type.equals(TerminalType.Panes)) {
                solveSlot(event.slot, event.stack.getItem().equals(Items.RED_STAINED_GLASS_PANE));
            }
            if (type.equals(TerminalType.StartsWith)) {
                String character = Utils.toLower(String.valueOf(event.title.charAt(event.title.indexOf("'") + 1)));
                String name = Utils.toLower(Utils.toPlain(event.stack.getHoverName())).trim();
                solveSlot(event.slot, !name.isEmpty() && name.startsWith(character) && !Utils.hasGlint(event.stack));
            }
            if (type.equals(TerminalType.Select)) {
                String color = event.title.replace("Select all the", "").replace("items!", "").trim();
                String colorName = color.equals("SILVER") ? "light_gray" : Utils.toLower(color).replace(" ", "_");
                for (DyeColor dye : DyeColor.values()) {
                    if (dye.getName().equals(colorName)) {
                        solveSlot(event.slot, !Utils.hasGlint(event.stack) && checkStackColor(event.stack, dye, colorName));
                        break;
                    }
                }
            }
            if (type.equals(TerminalType.InOrder)) {
                List<Slot> orderSlots = new ArrayList<>();
                for (Slot slot : Utils.getContainerSlots(event.handler)) {
                    Item item = slot.getItem().getItem();
                    SlotOptions.setDisabled(event.slot, true);
                    if (item.equals(Items.RED_STAINED_GLASS_PANE) || item.equals(Items.LIME_STAINED_GLASS_PANE)) {
                        orderSlots.add(slot);
                    } else {
                        solveSlot(event.slot, false);
                    }
                }
                if (orderSlots.size() == 14) { // scuffed way to ensure every slot is sent in by the server
                    orderSlots.removeIf(slot -> slot.getItem().getItem().equals(Items.LIME_STAINED_GLASS_PANE));
                    if (orderSlots.isEmpty()) {
                        return;
                    }
                    orderSlots.sort(Comparator.comparingInt(slot -> slot.getItem().getCount()));
                    for (int i = 0; i < optionStacks.size(); i++) {
                        if (orderSlots.size() > i) {
                            Slot slot = orderSlots.get(i);
                            solveSlot(slot, SlotOptions.stackWithCount(optionStacks.get(i), slot.getItem().getCount()), i == 0);
                        }
                    }
                }
            }
            if (type.equals(TerminalType.Colors)) {
                List<Slot> colorSlots = new ArrayList<>();
                for (Slot slot : Utils.getContainerSlots(event.handler)) {
                    solveSlot(event.slot, false);
                    if (colorsOrder.contains(slot.getItem().getItem()) && !colorSlots.contains(slot)) {
                        colorSlots.add(slot);
                    }
                }
                if (colorSlots.size() == 9) {
                    int[] colorCounts = {0, 0, 0, 0, 0};
                    for (Slot slot : colorSlots) {
                        colorCounts[colorsOrder.indexOf(slot.getItem().getItem())] += 1;
                    }
                    int mostCommon = -1, highestCommon = 0;
                    for (int i = 0; i < 5; i++) {
                        if (colorCounts[i] > highestCommon) {
                            highestCommon = colorCounts[i];
                            mostCommon = i;
                        }
                    }
                    for (Slot slot : colorSlots) {
                        int target = Math.negateExact(mostCommon - colorsOrder.indexOf(slot.getItem().getItem()));
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
        if (instance.isActive() && Utils.isOnDungeonFloor("7") && event.slot != null && event.handler.containerId != lastSyncId) {
            TerminalType type = getTerminalType(event.title);
            if (!isTypeEnabled(type)) return;
            lastSyncId = event.handler.containerId;
            switch (type) {
                case Panes, StartsWith, Select -> solveSlot(event.slot, false);
                case InOrder -> {
                    ItemStack spoofed = SlotOptions.getSpoofed(event.slot);
                    int count = spoofed.getCount();
                    for (Slot slot : Utils.getContainerSlots(event.handler)) {
                        ItemStack slotStack = slot.getItem();
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
                    for (Slot slot : Utils.getContainerSlots(event.handler)) {
                        if (SlotOptions.isSpoofed(slot) && !SlotOptions.isDisabled(slot)) {
                            RenderColor color = switch (type) {
                                case Panes -> panesColor.value();
                                case StartsWith -> startsWithColor.value();
                                case Select -> selectColor.value();
                                default -> RenderColor.white;
                            };
                            event.drawFill(slot.index, color);
                        }
                    }
                }
                case InOrder -> {
                    for (Slot slot : Utils.getContainerSlots(event.handler)) {
                        if (SlotOptions.isSpoofed(slot)) {
                            ItemStack spoofed = SlotOptions.getSpoofed(slot);
                            if (spoofed.getItem().equals(SlotOptions.BACKGROUND.getItem())) continue;
                            Item item = spoofed.getItem();
                            for (int i = 0; i < optionStacks.size(); i++) {
                                if (item.equals(optionStacks.get(i).getItem())) {
                                    event.drawFill(slot.index, switch (i) {
                                        case 0 -> inOrderColorFirst.value();
                                        case 1 -> inOrderColorSecond.value();
                                        default -> inOrderColorThird.value();
                                    });
                                }
                            }
                            event.drawLabel(slot.index, Component.literal(String.valueOf(spoofed.getCount())));
                        }
                    }
                }
                case Colors -> {
                    for (Slot slot : Utils.getContainerSlots(event.handler)) {
                        if (SlotOptions.isSpoofed(slot) && !SlotOptions.isDisabled(slot)) {
                            ItemStack spoofed = SlotOptions.getSpoofed(slot);
                            String count = String.valueOf(spoofed.getCount());
                            if (spoofed.getItem().equals(SlotOptions.FIRST.getItem())) {
                                event.drawFill(slot.index, colorsColorFirst.value());
                                event.drawLabel(slot.index, Component.literal(count));
                            } else {
                                event.drawFill(slot.index, colorsColorSecond.value());
                                event.drawLabel(slot.index, Component.literal("-" + count));
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onBeforeTooltip(TooltipRenderEvent.Before event) {
        if (instance.isActive() && !getTerminalType(event.title).equals(TerminalType.None)) {
            event.cancel();
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        lastSyncId = -1;
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