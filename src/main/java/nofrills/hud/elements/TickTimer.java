package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.features.dungeons.TickTimers;
import nofrills.hud.SimpleTextElement;
import nofrills.misc.Utils;

public class TickTimer extends SimpleTextElement {

    public TickTimer(String text) {
        super(Text.literal(text), new Feature("tickTimerElement"), "Tick Timer Element");
        this.options = this.getBaseSettings();
        this.setDesc("Displays timers while in Dungeons. Used by the Tick Timers feature.");
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender()) {
            return;
        } else if (!this.isEditingHud()) {
            if (!Utils.isInDungeons() || TickTimers.getTimerList().isEmpty()) return;
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }
}
