package nofrills.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import nofrills.misc.RenderColor;

import static nofrills.Main.mc;

public class SimpleTextElement extends HudElement {
    public Text text;
    public Text defaultText;
    public boolean leftHand;
    public RenderColor color;

    public SimpleTextElement(double posX, double posY, Text text, RenderColor color) {
        super(posX, posY, 0, 0);
        this.text = text;
        this.defaultText = text;
        this.color = color;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.shouldRender()) {
            calculateDimensions(context);
            int centerX = (int) minX;
            int centerY = (int) (minY - mc.textRenderer.fontHeight * 0.5 + (maxY - minY) * 0.5);
            context.drawTextWithShadow(mc.textRenderer, text, centerX, centerY, color.hex);
        }
    }

    @Override
    public void calculateDimensions(DrawContext context) {
        int resX = context.getScaledWindowWidth();
        int resY = context.getScaledWindowHeight();
        int width = mc.textRenderer.getWidth(text);
        int height = mc.textRenderer.fontHeight;
        double currentPosX = leftHand ? getOffsetX(context, getX(context, posX) - width) : posX;
        sizeX = getOffsetX(context, width);
        sizeY = getOffsetY(context, height);
        minX = resX * currentPosX;
        maxX = resX * (currentPosX + sizeX);
        minY = resY * posY;
        maxY = resY * (posY + sizeY);
        snapX = resX * 0.01;
        snapY = resY * 0.01;
    }

    public void setText(String newText) {
        text = Text.of(newText);
    }

    public void resetText() {
        text = defaultText;
    }

    public void setProperties(boolean enabled, boolean hidden, boolean leftHand, double x, double y) {
        this.enabled = enabled;
        this.hidden = hidden;
        this.leftHand = leftHand;
        if (!HudManager.isEditingHud()) {
            this.posX = x;
            this.posY = y;
        }
    }
}
