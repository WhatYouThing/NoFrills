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
    public static final ItemStack BACKGROUND = stackWithName(Items.BLACK_STAINED_GLASS_PANE.getDefaultStack(), " ");
    public static final ItemStack SOLID_BACKGROUND = stackWithName(Items.GRAY_CONCRETE.getDefaultStack(), " ");
    public static final ItemStack FIRST = stackWithName(Items.LIME_CONCRETE.getDefaultStack(), Utils.Symbols.format + "aClick here!");
    public static final ItemStack SECOND = stackWithName(Items.ORANGE_CONCRETE.getDefaultStack(), Utils.Symbols.format + "9Click next.");
    public static final ItemStack THIRD = stackWithName(Items.RED_CONCRETE.getDefaultStack(), Utils.Symbols.format + "cClick after.");
    public static final ConcurrentHashMap<Slot, Flags> slotFlags = new ConcurrentHashMap<>();

    public static ItemStack stackWithName(ItemStack stack, String name) {
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.of(name));
        return stack;
    }

    public static ItemStack stackWithName(ItemStack stack, Text name) {
        stack.set(DataComponentTypes.CUSTOM_NAME, name);
        return stack;
    }

    public static ItemStack stackWithCount(ItemStack stack, int quantity) {
        return stack.copyWithCount(quantity);
    }

    public static Flags getOrInit(Slot slot) {
        if (slot != null) {
            if (!slotFlags.containsKey(slot)) {
                slotFlags.put(slot, new Flags());
            }
            return slotFlags.get(slot);
        }
        return new Flags();
    }

    public static boolean isDisabled(Slot slot) {
        return slot != null && slotFlags.containsKey(slot) && slotFlags.get(slot).disabled;
    }

    public static void setDisabled(Slot slot, boolean disabled) {
        getOrInit(slot).setDisabled(disabled);
    }

    public static void clearDisabled() {
        for (Map.Entry<Slot, Flags> entry : slotFlags.entrySet()) {
            Flags value = entry.getValue();
            if (value.disabled) {
                entry.setValue(value.setDisabled(false));
            }
        }
    }

    public static boolean isSpoofed(Slot slot) {
        return slot != null && slotFlags.containsKey(slot) && slotFlags.get(slot).spoofed;
    }

    public static ItemStack getSpoofed(Slot slot) {
        if (isSpoofed(slot)) {
            return slotFlags.get(slot).replacement;
        }
        return ItemStack.EMPTY;
    }

    public static void setSpoofed(Slot slot, ItemStack replacement) {
        getOrInit(slot).setSpoofed(true).setReplacement(replacement);
    }

    public static void clearSpoofed(Slot slot) {
        if (slot != null && slotFlags.containsKey(slot)) {
            slotFlags.get(slot).setSpoofed(false);
        }
    }

    public static void clearSpoofed() {
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

    public static RenderColor getBackground(Slot slot) {
        if (hasBackground(slot)) {
            return slotFlags.get(slot).color;
        }
        return RenderColor.fromHex(0xffffff);
    }

    public static void setBackground(Slot slot, RenderColor color) {
        getOrInit(slot).setBackground(true).setBackgroundColor(color);
    }

    public static void clearBackground(Slot slot) {
        if (slot != null && slotFlags.containsKey(slot)) {
            slotFlags.get(slot).setBackground(false);
        }
    }

    public static void clearBackground() {
        for (Map.Entry<Slot, Flags> entry : slotFlags.entrySet()) {
            Flags value = entry.getValue();
            if (value.background) {
                entry.setValue(value.setBackground(false));
            }
        }
    }

    public static boolean hasCount(Slot slot) {
        return slot != null && slotFlags.containsKey(slot) && slotFlags.get(slot).count != null;
    }

    public static String getCount(Slot slot) {
        if (hasCount(slot)) {
            return slotFlags.get(slot).count;
        }
        return "";
    }

    public static void setCount(Slot slot, String count) {
        getOrInit(slot).setCount(count);
    }

    public static void clearCount(Slot slot) {
        if (slot != null && slotFlags.containsKey(slot)) {
            slotFlags.get(slot).setCount(null);
        }
    }

    public static void clearCount() {
        for (Map.Entry<Slot, Flags> entry : slotFlags.entrySet()) {
            Flags value = entry.getValue();
            if (value.count != null) {
                entry.setValue(value.setCount(null));
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
        public String count = null;

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

        public Flags setCount(String count) {
            this.count = count;
            return this;
        }
    }
}
