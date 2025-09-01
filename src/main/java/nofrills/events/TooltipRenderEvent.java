package nofrills.events;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

import java.util.List;

public class TooltipRenderEvent {
    public List<Text> lines;
    public ItemStack stack;
    public NbtCompound customData;
    public String title;

    public TooltipRenderEvent(List<Text> lines, ItemStack stack, NbtCompound customData, String title) {
        this.lines = lines;
        this.stack = stack;
        this.customData = customData;
        this.title = title;
    }

    public void addLine(Text line) {
        try {
            lines.add(line);
        } catch (UnsupportedOperationException ignored) {
        }
    }
}
