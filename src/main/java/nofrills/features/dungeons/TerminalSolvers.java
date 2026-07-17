package nofrills.features.dungeons;

import com.google.common.collect.Maps;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import nofrills.config.*;
import nofrills.events.*;
import nofrills.misc.ConcurrentHashSet;
import nofrills.misc.DungeonUtil;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@EventListener
public class TerminalSolvers {
    public static final Feature instance = new Feature("terminalSolvers");

    public static final SettingBool panes = new SettingBool(false, "panes", instance);
    public static final SettingColor panesColor = new SettingColor(RenderColor.fromArgb(0xff5ca0bf), "panesColor", instance);
    public static final SettingBool startsWith = new SettingBool(false, "startsWith", instance);
    public static final SettingColor startsWithColor = new SettingColor(RenderColor.fromArgb(0xff5ca0bf), "startsWithColor", instance);
    public static final SettingBool select = new SettingBool(false, "select", instance);
    public static final SettingColor selectColor = new SettingColor(RenderColor.fromArgb(0xff5ca0bf), "selectColor", instance);
    public static final SettingBool inOrder = new SettingBool(false, "inOrder", instance);
    public static final SettingBool inOrderDrawNumbers = new SettingBool(false, "inOrderDrawNumbers", instance);
    public static final SettingColor inOrderColorFirst = new SettingColor(RenderColor.fromArgb(0xff5ca0bf), "inOrderColorFirst", instance);
    public static final SettingColor inOrderColorSecond = new SettingColor(RenderColor.fromArgb(0xff45788f), "inOrderColorSecond", instance);
    public static final SettingColor inOrderColorThird = new SettingColor(RenderColor.fromArgb(0xff2e505f), "inOrderColorThird", instance);
    public static final SettingBool colors = new SettingBool(false, "colors", instance);
    public static final SettingColor colorsColorFirst = new SettingColor(RenderColor.fromArgb(0xff5ca0bf), "colorsColorFirst", instance);
    public static final SettingColor colorsColorSecond = new SettingColor(RenderColor.fromArgb(0xff45788f), "colorsColorSecond", instance);
    public static final SettingInt firstClickTicks = new SettingInt(10, "firstClickTicks", instance);
    public static final SettingBool soundOnClick = new SettingBool(false, "soundOnClick", instance);
    public static final SettingString clickSound = new SettingString("minecraft:entity.blaze.hurt", "clickSound", instance);
    public static final SettingDouble clickSoundVolume = new SettingDouble(2.0, "clickSoundVolume", instance);
    public static final SettingDouble clickSoundPitch = new SettingDouble(2.0, "clickSoundPitch", instance);
    public static final SettingColor backgroundColor = new SettingColor(RenderColor.fromFormat(ChatFormatting.DARK_GRAY), "backgroundColor", instance);

    private static final List<Item> colorsOrder = List.of(
            Items.GREEN_STAINED_GLASS_PANE,
            Items.YELLOW_STAINED_GLASS_PANE,
            Items.ORANGE_STAINED_GLASS_PANE,
            Items.RED_STAINED_GLASS_PANE,
            Items.BLUE_STAINED_GLASS_PANE
    );
    private static TerminalSolution currentSolution = null;
    private static int tickCounter = 0;

    public static TerminalType getTerminalType(String title) {
        if (title.startsWith("Correct all the panes!")) return TerminalType.Panes;
        if (title.startsWith("Click in order!")) return TerminalType.InOrder;
        if (title.startsWith("What starts with:") && title.endsWith("?")) return TerminalType.StartsWith;
        if (title.startsWith("Select all the") && title.endsWith("items!")) return TerminalType.Select;
        if (title.startsWith("Change all to same color!")) return TerminalType.Colors;
        if (title.equals("Click the button on time!")) return TerminalType.Melody;
        return TerminalType.None;
    }

