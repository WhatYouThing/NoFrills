package nofrills.hud.elements;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import nofrills.hud.SimpleTextElement;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import static nofrills.Main.Config;

public class TPS extends SimpleTextElement {

    public TPS(Text text, RenderColor color) {
        super(0, 0, text, color);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.setProperties(Config.tpsEnabled(), false, Config.tpsLeftHand(), Config.tpsPosX(), Config.tpsPosY());
        super.render(context, mouseX, mouseY, delta);
        Config.tpsPosX(this.posX);
        Config.tpsPosY(this.posY);
    }

    public void setTps(int tps) {
        this.setText(Utils.format("§bTPS: §f{}", tps));
    }
}
