package nofrills.hud.elements;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import nofrills.hud.SimpleTextElement;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import static nofrills.Main.Config;

public class Day extends SimpleTextElement {

    public Day(Text text, RenderColor color) {
        super(0, 0, text, color);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.setProperties(Config.dayEnabled(), false, Config.dayLeftHand(), Config.dayPosX(), Config.dayPosY());
        super.render(context, mouseX, mouseY, delta);
        Config.dayPosX(this.posX);
        Config.dayPosY(this.posY);
    }

    public void setDay(long day) {
        this.setText(Utils.format("§bDay: §f{}", day));
    }
}
