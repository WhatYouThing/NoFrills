package nofrills.hud.clickgui.components;

import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.DrawContext;

public class FlatTextbox extends TextBoxComponent {

    public FlatTextbox(Sizing horizontalSizing) {
        super(horizontalSizing);
        this.verticalSizing(Sizing.fixed(18));
        this.margins(Insets.of(0, 0, 0, 8));
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.fill(this.x(), this.y(), this.getX() + this.width() + 4, this.y() + this.height(), 0xff101010);
        context.drawBorder(this.x(), this.y(), this.width() + 4, this.height(), 0xff5ca0bf);
        super.renderWidget(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean drawsBackground() {
        return false;
    }

    @Override
    public int getInnerWidth() {
        return this.width - 8;
    }
}
