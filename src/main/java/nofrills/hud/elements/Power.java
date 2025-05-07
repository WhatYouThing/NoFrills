package nofrills.hud.elements;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import nofrills.config.Config;
import nofrills.hud.SimpleTextElement;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

public class Power extends SimpleTextElement {

    public Power(Text text, RenderColor color) {
        super(0, 0, text, color);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.setProperties(Config.powerEnabled, Config.powerDungeonsOnly && !Utils.isInDungeons(), Config.powerLeftHand, Config.powerPosX, Config.powerPosY);
        super.render(context, mouseX, mouseY, delta);
        Config.powerPosX = this.posX;
        Config.powerPosY = this.posY;
    }

    public void setPower(double power) {
        this.setText(Utils.format("§bPower: §f{}", power));
    }
}
