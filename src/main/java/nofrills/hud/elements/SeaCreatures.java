package nofrills.hud.elements;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.config.SettingDouble;
import nofrills.config.SettingEnum;
import nofrills.hud.SimpleTextElement;
import nofrills.misc.Utils;


public class SeaCreatures extends SimpleTextElement {
    public final Feature instance = new Feature("seaCreaturesElement");

    public final SettingDouble x = new SettingDouble(0.01, "x", instance.key());
    public final SettingDouble y = new SettingDouble(0.13, "y", instance.key());
    public final SettingEnum<alignment> align = new SettingEnum<>(alignment.Left, alignment.class, "align", instance.key());

    public SeaCreatures(Text text) {
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

    public void setCount(int count) {
        if (count > 0) {
            this.setText(Utils.format("§3Sea Creatures: §f{}", count));
        } else {
            this.setText("§3Sea Creatures: §70");
        }
    }
}
