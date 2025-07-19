package nofrills.hud.elements;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import nofrills.config.Feature;
import nofrills.config.SettingDouble;
import nofrills.config.SettingEnum;
import nofrills.config.SettingInt;
import nofrills.hud.SimpleTextElement;
import nofrills.misc.Utils;

public class LagMeter extends SimpleTextElement {
    public final Feature instance = new Feature("lagMeterElement");

    public final SettingDouble x = new SettingDouble(0.01, "x", instance.key());
    public final SettingDouble y = new SettingDouble(0.19, "y", instance.key());
    public final SettingEnum<alignment> align = new SettingEnum<>(alignment.Left, alignment.class, "align", instance.key());
    public final SettingInt min = new SettingInt(500, "min", instance.key());

    public long lastTick = 0;

    public LagMeter(Text text) {
        super(0, 0, text);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (instance.isActive()) {
            this.label.horizontalTextAlignment(this.getAlignment(align.value()));
            this.updateX((int) x.value() * context.getScaledWindowWidth());
            this.updateY((int) y.value() * context.getScaledWindowHeight());
            if (lastTick != 0) {
                long sinceTick = Util.getMeasuringTimeMs() - lastTick;
                if (sinceTick >= min.value()) {
                    this.hidden = false;
                    this.setText(Utils.format("§cLast server tick was {}s ago", Utils.formatDecimal(sinceTick * 0.001)));
                } else {
                    this.hidden = true;
                }
            } else {
                this.hidden = true;
            }
            super.render(context, mouseX, mouseY, delta);
        }
    }

    public void setTickTime(long time) {
        this.lastTick = time;
    }
}
