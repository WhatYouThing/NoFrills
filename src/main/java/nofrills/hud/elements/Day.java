package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.network.chat.Component;
import nofrills.config.Feature;
import nofrills.hud.SimpleTextElement;
import nofrills.misc.Utils;

public class Day extends SimpleTextElement {

    public Day(String text) {
        super(Component.literal(text), new Feature("dayElement"), "Day Display");
        this.options = this.getBaseSettings();
        this.setDesc("Displays the day that the server world is on.");
        this.setCategory(Category.Info);
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.shouldRender()) {
            super.draw(context, mouseX, mouseY, partialTicks, delta);
        }
    }

    public void setDay(long day) {
        this.setText(Utils.format("Day: §f{}", day));
    }
}
