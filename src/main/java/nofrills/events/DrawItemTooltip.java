package nofrills.events;

import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

public class DrawItemTooltip {
    public List<Text> lines;
    public ItemStack stack;
    public NbtComponent customData;

    public DrawItemTooltip(List<Text> lines, ItemStack stack, NbtComponent customData) {
        this.lines = lines;
        this.stack = stack;
        this.customData = customData;
    }

    public void addLine(Text line) {
        try {
            lines.add(line);
        }
        catch (UnsupportedOperationException ignored) {}
    }
}
