package nofrills.hud.clickgui.components;

import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.UIComponent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

import static nofrills.Main.mc;

public class PlainLabel extends LabelComponent {
    public String plainTooltip = "";
    public String plainText;

    public PlainLabel(Component text) {
        super(text);
        this.plainText = text.getString();
    }

    @Override
    public boolean onMouseDown(MouseButtonEvent click, boolean doubled) {
        return false;
    }

    @Override
    public UIComponent tooltip(Component tooltip) {
        this.plainTooltip = tooltip.getString().replaceAll("\n", " ");
        return super.tooltip(tooltip);
    }

    @Override
    public LabelComponent text(Component text) {
        this.plainText = text.getString();
        return super.text(text);
    }

    @Override
    public void drawTooltip(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldDrawTooltip(mouseX, mouseY)) return;
        context.drawTooltip(mc.font, mouseX, mouseY, this.tooltip());
    }

    public int getTextHeight() { // premium
        List<FormattedCharSequence> lines = mc.font.split(text, this.horizontalSizing().get().isContent() ? 0 : this.horizontalSizing().get().value);
        return (lines.size() * (this.lineHeight() + this.lineSpacing())) - this.lineSpacing();
    }

    public String getTooltip() {
        return this.plainTooltip;
    }

    public String getText() {
        return plainText;
    }
}
