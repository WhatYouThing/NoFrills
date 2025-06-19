package nofrills.hud.clickgui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.text.Text;

import static nofrills.Main.mc;

public class ClickGuiCategory implements Drawable {
    public Text title;
    public int posX;
    public int posY;

    public ClickGuiCategory(Text title, int posX, int posY) {
        this.title = title;
        this.posX = posX;
        this.posY = posY;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int height = mc.textRenderer.fontHeight;
        int width = mc.textRenderer.getWidth(title);
        context.fill(posX, posY, posX + width + 20, posY + height + 2, 0xff000000);
        context.drawCenteredTextWithShadow(mc.textRenderer, title, posX + (width + 20) / 2, posY + 1, 0xffffff);
    }
}

