package nofrills.events;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import nofrills.misc.Utils;
import nofrills.mixin.AbstractContainerScreenAccessor;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public class TooltipRenderEvent extends Cancellable {
    public List<Component> lines;
    public List<Component> replacement;
    public ItemStack stack;
    public CompoundTag customData;
    public String title = "";
    public Slot slot = null;

    public TooltipRenderEvent(List<Component> lines, ItemStack stack) {
        this.setCancelled(false);
        this.lines = lines;
        this.replacement = null;
        this.stack = stack;
        this.customData = Utils.getCustomData(stack);
        if (mc.gui.screen() instanceof AbstractContainerScreen<?> container) {
            this.title = container.getTitle().getString();
            this.slot = ((AbstractContainerScreenAccessor) container).getHoveredSlot();
        }
    }

    public void addLine(Component line) {
        if (this.replacement == null) {
            this.replacement = new ArrayList<>(this.lines);
        }
        replacement.add(line);
    }
}
