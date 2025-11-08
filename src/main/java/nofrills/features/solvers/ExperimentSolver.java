package nofrills.features.solvers;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.ScreenOpenEvent;
import nofrills.events.SendPacketEvent;
import nofrills.events.SlotUpdateEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.SlotOptions;
import nofrills.misc.Utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static nofrills.Main.mc;

public class ExperimentSolver {
    public static final Feature instance = new Feature("experimentSolver");

    public static final SettingBool chronomatron = new SettingBool(false, "chronomatron", instance.key());
    public static final SettingBool ultrasequencer = new SettingBool(false, "ultrasequencer", instance.key());
    public static final SettingBool superpairs = new SettingBool(false, "superpairs", instance.key());

    private static final List<Solution> chronoSolution = new ArrayList<>();
    private static final List<Solution> ultraSolution = new ArrayList<>();
    private static final RenderColor superColorFound = RenderColor.fromHex(0x00ff00, 0.5f);
    private static final RenderColor superColorPotential = RenderColor.fromHex(0xffff00, 0.5f);
    private static Solution superSolution = new Solution();
    private static boolean rememberPhase = true;

    private static void updatePhase(ItemStack stack) {
        Item item = stack.getItem();
        if (!rememberPhase && item.equals(Items.GLOWSTONE)) {
            rememberPhase = true;
        }
        if (rememberPhase && item.equals(Items.CLOCK)) {
            rememberPhase = false;
        }
    }

    public static ExperimentType getExperimentType() {
        if (Utils.isOnPrivateIsland() && mc.currentScreen instanceof GenericContainerScreen container) {
            String title = container.getTitle().getString();
            if (title.startsWith("Chronomatron (")) return ExperimentType.Chronomatron;
            if (title.startsWith("Ultrasequencer (")) return ExperimentType.Ultrasequencer;
            if (title.startsWith("Superpairs (")) return ExperimentType.Superpairs;
        }
        return ExperimentType.None;
    }

    private static boolean isStatus(ItemStack stack) {
        Item item = stack.getItem();
        String name = Utils.toPlain(stack.getName());
        return item.equals(Items.CLOCK)
                || item.equals(Items.BOOKSHELF)
                || (item.equals(Items.GLOWSTONE) && !name.equals("Enchanted Book"))
                || item.equals(Items.CAULDRON);
    }

    private static boolean isDye(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof DyeItem
                || item.equals(Items.INK_SAC)
                || item.equals(Items.BONE_MEAL)
                || item.equals(Items.LAPIS_LAZULI)
                || item.equals(Items.COCOA_BEANS);
    }

