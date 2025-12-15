package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.features.dungeons.TickTimers;
import nofrills.hud.HudSettings;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.Utils;

import java.util.List;

public class TickTimer extends SimpleTextElement {

    public TickTimer(String text) {
        super(Text.literal(text), new Feature("tickTimerElement"), "Tick Timer Element");
        this.options = new HudSettings(List.of(
                new Settings.Description("Usage", "This element is used by the Tick Timers feature to display the current tick timer. Only appears while in Dungeons."),
                new Settings.Toggle("Shadow", this.textShadow, "Adds a shadow to the element's text."),
                new Settings.Dropdown<>("Alignment", this.textAlignment, "The alignment of the element's text.")
        ));
        this.options.setTitle(this.elementLabel);
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