    public static TerminalSolution getCurrentSolution() {
        return currentSolution;
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

    @EventHandler
    private static void onSlotUpdate(SlotUpdateEvent event) {
        if (instance.isActive() && !event.isInventory && event.slot != null && DungeonUtil.isOnFloor("7")) {
            TerminalType type = getTerminalType(event.title);
            if (currentSolution == null && !type.equals(TerminalType.None)) {
                currentSolution = new TerminalSolution(type); // create dummy solution even if melody to block first clicks correctly
            }
            if (!isTypeEnabled(type)) {
                return;
            }
            switch (type) {
                case Panes -> {
                    Item item = event.stack.getItem();
                    if (item.equals(Items.RED_STAINED_GLASS_PANE)) {
                        currentSolution.setEnabled(event.slot);
                    } else if (item.equals(Items.LIME_STAINED_GLASS_PANE)) {
                        currentSolution.setDisabled(event.slot);
                    }
                }
                case StartsWith -> {
                    String character = Utils.toLower(String.valueOf(event.title.charAt(event.title.indexOf("'") + 1)));
                    String stackName = Utils.toLower(Utils.toPlain(event.stack.getHoverName())).trim();
                    if (!stackName.isEmpty()) {
                        currentSolution.resetIfMismatch(event.stack, event.slotId);
                        if (stackName.startsWith(character)) {
                            currentSolution.setEnabled(event.slot);
                        } else {
                            currentSolution.setDisabled(event.slot);
                        }
                    }
                }
                case Select -> {
                    String color = event.title.replace("Select all the", "").replace("items!", "").trim();
                    String colorName = color.equals("SILVER") ? "light_gray" : Utils.toLower(color).replace(" ", "_");
                    String stackName = Utils.toLower(Utils.toPlain(event.stack.getHoverName())).trim();
                    if (!stackName.isEmpty()) {
                        for (DyeColor dye : DyeColor.values()) {
                            if (dye.getName().equals(colorName)) {
                                currentSolution.resetIfMismatch(event.stack, event.slotId);
                                if (checkStackColor(event.stack, dye, colorName)) {
                                    currentSolution.setEnabled(event.slot);
                                } else {
                                    currentSolution.setDisabled(event.slot);
                                }
                                break;
                            }
                        }
                    }
                }
                case InOrder -> {
                    Item item = event.stack.getItem();
                    if (item.equals(Items.RED_STAINED_GLASS_PANE)) {
                        currentSolution.setEnabled(event.slot, event.stack.count());
                    } else if (item.equals(Items.LIME_STAINED_GLASS_PANE)) {
                        currentSolution.setDisabled(event.slot);
                    }
                }
                case Colors -> {
                    Item item = event.stack.getItem();
                    int index = colorsOrder.indexOf(item);
                    if (index != -1) {
                        currentSolution.setEnabled(event.slot, index);
                    }
                }
                default -> {
                }
            }
        }
    }

    @EventHandler
    private static void onSlotClick(SlotClickEvent event) {
        if (instance.isActive() && event.slot != null && !event.isInventory && DungeonUtil.isOnFloor("7")) {
            TerminalType type = getTerminalType(event.title);
            if (isTypeEnabled(type) || type.equals(TerminalType.Melody)) {
                if (currentSolution == null || (currentSolution.openedAtTick + firstClickTicks.value() >= tickCounter)) {
                    event.cancel();
                    return;
                }
                switch (type) {
                    case Panes, StartsWith, Select -> {
                        if (currentSolution.isClicked(event.slot.index) || currentSolution.isDisabled(event.slot.index)) {
                            event.cancel();
                        } else {
                            currentSolution.setClicked(event.slot, event.handler.containerId);
                        }
                    }
                    case InOrder -> {
                        Optional<Map.Entry<Integer, Integer>> first = currentSolution.getSolution().stream()
                                .filter(entry -> !currentSolution.isClicked(entry.getKey()) && !currentSolution.isDisabled(entry.getKey()))
                                .findFirst();
                        if (first.isEmpty() || first.get().getKey() != event.slotId) {
                            event.cancel();
                        } else {
                            currentSolution.setClicked(event.slot, event.handler.containerId);
                        }
                    }
                    case Colors -> {
                        List<Map.Entry<Integer, Integer>> solution = currentSolution.getSolution();
                        if (solution.stream().anyMatch(entry -> entry.getKey() == event.slotId && entry.getValue() == 0)) {
                            event.cancel();
                        } else {
                            int index = currentSolution.solutionMap.getOrDefault(event.slot.index, -1);
                            if (index != -1) {
                                int modifier = event.button == GLFW.GLFW_MOUSE_BUTTON_RIGHT ? -1 : 1;
                                int newIndex = index - modifier;
                                if (newIndex < 0) {
                                    currentSolution.solutionMap.put(event.slot.index, 4);
                                } else if (newIndex > 4) {
                                    currentSolution.solutionMap.put(event.slot.index, 0);
                                } else {
                                    currentSolution.solutionMap.put(event.slot.index, newIndex);
                                }
                                if (soundOnClick.value()) {
                                    Utils.playSound(clickSound.value(), clickSoundVolume.valueFloat(), clickSoundPitch.valueFloat());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onScreenRender(ScreenRenderEvent.After event) {
        if (instance.isActive() && DungeonUtil.isOnFloor("7")) {
            TerminalType type = getTerminalType(event.title);
            if (currentSolution == null || !isTypeEnabled(type)) return;
            switch (type) {
                case Panes, StartsWith, Select -> {
                    for (Map.Entry<Integer, Integer> entry : currentSolution.getSolution()) {
                        int slotIndex = entry.getKey();
                        if (!currentSolution.isClicked(slotIndex) && !currentSolution.isDisabled(slotIndex)) {
                            RenderColor color = switch (type) {
                                case Panes -> panesColor.value();
                                case StartsWith -> startsWithColor.value();
                                case Select -> selectColor.value();
                                default -> null;
                            };
                            if (color != null) event.drawFill(slotIndex, color);
                        } else {
                            event.drawFill(slotIndex, backgroundColor.value());
                        }
                    }
                }
                case InOrder -> {
                    int drawnSlots = 0;
                    for (Map.Entry<Integer, Integer> entry : currentSolution.getSolution()) {
                        int slotIndex = entry.getKey();
                        if (drawnSlots == 3 || currentSolution.isClicked(slotIndex) || currentSolution.isDisabled(slotIndex)) {
                            event.drawFill(slotIndex, backgroundColor.value());
                        } else {
                            event.drawFill(slotIndex, switch (drawnSlots) {
                                case 1 -> inOrderColorSecond.value();
                                case 2 -> inOrderColorThird.value();
                                default -> inOrderColorFirst.value();
                            });
                            if (inOrderDrawNumbers.value()) {
                                event.drawLabel(slotIndex, Component.literal(String.valueOf(entry.getValue())));
                            }
                            drawnSlots++;
                        }
                    }
                }
                case Colors -> {
                    for (Map.Entry<Integer, Integer> entry : currentSolution.getSolution()) {
                        int slotIndex = entry.getKey();
                        int target = entry.getValue();
                        if (target == 0) {
                            event.drawFill(slotIndex, backgroundColor.value());
                        } else {
                            event.drawFill(slotIndex, target > 0 ? colorsColorFirst.value() : colorsColorSecond.value());
                            event.drawLabel(slotIndex, Component.literal(String.valueOf(target)));
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
    private static void onServerTick(ServerTickEvent event) {
        if (instance.isActive() && DungeonUtil.isOnFloor("7")) {
            tickCounter++;
        }
    }

    @EventHandler
    private static void onScreenClose(ScreenCloseEvent event) {
        currentSolution = null;
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        currentSolution = null;
        tickCounter = 0;
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

    public static class TerminalSolution {
        public final ConcurrentHashMap<Integer, Integer> solutionMap = new ConcurrentHashMap<>();
        public final ConcurrentHashSet<Integer> clickedSet = new ConcurrentHashSet<>();
        public final ConcurrentHashMap<Integer, Item> contents = new ConcurrentHashMap<>();
        public final TerminalType type;
        public int openedAtTick;
        public int containerId = -1;

        public TerminalSolution(TerminalType type) {
            this.type = type;
            this.openedAtTick = tickCounter;
        }

        public void setEnabled(Slot slot, int count) {
            this.solutionMap.put(slot.index, count);
        }

        public void setEnabled(Slot slot) {
            this.setEnabled(slot, 1);
        }

        public void setDisabled(Slot slot) {
            this.solutionMap.put(slot.index, -1);
        }

        public void setClicked(Slot slot, int containerId) {
            if (this.containerId != containerId) {
                this.clickedSet.add(slot.index);
                this.containerId = containerId;
                if (soundOnClick.value()) {
                    Utils.playSound(clickSound.value(), clickSoundVolume.valueFloat(), clickSoundPitch.valueFloat());
                }
            }
        }

        public boolean isDisabled(int slotIndex) {
            return this.solutionMap.getOrDefault(slotIndex, -1) == -1;
        }

        public boolean isClicked(int slotIndex) {
            return this.clickedSet.contains(slotIndex);
        }

        public void resetIfMismatch(ItemStack stack, int slotId) {
            if (this.contents.containsKey(slotId) && !this.contents.get(slotId).equals(stack.getItem())) {
                this.solutionMap.clear();
                this.clickedSet.clear();
                this.contents.clear();
                this.openedAtTick = tickCounter;
                this.containerId = -1;
            }
            this.contents.put(slotId, stack.getItem());
        }

        public List<Map.Entry<Integer, Integer>> getSolution() {
            return switch (this.type) {
                case InOrder -> this.solutionMap.entrySet().stream()
                        .sorted(Comparator.comparingInt(Map.Entry::getValue))
                        .toList();
                case Colors -> {
                    int mostCommon = this.solutionMap.values()
                            .stream().mapToInt(i -> i)
                            .boxed()
                            .collect(Collectors.groupingBy(
                                    i -> i,
                                    Collectors.counting()
                            ))
                            .entrySet().stream()
                            .max((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
                            .map(Map.Entry::getKey)
                            .orElse(-1);
                    if (mostCommon == -1) yield List.of();
                    yield this.solutionMap.entrySet().stream()
                            .map(entry -> {
                                int slotIndex = entry.getKey();
                                int target = Math.negateExact(mostCommon - entry.getValue());
                                if (Math.abs(target) > 2) {
                                    int offset = Math.abs(target) == 4 ? 3 : 1;
                                    return Maps.immutableEntry(slotIndex, Math.negateExact(target) + (target > 0 ? offset : -offset));
                                }
                                return Maps.immutableEntry(slotIndex, target);
                            })
                            .toList();
                }
                default -> this.solutionMap.entrySet().stream().toList();
            };
        }
    }
}