package nofrills.hud.elements;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import nofrills.config.Config;
import nofrills.hud.SimpleTextElement;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

public class SeaCreatures extends SimpleTextElement {

    public SeaCreatures(Text text, RenderColor color) {
        super(0, 0, text, color);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.setProperties(Config.seaCreaturesEnabled, false, Config.seaCreaturesLeftHand, Config.seaCreaturesPosX, Config.seaCreaturesPosY);
        super.render(context, mouseX, mouseY, delta);
        Config.seaCreaturesPosX = this.posX;
        Config.seaCreaturesPosY = this.posY;
    }

    public void setCount(int count) {
        if (count > 0) {
            this.setText(Utils.format("§3Sea Creatures: §f{}", count));
        } else {
            this.setText("§3Sea Creatures: §70");
        }
    }
}
