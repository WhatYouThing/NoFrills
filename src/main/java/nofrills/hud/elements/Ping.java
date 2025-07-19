package nofrills.hud.elements;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.config.SettingDouble;
import nofrills.config.SettingEnum;
import nofrills.hud.SimpleTextElement;
import nofrills.misc.Utils;

public class Ping extends SimpleTextElement {
    public final Feature instance = new Feature("pingElement");

    public final SettingDouble x = new SettingDouble(0.01, "x", instance.key());
    public final SettingDouble y = new SettingDouble(0.01, "y", instance.key());
    public final SettingEnum<alignment> align = new SettingEnum<>(alignment.Left, alignment.class, "align", instance.key());

    public Ping(Text text) {
        super(0, 0, text);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (instance.isActive()) {
            this.label.horizontalTextAlignment(this.getAlignment(align.value()));
            this.updateX((int) x.value() * context.getScaledWindowWidth());
            this.updateY((int) y.value() * context.getScaledWindowHeight());
            super.render(context, mouseX, mouseY, delta);
        }
    }

    public void setPing(long ping) {
        this.setText(Utils.format("§bPing: §f{}ms", ping));
    }
}
