package nofrills.hud.clickgui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.text.Text;

public class ClickGuiModule implements Drawable {
    public Text title;

    public ClickGuiModule(Text title) {
        this.title = title;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

    }
}