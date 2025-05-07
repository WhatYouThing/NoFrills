package nofrills.hud.elements;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import nofrills.config.Config;
import nofrills.hud.SimpleTextElement;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

public class Ping extends SimpleTextElement {

    public Ping(Text text, RenderColor color) {
        super(0, 0, text, color);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.setProperties(Config.pingEnabled, false, Config.pingLeftHand, Config.pingPosX, Config.pingPosY);
        super.render(context, mouseX, mouseY, delta);
        Config.pingPosX = this.posX;
        Config.pingPosY = this.posY;
    }

    public void setPing(long ping) {
        this.setText(Utils.format("§bPing: §f{}§7ms", ping));
    }
}
