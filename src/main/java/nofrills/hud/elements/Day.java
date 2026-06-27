package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.network.chat.Component;
import nofrills.config.Feature;
import nofrills.hud.SimpleTextElement;
import nofrills.misc.Utils;

public final class Day extends SimpleTextElement {
    private long day = 0;
    private boolean dirty = false;

    public Day(String text) {
        super(Component.literal(text), new Feature("dayElement"), "Day Display");
        this.options = this.getBaseSettings();
        this.setDesc("Displays the day that the server world is on.");
        this.setCategory(Category.Info);
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.shouldRender()) {
            if (this.dirty) {
                this.setText(Utils.format("Day: §f{}", this.day));
                this.dirty = false;
            }
            super.draw(context, mouseX, mouseY, partialTicks, delta);
        }
    }

    public void setDay(long day) {
        this.day = day;
        this.dirty = true;
    }
}
