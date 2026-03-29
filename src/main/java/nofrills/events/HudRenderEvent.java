package nofrills.events;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class HudRenderEvent {
    public GuiGraphicsExtractor context;
    public Font textRenderer;
    public DeltaTracker tickCounter;

    public HudRenderEvent(GuiGraphicsExtractor context, Font textRenderer, DeltaTracker tickCounter) {
        this.context = context;
        this.textRenderer = textRenderer;
        this.tickCounter = tickCounter;
    }
}
