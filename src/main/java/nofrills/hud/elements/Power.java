package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.hud.SimpleTextElement;
import nofrills.misc.Utils;

public final class Power extends SimpleTextElement {

    public Power(String text) {
        super(Text.literal(text), new Feature("powerElement"), "Power Display");
        this.options = this.getBaseSettings();
        this.setDesc("Displays the power blessing level while in Dungeons.");
        this.setCategory(Category.Dungeons);
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender()) {
            return;
        } else if (!this.isEditingHud() && !Utils.isInDungeons()) {
            return;
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    public void setPower(double power) {
        this.setText(Utils.format("Power: §f{}", power));
    }
}
