package nofrills.hud.clickgui.components;

import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.core.UIComponent;
import net.minecraft.client.gui.Click;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;

import static nofrills.Main.mc;

public class PlainLabel extends LabelComponent {
    public String plainTooltip = "";
    public String plainText;

    public PlainLabel(Text text) {
        super(text);
        this.plainText = text.getString();
    }

    @Override
    public boolean onMouseDown(Click click, boolean doubled) {
        return false;
    }

    @Override
    public UIComponent tooltip(Text tooltip) {
        this.plainTooltip = tooltip.getString().replaceAll("\n", " ");
        return super.tooltip(tooltip);
    }

    @Override
    public LabelComponent text(Text text) {
        this.plainText = text.getString();
        return super.text(text);
    }

    public int getTextHeight() { // premium
        List<OrderedText> lines = mc.textRenderer.wrapLines(text, this.horizontalSizing().get().isContent() ? 0 : this.horizontalSizing().get().value);
        return (lines.size() * (this.lineHeight() + this.lineSpacing())) - this.lineSpacing();
    }

    public String getTooltip() {
        return this.plainTooltip;
    }

    public String getText() {
        return plainText;
    }
}
