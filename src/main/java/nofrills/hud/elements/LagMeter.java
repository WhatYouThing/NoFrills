package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import nofrills.config.Feature;
import nofrills.config.SettingInt;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.Utils;

import java.util.List;

public class LagMeter extends SimpleTextElement {
    public final SettingInt min = new SettingInt(500, "min", instance.key());
    public long lastTick = 0;

    public LagMeter(String text) {
        super(Text.literal(text), new Feature("lagMeterElement"), "Lag Meter Element");
        this.options = this.getBaseSettings(List.of(
                new Settings.SliderInt("Minimum Time", 0, 5000, 50, min, "The minimum amount of time (in milliseconds) since the last tick for the element to be visible.")
        ));
        this.setDesc("Displays the time since the last received server tick.");
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender()) {
            return;
        } else if (!this.isEditingHud()) {
            if (lastTick != 0) {
                long sinceTick = Util.getMeasuringTimeMs() - lastTick;
                if (sinceTick >= min.value()) {
                    this.setText(Utils.format("Last server tick was {}s ago", Utils.formatDecimal(sinceTick * 0.001)));
                } else {
                    return;
                }
            } else {
                return;
            }
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    public void setTickTime(long time) {
        this.lastTick = time;
    }
}
