package nofrills.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import nofrills.misc.RenderColor;

import static nofrills.Main.mc;

public class SimpleTextElement extends HudElement {
    public Text text;
    public Text defaultText;
    public RenderColor color;

    public SimpleTextElement(double posX, double posY, double sizeX, double sizeY, Text text, RenderColor color) {
        super(posX, posY, sizeX, sizeY);
        this.text = text;
        this.defaultText = text;
        this.color = color;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int centerX = (int) (minX + (maxX - minX) * 0.5);
        int centerY = (int) (minY - mc.textRenderer.fontHeight * 0.5 + (maxY - minY) * 0.5);
        context.drawCenteredTextWithShadow(mc.textRenderer, text, centerX, centerY, color.hex);
        context.fill((int) minX, (int) minY, (int) maxX, (int) maxY, ColorHelper.fromFloats(0.67f, 0.0f, 0.0f, 0.0f));
    }

    public void setText(Text newText) {
        text = newText;
    }

    public void resetText() {
        text = defaultText;
    }
}
