package nofrills.events;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

import java.util.List;

public class DrawItemTooltip {
    public List<Text> lines;
    public ItemStack stack;
    public NbtCompound customData;

    public DrawItemTooltip(List<Text> lines, ItemStack stack, NbtCompound customData) {
        this.lines = lines;
        this.stack = stack;
        this.customData = customData;
    }

    public void addLine(Text line) {
        lines.add(line);
    }
}
