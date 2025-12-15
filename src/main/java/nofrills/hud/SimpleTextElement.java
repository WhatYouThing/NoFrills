package nofrills.hud;

import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingEnum;
import nofrills.hud.clickgui.components.PlainLabel;

public class SimpleTextElement extends HudElement {
    public final SettingEnum<TextAlignment> textAlignment;
    public final SettingBool textShadow;
    public MutableText text;
    public MutableText defaultText;
    public LabelComponent label;

    public SimpleTextElement(MutableText text, Feature instance, String label) {
        super(Containers.horizontalFlow(Sizing.content(), Sizing.content()), instance, label);
        this.textAlignment = new SettingEnum<>(TextAlignment.Left, TextAlignment.class, "align", instance);
        this.textShadow = new SettingBool(true, "shadow", instance);
        this.text = text.withColor(this.getTextColor());
        this.defaultText = this.text.copy();
        this.label = new PlainLabel(this.text);
        this.label.margins(Insets.of(2));
        this.layout.child(this.label);
    }

    @Override
    public boolean shouldRender() {
        boolean shouldRender = super.shouldRender();
        if (shouldRender) {
            HorizontalAlignment alignment = switch (this.textAlignment.value()) {
                case Left -> HorizontalAlignment.LEFT;
                case Center -> HorizontalAlignment.CENTER;
                case Right -> HorizontalAlignment.RIGHT;
            };
            this.label.shadow(this.textShadow.value());
            this.label.horizontalTextAlignment(alignment);
            this.layout.horizontalAlignment(alignment);
        }
        return shouldRender;
    }

    public void setText(String text) {
        this.label.text(Text.literal(text).withColor(this.getTextColor()));
    }

    public void setText(MutableText text) {
        this.label.text(text.withColor(this.getTextColor()));
    }

    public void setDefaultText() {
        this.label.text(this.defaultText);
    }

    public int getTextColor() {
        return 0x5ca0bf;
    }

    public enum TextAlignment {
        Left,
        Center,
        Right
    }
}
