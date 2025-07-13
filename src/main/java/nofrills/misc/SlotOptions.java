package nofrills.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import nofrills.events.ScreenOpenEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SlotOptions {
    public static final ItemStack background = stackWithName(Items.BLACK_STAINED_GLASS_PANE.getDefaultStack(), " ");
    public static final ItemStack first = stackWithName(Items.LIME_CONCRETE.getDefaultStack(), Utils.Symbols.format + "aClick here!");
    public static final ItemStack second = stackWithName(Items.ORANGE_CONCRETE.getDefaultStack(), Utils.Symbols.format + "9Click next.");
    public static final ConcurrentHashMap<Slot, Flags> slotFlags = new ConcurrentHashMap<>();

    public static ItemStack stackWithName(ItemStack stack, String name) {
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.of(name));
        return stack;
    }

    public static ItemStack stackWithQuantity(ItemStack stack, int quantity) {
        ItemStack copy = stack.copy();
        copy.setCount(quantity);
        return copy;
    }

    public static boolean isSlotDisabled(Slot slot) {
        return slot != null && slotFlags.containsKey(slot) && slotFlags.get(slot).disabled;
    }

    public static void disableSlot(Slot slot, boolean disabled) {
        if (slot != null) {
            if (slotFlags.containsKey(slot)) {
                slotFlags.get(slot).setDisabled(disabled);
            } else {
                slotFlags.put(slot, new Flags().setDisabled(disabled));
            }
        }
    }

    public static void clearDisabledSlots() {
        for (Map.Entry<Slot, Flags> entry : slotFlags.entrySet()) {
            Flags value = entry.getValue();
            if (value.disabled) {
                entry.setValue(value.setDisabled(false));
            }
        }
    }

    public static boolean isSlotSpoofed(Slot slot) {
        return slot != null && slotFlags.containsKey(slot) && slotFlags.get(slot).spoofed;
    }

    public static ItemStack getSpoofedStack(Slot slot) {
        if (isSlotSpoofed(slot)) {
            return slotFlags.get(slot).replacement;
        }
        return ItemStack.EMPTY;
    }

    public static void spoofSlot(Slot slot, ItemStack replacement) {
        if (slot != null) {
            if (slotFlags.containsKey(slot)) {
                slotFlags.get(slot).setSpoofed(true).setReplacement(replacement);
            } else {
                slotFlags.put(slot, new Flags().setSpoofed(true).setReplacement(replacement));
            }
        }
    }

    public static void clearSpoof(Slot slot) {
        if (slot != null && slotFlags.containsKey(slot)) {
            slotFlags.get(slot).setSpoofed(false);
        }
    }

    public static void clearSpoofedSlots() {
        for (Map.Entry<Slot, Flags> entry : slotFlags.entrySet()) {
            Flags value = entry.getValue();
            if (value.spoofed) {
                entry.setValue(value.setSpoofed(false));
            }
        }
    }

    public static boolean hasBackground(Slot slot) {
        return slot != null && slotFlags.containsKey(slot) && slotFlags.get(slot).background;
    }

    public static RenderColor getBackgroundColor(Slot slot) {
        if (hasBackground(slot)) {
            return slotFlags.get(slot).color;
        }
        return RenderColor.fromHex(0xffffff);
    }

    public static void setBackground(Slot slot, RenderColor color) {
        if (slot != null) {
            if (slotFlags.containsKey(slot)) {
                slotFlags.get(slot).setBackground(true).setBackgroundColor(color);
            } else {
                slotFlags.put(slot, new Flags().setBackground(true).setBackgroundColor(color));
            }
        }
    }

    public static void clearBackground(Slot slot) {
        if (slot != null && slotFlags.containsKey(slot)) {
            slotFlags.get(slot).setBackground(false);
        }
    }

    public static void clearBackgrounds() {
        for (Map.Entry<Slot, Flags> entry : slotFlags.entrySet()) {
            Flags value = entry.getValue();
            if (value.background) {
                entry.setValue(value.setBackground(false));
            }
        }
    }

    @EventHandler
    private static void onScreen(ScreenOpenEvent event) {
        slotFlags.clear();
    }

    public static class Flags {
        public boolean disabled = false;
        public boolean spoofed = false;
        public ItemStack replacement = ItemStack.EMPTY;
        public boolean background = false;
        public RenderColor color = RenderColor.fromHex(0xffffff);

        public Flags() {
        }

        public Flags setDisabled(boolean toggle) {
            this.disabled = toggle;
            return this;
        }

        public Flags setSpoofed(boolean toggle) {
            this.spoofed = toggle;
            return this;
        }

        public Flags setReplacement(ItemStack replacement) {
            this.replacement = replacement;
            return this;
        }

        public Flags setBackground(boolean toggle) {
            this.background = toggle;
            return this;
        }

        public Flags setBackgroundColor(RenderColor color) {
            this.color = color;
            return this;
        }
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
