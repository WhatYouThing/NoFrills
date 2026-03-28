package nofrills.events;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.renderstate.LineElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.chat.Component;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import org.joml.Matrix3x2f;

import java.util.Optional;

import static nofrills.Main.mc;

public class ScreenRenderEvent {
    public GuiGraphicsExtractor context;
    public int mouseX;
    public int mouseY;
    public float deltaTicks;
    public String title;
    public AbstractContainerMenu handler;
    public Slot focusedSlot;

    public ScreenRenderEvent(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks, String title, AbstractContainerMenu handler, Slot focusedSlot) {
        this.context = context;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.deltaTicks = deltaTicks;
        this.title = title;
        this.handler = handler;
        this.focusedSlot = focusedSlot;
    }

    private Optional<Slot> getSlot(int slotId) {
        if (slotId < 0 || slotId >= this.handler.slots.size()) {
            return Optional.empty();
        }
        return Optional.of(this.handler.getSlot(slotId));
    }

    public void drawLine(int firstId, int secondId, double width, RenderColor color) {
        Optional<Slot> slot1 = this.getSlot(firstId);
        Optional<Slot> slot2 = this.getSlot(secondId);
        if (slot1.isPresent() && slot2.isPresent()) {
            Slot first = slot1.get();
            Slot second = slot2.get();
            this.drawLine(RenderPipelines.GUI, first.x + 8, first.y + 8, second.x + 8, second.y + 8, width, Color.ofArgb(color.argb));
        }
    }

    public void drawLine(RenderPipeline pipeline, int x1, int y1, int x2, int y2, double width, Color color) {
        this.context.guiRenderState.addGuiElement(new LineElementRenderState(
                pipeline,
                new Matrix3x2f(context.pose()),
                context.scissorStack.peek(),
                x1, y1, x2, y2,
                width,
                color
        ));
    }

    public void drawBorder(int slotId, RenderColor color) {
        this.getSlot(slotId).ifPresent(slot -> Rendering.drawBorder(this.context, slot.x, slot.y, 16, 16, color.argb));
    }

    public void drawLabel(int slotId, Component text) {
        this.getSlot(slotId).ifPresent(slot -> this.context.centeredText(mc.font, text, slot.x + 8, slot.y + 4, RenderColor.white.argb));
    }

    public void drawFill(int slotId, RenderColor color) {
        this.getSlot(slotId).ifPresent(slot -> this.context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, color.argb));
    }

    public static class Before extends ScreenRenderEvent {
        public Before(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks, String title, AbstractContainerMenu handler, Slot focusedSlot) {
            super(context, mouseX, mouseY, deltaTicks, title, handler, focusedSlot);
        }
    }

    public static class After extends ScreenRenderEvent {
        public After(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks, String title, AbstractContainerMenu handler, Slot focusedSlot) {
            super(context, mouseX, mouseY, deltaTicks, title, handler, focusedSlot);
        }
    }
}
