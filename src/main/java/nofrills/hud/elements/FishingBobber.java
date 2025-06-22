package nofrills.hud.elements;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import nofrills.hud.SimpleTextElement;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import static nofrills.Main.Config;

public class FishingBobber extends SimpleTextElement {

    public FishingBobber(Text text, RenderColor color) {
        super(0, 0, text, color);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.setProperties(Config.bobberEnabled(), false, Config.bobberLeftHand(), Config.bobberPosX(), Config.bobberPosY());
        super.render(context, mouseX, mouseY, delta);
        Config.bobberPosX(this.posX);
        Config.bobberPosY(this.posY);
    }

    public void setActive() {
        this.setText("§cBobber: §aActive");
    }

    public void setInactive() {
        this.setText("§cBobber: §7Inactive");
    }

    public void setTimer(String timer) {
        if (timer.equals("!!!")) {
            this.setText("§cBobber: §c§lReel!");
        } else {
            this.setText(Utils.format("§cBobber: §e§l{}", timer));
        }
    }
}
