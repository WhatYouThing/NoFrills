package nofrills.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import nofrills.events.ScreenOpenEvent;

import java.util.ArrayList;
import java.util.List;

public class SlotOptions {
    private static final List<DisabledSlot> disabledSlots = new ArrayList<>();
    private static final List<SpoofedSlot> spoofedSlots = new ArrayList<>();

    public static List<DisabledSlot> getDisabledSlots() {
        return new ArrayList<>(disabledSlots);
    }

    public static boolean isSlotDisabled(Slot slot) {
        return slot != null && getDisabledSlots().stream().anyMatch(disabled -> disabled.isSlot(slot));
    }

    public static void disableSlot(Slot slot, boolean disabled) {
        disabledSlots.removeIf(disabledSlot -> disabledSlot.isSlot(slot));
        if (disabled) {
            disabledSlots.add(new DisabledSlot(slot));
        }
    }

    public static void clearDisabledSlots() {
        disabledSlots.clear();
    }

    public static List<SpoofedSlot> getSpoofedSlots() {
        return new ArrayList<>(spoofedSlots);
    }

    public static boolean isSlotSpoofed(Slot slot) {
        return slot != null && getSpoofedSlots().stream().anyMatch(spoofed -> spoofed.isSlot(slot));
    }

    public static ItemStack getSpoofedStack(Slot slot) {
        return getSpoofedSlots().stream().filter(spoofed -> spoofed.isSlot(slot)).findFirst().map(value -> value.replacementStack).orElse(null);
    }

    public static void spoofSlot(Slot slot, ItemStack replacement) {
        spoofedSlots.removeIf(spoofed -> spoofed.isSlot(slot));
        spoofedSlots.add(new SpoofedSlot(slot, replacement));
    }

    public static void clearSpoof(Slot slot) {
        spoofedSlots.removeIf(spoofed -> spoofed.isSlot(slot));
    }

    public static void clearSpoofedSlots() {
        spoofedSlots.clear();
    }

    @EventHandler
    private static void onScreen(ScreenOpenEvent event) {
        clearDisabledSlots();
        clearSpoofedSlots();
    }

    public static class SpoofedSlot {
        public int slotId;
        public ItemStack replacementStack;

        public SpoofedSlot(Slot slot, ItemStack replacementStack) {
            this.slotId = slot.id;
            this.replacementStack = replacementStack;
        }

        public boolean isSlot(Slot slot) {
            return slot != null && slot.id == slotId;
        }
    }

    public static class DisabledSlot {
        public int slotId;

        public DisabledSlot(Slot slot) {
            this.slotId = slot.id;
        }

        public boolean isSlot(Slot slot) {
            return slot != null && slot.id == slotId;
        }
    }
}
