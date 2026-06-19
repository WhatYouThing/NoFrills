package nofrills.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class TooltipRenderEvent {
    public List<Component> lines;
    public ItemStack stack;
    public CompoundTag customData;
    public String title;

    public TooltipRenderEvent(List<Component> lines, ItemStack stack, CompoundTag customData, String title) {
        this.lines = lines;
        this.stack = stack;
        this.customData = customData;
        this.title = title;
    }

    public void addLine(Component line) {
        try {
            lines.add(line);
        } catch (UnsupportedOperationException ignored) {
        }
    }

    public static final class Before extends Cancellable {
        public ItemStack stack;
        public String title;

        public Before(ItemStack stack, String title) {
            this.setCancelled(false);
            this.stack = stack;
            this.title = title;
        }
    }
}