    private static boolean isPowerup(ItemStack stack) {
        for (String line : Utils.getLoreLines(stack)) {
            if (Utils.toLower(line).contains("powerup")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isTerracotta(ItemStack stack) {
        return stack.getItem().toString().endsWith("terracotta");
    }

    private static boolean isStainedGlass(ItemStack stack) {
        return stack.getItem().toString().endsWith("stained_glass");
    }

    private static boolean isStainedGlassPane(ItemStack stack) {
        return stack.getItem().toString().endsWith("stained_glass_pane");
    }

    private static List<Slot> getContainerSlots(GenericContainerScreenHandler handler) {
        Inventory inventory = handler.getInventory();
        List<Slot> slots = new ArrayList<>(handler.slots);
        slots.removeIf(slot -> inventory.getStack(slot.id).isEmpty());
        return slots;
    }

    private static void showChronoSolution() {
        if (!chronoSolution.isEmpty()) {
            for (Slot solution : chronoSolution.getFirst().slots) {
                SlotOptions.setSpoofed(solution, SlotOptions.FIRST);
                SlotOptions.setDisabled(solution, false);
            }
        }
        if (chronoSolution.size() > 1) {
            Solution first = chronoSolution.getFirst();
            for (Slot solution : chronoSolution.get(1).slots) {
                if (first.slots.stream().noneMatch(slot -> slot.id == solution.id)) {
                    SlotOptions.setSpoofed(solution, SlotOptions.SECOND);
                    SlotOptions.setDisabled(solution, true);
                }
            }
        }
    }

    private static void showUltraSolution() {
        if (!ultraSolution.isEmpty()) {
            Solution first = ultraSolution.getFirst();
            SlotOptions.setSpoofed(first.slot, SlotOptions.stackWithCount(SlotOptions.FIRST, first.stack.getCount()));
            SlotOptions.setDisabled(first.slot, false);
        }
        if (ultraSolution.size() > 1) {
            Solution second = ultraSolution.get(1);
            SlotOptions.setSpoofed(second.slot, SlotOptions.stackWithCount(SlotOptions.SECOND, second.stack.getCount()));
            SlotOptions.setDisabled(second.slot, true);
        }
    }

    private static boolean matchSuperStacks(ItemStack first, ItemStack second) {
        return first.getItem().equals(second.getItem())
                && first.getName().getString().equals(second.getName().getString())
                && first.getCount() == second.getCount()
                && Objects.equals(Utils.getTextureUrl(first), Utils.getTextureUrl(second));
    }

    @EventHandler
    private static void onSlotUpdate(SlotUpdateEvent event) {
        if (!instance.isActive() || event.isInventory) {
            return;
        }
        ExperimentType experimentType = getExperimentType();
        if (experimentType.equals(ExperimentType.None)) {
            return;
        }
        Item item = event.stack.getItem();
        Slot eventSlot = event.handler.getSlot(event.slotId);
        updatePhase(event.stack);
        if (chronomatron.value() && experimentType.equals(ExperimentType.Chronomatron)) {
            if (rememberPhase) {
                for (Slot slot : getContainerSlots(event.handler)) {
                    SlotOptions.setDisabled(slot, true);
                }
                if (isTerracotta(event.stack)) {
                    if (chronoSolution.isEmpty()) {
                        chronoSolution.add(new Solution(new ArrayList<>()));
                    }
                    chronoSolution.getLast().slots.add(eventSlot);
                } else if (isStainedGlass(event.stack)) {
                    if (!chronoSolution.isEmpty() && chronoSolution.getLast().slots.stream().anyMatch(slot -> slot.id == event.slotId)) {
                        chronoSolution.add(new Solution(new ArrayList<>()));
                    }
                }
            } else {
                if (isStatus(event.stack)) {
                    return;
                }
                showChronoSolution();
            }
        }
        if (ultrasequencer.value() && experimentType.equals(ExperimentType.Ultrasequencer)) {
            if (item.equals(Items.CLOCK)) {
                ultraSolution.sort(Comparator.comparingInt(s -> s.stack.getCount()));
                showUltraSolution();
            } else if (item.equals(Items.GLOWSTONE)) {
                List<Solution> solution = new ArrayList<>();
                SlotOptions.clearSpoofed();
                SlotOptions.clearDisabled();
                for (Slot slot : getContainerSlots(event.handler)) {
                    SlotOptions.setDisabled(slot, true);
                    if (isDye(slot.getStack())) {
                        solution.add(new Solution(slot.getStack(), slot));
                    }
                }
                ultraSolution.clear();
                ultraSolution.addAll(solution);
            }
        }
        if (superpairs.value() && experimentType.equals(ExperimentType.Superpairs) && !isStatus(event.stack) && !isPowerup(event.stack)) {
            if (!isStainedGlass(event.stack) && !isStainedGlassPane(event.stack) && !item.equals(Items.AIR)) {
                if (superSolution.slot != null && superSolution.slot != eventSlot) {
                    if (matchSuperStacks(event.stack, superSolution.slot.getStack())) {
                        SlotOptions.setBackground(eventSlot, superColorFound);
                        SlotOptions.setBackground(superSolution.slot, superColorFound);
                    }
                }
                for (Map.Entry<Slot, ItemStack> solution : superSolution.rewards.entrySet()) {
                    if (!SlotOptions.hasBackground(eventSlot) && !eventSlot.equals(solution.getKey()) && matchSuperStacks(event.stack, solution.getValue())) {
                        SlotOptions.setBackground(eventSlot, superColorPotential);
                        SlotOptions.setBackground(solution.getKey(), superColorPotential);
                    }
                }
                superSolution.rewards.put(eventSlot, event.stack);
                superSolution.slot = eventSlot;
                SlotOptions.setSpoofed(eventSlot, event.stack);
            }
        }
    }

    @EventHandler
    private static void onScreen(ScreenOpenEvent event) {
        if (instance.isActive()) {
            rememberPhase = true;
            chronoSolution.clear();
            ultraSolution.clear();
            superSolution = new Solution();
        }
    }

    @EventHandler
    private static void onSendPacket(SendPacketEvent event) {
        if (instance.isActive() && event.packet instanceof ClickSlotC2SPacket clickPacket) {
            ExperimentType type = getExperimentType();
            int slotId = clickPacket.slot();
            if (chronomatron.value() && type.equals(ExperimentType.Chronomatron) && !rememberPhase) {
                if (!chronoSolution.isEmpty()) {
                    Solution first = chronoSolution.getFirst();
                    if (first.slots.stream().anyMatch(slot -> slot.id == slotId)) {
                        for (Slot slot : first.slots) {
                            SlotOptions.clearSpoofed(slot);
                            SlotOptions.setDisabled(slot, true);
                        }
                        chronoSolution.removeFirst();
                    }
                }
                showChronoSolution();
            }
            if (ultrasequencer.value() && type.equals(ExperimentType.Ultrasequencer) && !rememberPhase) {
                if (!ultraSolution.isEmpty()) {
                    if (ultraSolution.getFirst().slot.id == slotId) {
                        Solution first = ultraSolution.getFirst();
                        SlotOptions.setSpoofed(first.slot, SlotOptions.stackWithCount(Items.GRAY_CONCRETE.getDefaultStack(), first.stack.getCount()));
                        SlotOptions.setDisabled(first.slot, true);
                        ultraSolution.removeFirst();
                    }
                }
                showUltraSolution();
            }
        }
    }

    public enum ExperimentType {
        Chronomatron,
        Ultrasequencer,
        Superpairs,
        None
    }

    private static class Solution {
        public ExperimentType type;
        public ItemStack stack;
        public Slot slot;
        public List<Slot> slots;
        public ConcurrentHashMap<Slot, ItemStack> rewards;

        public Solution(ItemStack stack, Slot slot) {
            this.type = ExperimentType.Ultrasequencer;
            this.stack = stack;
            this.slot = slot;
        }

        public Solution(List<Slot> slots) {
            this.type = ExperimentType.Chronomatron;
            this.slots = slots;
        }

        public Solution() {
            this.type = ExperimentType.Ultrasequencer;
            this.rewards = new ConcurrentHashMap<>();
            this.slot = null;
        }
    }
}
