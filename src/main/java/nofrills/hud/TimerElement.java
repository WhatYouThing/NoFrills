package nofrills.hud;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import nofrills.config.Feature;
import nofrills.misc.Utils;

public class TimerElement extends SimpleTextElement {
    protected final String timerText;
    protected long duration = 0;
    protected long time = 0;

    public TimerElement(String text, Feature instance, String label) {
        super(Text.literal(Utils.format(text, "N/A")), instance, label);
        this.timerText = text;
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender()) {
            return;
        } else if (!this.isEditingHud() && this.time == 0) {
            return;
        }
        this.updateTimer();
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    public String millisecondsAsTime(long time) {
        if (time < 0) {
            return "0.00s";
        }
        return Utils.formatDecimal(time * 0.001) + "s";
    }

    public String getTimeColor(long timeLeft) {
        double percentage = (double) timeLeft / this.duration;
        if (percentage > 0.66) {
            return "§c";
        }
        if (percentage > 0.33) {
            return "§6";
        }
        return "§a";
    }

    public void updateTimer() {
        long timeLeft = this.time - Util.getMeasuringTimeMs();
        if (timeLeft > 0) {
            this.setText(Utils.format(this.timerText, this.getTimeColor(timeLeft) + this.millisecondsAsTime(timeLeft)));
        } else if (this.time != 0) {
            this.pause();
        }
    }

    public void start(long duration) {
        this.duration = duration;
        this.time = duration + Util.getMeasuringTimeMs();
    }

    public void pause() {
        this.duration = 0;
        this.time = 0;
    }
}
