package nofrills.events;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class HudRenderEvent {
    public DrawContext context;
    public TextRenderer textRenderer;
    public RenderTickCounter tickCounter;

    public HudRenderEvent(DrawContext context, TextRenderer textRenderer, RenderTickCounter tickCounter) {
        this.context = context;
        this.textRenderer = textRenderer;
        this.tickCounter = tickCounter;
    }
}
