package nofrills.hud.clickgui.components;

import io.wispforest.owo.ui.component.TextAreaComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.DrawContext;

import static nofrills.Main.mc;

public class FlatTextbox extends TextAreaComponent {
    public FlatTextbox(Sizing horizontalSizing, Sizing verticalSizing) {
        super(horizontalSizing, verticalSizing);
    }

    @Override
    protected void draw(DrawContext context, int x, int y, int width, int height) {
        context.fill(x, y, x + width, y + height, 0xaaffffff);
    }

    @Override
    protected void drawScrollbar(DrawContext context) {
    }

    @Override
    public int getContentsHeight() {
        return mc.textRenderer.fontHeight;
    }
}
