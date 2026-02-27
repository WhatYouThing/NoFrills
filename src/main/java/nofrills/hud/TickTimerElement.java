package nofrills.hud;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.misc.Utils;

public class TickTimerElement extends SimpleTextElement {
    protected final String timerText;
    protected int ticks = -1;
    protected int startTicks = 0;
    protected boolean repeating = false;

    public TickTimerElement(String text, Feature instance, String label) {
        super(Text.literal(Utils.format(text, "N/A")), instance, label);
        this.timerText = text;
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender()) {
            return;
        } else if (!this.isEditingHud() && (this.ticks == -1 || !Utils.isInDungeons())) {
            return;
        }
        this.updateTimer();
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    public void setStartTicks(int ticks) {
        this.startTicks = ticks;
    }

    public void setRepeating(boolean repeating) {
        this.repeating = repeating;
    }

    public String ticksAsTime(int ticks) {
        if (ticks < 0) {
            return "0.00s";
        }
        return Utils.formatDecimal(ticks / 20.0) + "s";
    }

    public void updateTimer() {
        this.setText(Utils.format(this.timerText, this.ticksAsTime(this.ticks)));
    }

    public void tick() {
        if (this.ticks > 0) {
            this.ticks--;
        }
        if (this.ticks == 0) {
            if (this.repeating) {
                this.ticks = this.startTicks;
            } else {
                this.ticks = -1;
            }
        }
    }

    public void start() {
        this.ticks = this.startTicks;
    }

    public void pause() {
        this.ticks = -1;
    }
}
