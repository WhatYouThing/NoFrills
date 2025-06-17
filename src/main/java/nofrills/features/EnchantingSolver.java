package nofrills.features;

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
import nofrills.config.Config;
import nofrills.events.ScreenOpenEvent;
import nofrills.events.ScreenSlotUpdateEvent;
import nofrills.events.SendPacketEvent;
import nofrills.misc.SlotOptions;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static nofrills.Main.mc;

public class EnchantingSolver {
    private static final List<Solution> chronoSolution = new ArrayList<>();
    private static final List<Solution> ultraSolution = new ArrayList<>();
    private static final List<Solution> superSolution = new ArrayList<>();
    private static boolean rememberPhase = true;

    private static void updatePhase(Item item) {
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
            if (title.startsWith("Chronomatron (")) {
                return ExperimentType.Chronomatron;
            }
            if (title.startsWith("Ultrasequencer (")) {
                return ExperimentType.Ultrasequencer;
            }
            if (title.startsWith("Superpairs (")) {
                return ExperimentType.Superpairs;
            }
        }
        return ExperimentType.None;
    }

    private static boolean isStatus(Item item) {
        return item.equals(Items.CLOCK) || item.equals(Items.BOOKSHELF) || item.equals(Items.GLOWSTONE);
    }

    private static boolean isDye(Item item) {
        return item instanceof DyeItem
                || item.equals(Items.INK_SAC)
                || item.equals(Items.BONE_MEAL)
                || item.equals(Items.LAPIS_LAZULI)
                || item.equals(Items.COCOA_BEANS);
    }

    private static boolean isTerracotta(Item item) {
        return item.toString().endsWith("terracotta");
    }

    private static boolean isStainedGlass(Item item) {
        return item.toString().endsWith("stained_glass");
    }

    private static List<Slot> getContainerSlots(GenericContainerScreenHandler handler) {
        Inventory inventory = handler.getInventory();
        List<Slot> slots = new ArrayList<>(handler.slots);
        slots.removeIf(slot -> inventory.getStack(slot.id).isEmpty());
        return slots;
    }

    @EventHandler
    private static void onSlotUpdate(ScreenSlotUpdateEvent event) {
        if (event.inventory.getStack(event.slotId).isEmpty()) {
            return;
        }
        ExperimentType experimentType = getExperimentType();
        if (experimentType.equals(ExperimentType.None)) {
            return;
        }
        Item item = event.stack.getItem();
        Slot eventSlot = event.handler.getSlot(event.slotId);
        updatePhase(item);
        if (Config.solveChronomatron && experimentType.equals(ExperimentType.Chronomatron)) {
            if (rememberPhase) {
                for (Slot slot : getContainerSlots(event.handler)) {
                    SlotOptions.disableSlot(slot, true);
                }
                if (isTerracotta(item)) {
                    if (chronoSolution.isEmpty()) {
                        chronoSolution.add(new Solution(new ArrayList<>()));
                    }
                    chronoSolution.getLast().slots.add(eventSlot);
                } else if (isStainedGlass(item)) {
                    if (!chronoSolution.isEmpty() && chronoSolution.getLast().slots.stream().anyMatch(slot -> slot.id == event.slotId)) {
                        chronoSolution.add(new Solution(new ArrayList<>()));
                    }
                }
            } else {
                if (isStatus(item)) {
                    return;
                }
                if (!chronoSolution.isEmpty()) {
                    for (Slot solution : chronoSolution.getFirst().slots) {
                        SlotOptions.spoofSlot(solution, SlotOptions.first);
                        SlotOptions.disableSlot(solution, false);
                    }
                }
                if (chronoSolution.size() > 1) {
                    Solution first = chronoSolution.getFirst();
                    for (Slot solution : chronoSolution.get(1).slots) {
                        if (first.slots.stream().noneMatch(slot -> slot.id == solution.id)) {
                            SlotOptions.spoofSlot(solution, SlotOptions.second);
                            SlotOptions.disableSlot(solution, true);
                        }
                    }
                }
            }
        }
        if (Config.solveUltrasequencer && experimentType.equals(ExperimentType.Ultrasequencer)) {
            if (item.equals(Items.CLOCK)) {
                ultraSolution.sort(Comparator.comparingInt(s -> s.stack.getCount()));
                if (!ultraSolution.isEmpty()) {
                    Solution first = ultraSolution.getFirst();
                    SlotOptions.spoofSlot(first.slot, SlotOptions.first);
                    SlotOptions.disableSlot(first.slot, false);
                }
                if (ultraSolution.size() > 1) {
                    Solution second = ultraSolution.get(1);
                    SlotOptions.spoofSlot(second.slot, SlotOptions.second);
                    SlotOptions.disableSlot(second.slot, true);
                }
            } else if (item.equals(Items.GLOWSTONE)) {
                List<Solution> solution = new ArrayList<>();
                SlotOptions.clearSpoofedSlots();
                SlotOptions.clearDisabledSlots();
                for (Slot slot : getContainerSlots(event.handler)) {
                    SlotOptions.disableSlot(slot, true);
                    if (isDye(slot.getStack().getItem())) {
                        solution.add(new Solution(slot.getStack(), slot));
                    }
                }
                ultraSolution.clear();
                ultraSolution.addAll(solution);
            }
        }
        if (Config.solveSuperpairs && experimentType.equals(ExperimentType.Superpairs)) {
            if (!isStainedGlass(item)) {
                superSolution.removeIf(s -> s.slot.id == event.slotId);
                superSolution.add(new Solution(event.stack, eventSlot));
            }
            SlotOptions.clearSpoofedSlots();
            SlotOptions.clearDisabledSlots();
            for (Solution solution : superSolution) {
                SlotOptions.spoofSlot(solution.slot, solution.stack);
                SlotOptions.disableSlot(solution.slot, false);
            }
        }
    }

    @EventHandler
    private static void onScreen(ScreenOpenEvent event) {
        rememberPhase = true;
        chronoSolution.clear();
        ultraSolution.clear();
        superSolution.clear();
    }

    @EventHandler
    private static void onSendPacket(SendPacketEvent event) {
        if (event.packet instanceof ClickSlotC2SPacket clickPacket) {
            ExperimentType type = getExperimentType();
            int slotId = clickPacket.slot();
            if (Config.solveChronomatron && type.equals(ExperimentType.Chronomatron) && !rememberPhase) {
                if (!chronoSolution.isEmpty()) {
                    Solution first = chronoSolution.getFirst();
                    if (first.slots.stream().anyMatch(slot -> slot.id == slotId)) {
                        for (Slot slot : first.slots) {
                            SlotOptions.clearSpoof(slot);
                            SlotOptions.disableSlot(slot, true);
                        }
                        chronoSolution.removeFirst();
                    }
                }
                if (!chronoSolution.isEmpty()) {
                    for (Slot solution : chronoSolution.getFirst().slots) {
                        SlotOptions.spoofSlot(solution, SlotOptions.first);
                        SlotOptions.disableSlot(solution, false);
                    }
                }
                if (chronoSolution.size() > 1) {
                    Solution first = chronoSolution.getFirst();
                    for (Slot solution : chronoSolution.get(1).slots) {
                        if (first.slots.stream().noneMatch(slot -> slot.id == solution.id)) {
                            SlotOptions.spoofSlot(solution, SlotOptions.second);
                            SlotOptions.disableSlot(solution, true);
                        }
                    }
                }
            }
            if (Config.solveUltrasequencer && type.equals(ExperimentType.Ultrasequencer) && !rememberPhase) {
                if (!ultraSolution.isEmpty()) {
                    if (ultraSolution.getFirst().slot.id == slotId) {
                        Solution first = ultraSolution.getFirst();
                        SlotOptions.spoofSlot(first.slot, SlotOptions.background);
                        SlotOptions.disableSlot(first.slot, true);
                        ultraSolution.removeFirst();
                    }
                }
                if (!ultraSolution.isEmpty()) {
                    Solution first = ultraSolution.getFirst();
                    SlotOptions.spoofSlot(first.slot, SlotOptions.first);
                    SlotOptions.disableSlot(first.slot, false);
                }
                if (ultraSolution.size() > 1) {
                    Solution second = ultraSolution.get(1);
                    SlotOptions.spoofSlot(second.slot, SlotOptions.second);
                    SlotOptions.disableSlot(second.slot, true);
                }
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

        public Solution(ItemStack stack, Slot slot) {
            this.type = ExperimentType.Ultrasequencer;
            this.stack = stack;
            this.slot = slot;
        }

        public Solution(List<Slot> slots) {
            this.type = ExperimentType.Chronomatron;
            this.slots = slots;
        }
    }
}
