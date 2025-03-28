package nofrills.hud;

import net.minecraft.client.gui.DrawContext;

import static nofrills.Main.mc;

public class TestElement extends HudElement {
    public TestElement(double posX, double posY, double sizeX, double sizeY) {
        super(posX, posY, sizeX, sizeY);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawTextWithShadow(mc.textRenderer, "deez", (int) this.minX, (int) this.minY, 0xffffff);
    }
}
