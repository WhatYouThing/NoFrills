package nofrills.events;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.renderstate.LineElementRenderState;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import org.joml.Matrix3x2f;

import static nofrills.Main.mc;

public class ScreenRenderEvent {
    public DrawContext context;
    public int mouseX;
    public int mouseY;
    public float deltaTicks;
    public String title;
    public ScreenHandler handler;
    public Slot focusedSlot;

    public ScreenRenderEvent(DrawContext context, int mouseX, int mouseY, float deltaTicks, String title, ScreenHandler handler, Slot focusedSlot) {
        this.context = context;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.deltaTicks = deltaTicks;
        this.title = title;
        this.handler = handler;
        this.focusedSlot = focusedSlot;
    }

    public void drawLine(int firstSlot, int secondSlot, double width, RenderColor color) {
        Slot slot1 = this.handler.getSlot(firstSlot);
        Slot slot2 = this.handler.getSlot(secondSlot);
        drawLine(RenderPipelines.GUI, slot1.x + 8, slot1.y + 8, slot2.x + 8, slot2.y + 8, width, Color.ofArgb(color.argb));
    }

    public void drawLine(RenderPipeline pipeline, int x1, int y1, int x2, int y2, double width, Color color) {
        this.context.state.addSimpleElement(new LineElementRenderState(
                pipeline,
                new Matrix3x2f(context.getMatrices()),
                context.scissorStack.peekLast(),
                x1, y1, x2, y2,
                width,
                color
        ));
    }

    public void drawBorder(int slotId, RenderColor color) {
        Slot slot = this.handler.getSlot(slotId);
        Rendering.drawBorder(this.context, slot.x, slot.y, 16, 16, color.argb);
    }

    public void drawLabel(int slotId, Text text) {
        Slot slot = this.handler.getSlot(slotId);
        this.context.drawCenteredTextWithShadow(mc.textRenderer, text, slot.x + 8, slot.y + 4, RenderColor.white.argb);
    }

    public void drawFill(int slotId, RenderColor color) {
        Slot slot = this.handler.getSlot(slotId);
        this.context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, color.argb);
    }

    public static class Before extends ScreenRenderEvent {
        public Before(DrawContext context, int mouseX, int mouseY, float deltaTicks, String title, ScreenHandler handler, Slot focusedSlot) {
            super(context, mouseX, mouseY, deltaTicks, title, handler, focusedSlot);
        }
    }

    public static class After extends ScreenRenderEvent {
        public After(DrawContext context, int mouseX, int mouseY, float deltaTicks, String title, ScreenHandler handler, Slot focusedSlot) {
            super(context, mouseX, mouseY, deltaTicks, title, handler, focusedSlot);
        }
    }
}
