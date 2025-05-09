package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
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
    private static final List<Solution> chronomatronSolution = new ArrayList<>();
    private static final List<Solution> ultrasequencerSolution = new ArrayList<>();
    private static final List<Solution> superpairsSolution = new ArrayList<>();
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
        return item instanceof DyeItem || item.equals(Items.INK_SAC) || item.equals(Items.BONE_MEAL) || item.equals(Items.LAPIS_LAZULI) || item.equals(Items.COCOA_BEANS);
    }

    private static boolean isTerracotta(Item item) {
        return item.toString().endsWith("terracotta");
    }

    private static boolean isStainedGlass(Item item) {
        return item.toString().endsWith("stained_glass");
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
                for (Slot slot : event.handler.slots) {
                    SlotOptions.disableSlot(slot, true);
                }
                if (isTerracotta(item)) {
                    if (chronomatronSolution.isEmpty()) {
                        chronomatronSolution.add(new Solution(new ArrayList<>()));
                    }
                    chronomatronSolution.getLast().slots.add(eventSlot);
                } else if (isStainedGlass(item)) {
                    if (!chronomatronSolution.isEmpty() && chronomatronSolution.getLast().slots.stream().anyMatch(slot -> slot.id == event.slotId)) {
                        chronomatronSolution.add(new Solution(new ArrayList<>()));
                    }
                }
            } else {
                if (isStatus(item)) {
                    return;
                }
                if (!chronomatronSolution.isEmpty()) {
                    for (Slot solution : chronomatronSolution.getFirst().slots) {
                        SlotOptions.spoofSlot(solution, SlotOptions.first);
                        SlotOptions.disableSlot(solution, false);
                    }
                }
                if (chronomatronSolution.size() > 1) {
                    Solution first = chronomatronSolution.getFirst();
                    for (Slot solution : chronomatronSolution.get(1).slots) {
                        if (first.slots.stream().noneMatch(slot -> slot.id == solution.id)) {
                            SlotOptions.spoofSlot(solution, SlotOptions.second);
                            SlotOptions.disableSlot(solution, true);
                        }
                    }
                }
            }
        }
        if (Config.solveUltrasequencer && experimentType.equals(ExperimentType.Ultrasequencer)) {
            if (isDye(item)) {
                ultrasequencerSolution.removeIf(s -> s.stack.getCount() == event.stack.getCount());
                ultrasequencerSolution.add(new Solution(event.stack, eventSlot));
            }
            ultrasequencerSolution.sort(Comparator.comparingInt(s -> s.stack.getCount()));
            SlotOptions.clearSpoofedSlots();
            SlotOptions.clearDisabledSlots();
            for (Slot slot : event.handler.slots) {
                SlotOptions.disableSlot(slot, true);
            }
            for (Solution solution : ultrasequencerSolution) {
                SlotOptions.spoofSlot(solution.slot, solution.stack);
                SlotOptions.disableSlot(solution.slot, false);
            }
        }
        if (Config.solveSuperpairs && experimentType.equals(ExperimentType.Superpairs)) {
            if (isStatus(item)) {
                return;
            }
            if (!isStainedGlass(item)) {
                superpairsSolution.removeIf(s -> s.slot.id == event.slotId);
                superpairsSolution.add(new Solution(event.stack, eventSlot));
            }
            SlotOptions.clearSpoofedSlots();
            SlotOptions.clearDisabledSlots();
            for (Solution solution : superpairsSolution) {
                SlotOptions.spoofSlot(solution.slot, solution.stack);
                SlotOptions.disableSlot(solution.slot, false);
            }
        }
    }

    @EventHandler
    private static void onScreen(ScreenOpenEvent event) {
        rememberPhase = true;
        chronomatronSolution.clear();
        ultrasequencerSolution.clear();
        superpairsSolution.clear();
    }

    @EventHandler
    private static void onSendPacket(SendPacketEvent event) {
        if (Config.solveChronomatron && event.packet instanceof ClickSlotC2SPacket clickPacket) {
            if (getExperimentType().equals(ExperimentType.Chronomatron) && !rememberPhase) {
                int slotId = clickPacket.getSlot();
                if (!chronomatronSolution.isEmpty()) {
                    Solution first = chronomatronSolution.getFirst();
                    if (first.slots.stream().anyMatch(slot -> slot.id == slotId)) {
                        for (Slot slot : first.slots) {
                            SlotOptions.clearSpoof(slot);
                            SlotOptions.disableSlot(slot, true);
                        }
                        chronomatronSolution.removeFirst();
                    }
                }
                if (!chronomatronSolution.isEmpty()) {
                    for (Slot solution : chronomatronSolution.getFirst().slots) {
                        SlotOptions.spoofSlot(solution, SlotOptions.first);
                        SlotOptions.disableSlot(solution, false);
                    }
                }
                if (chronomatronSolution.size() > 1) {
                    Solution first = chronomatronSolution.getFirst();
                    for (Slot solution : chronomatronSolution.get(1).slots) {
                        if (first.slots.stream().noneMatch(slot -> slot.id == solution.id)) {
                            SlotOptions.spoofSlot(solution, SlotOptions.second);
                            SlotOptions.disableSlot(solution, true);
                        }
                    }
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
