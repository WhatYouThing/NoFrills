package nofrills.hud;

import io.wispforest.owo.ui.container.DraggableContainer;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.DrawContext;

import static nofrills.Main.mc;

public class HudElement extends DraggableContainer<FlowLayout> {
    public FlowLayout layout;
    public boolean hidden;

    public HudElement(double posX, double posY, FlowLayout layout) {
        super(Sizing.content(), Sizing.content(), layout);
        this.layout = layout;
        this.foreheadSize(5);
        this.updateX((int) (posX * mc.getWindow().getScaledWidth()));
        this.updateY((int) (posY * mc.getWindow().getScaledHeight()));
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!this.hidden) {
            super.draw(OwoUIDrawContext.of(context), mouseX, mouseY, mc.getRenderTickCounter().getTickProgress(false), delta);
        }
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
    }
}