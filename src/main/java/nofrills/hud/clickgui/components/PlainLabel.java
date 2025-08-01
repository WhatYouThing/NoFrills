package nofrills.hud.clickgui.components;

import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.core.Component;
import net.minecraft.text.Text;

public class PlainLabel extends LabelComponent {
    public String plainTooltip = "";
    public String plainText;

    public PlainLabel(Text text) {
        super(text);
        this.plainText = text.getString();
    }

    @Override
    public Component tooltip(Text tooltip) {
        this.plainTooltip = tooltip.getString().replaceAll("\n", " ");
        return super.tooltip(tooltip);
    }

    @Override
    public LabelComponent text(Text text) {
        this.plainText = text.getString();
        return super.text(text);
    }

    public String getTooltip() {
        return this.plainTooltip;
    }

    public String getText() {
        return plainText;
    }
}
