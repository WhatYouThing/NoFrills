package nofrills.hud;

import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.text.Text;
import nofrills.config.SettingBool;
import nofrills.config.SettingEnum;
import nofrills.hud.clickgui.components.PlainLabel;

public class SimpleTextElement extends HudElement {
    public Text text;
    public Text defaultText;
    public LabelComponent label;

    public SimpleTextElement(Text text) {
        super(Containers.verticalFlow(Sizing.content(), Sizing.content()));
        this.text = text;
        this.defaultText = text;
        this.label = new PlainLabel(text);
        this.label.margins(Insets.of(2));
        this.layout.child(this.label);
    }

    public void setText(String text) {
        this.label.text(Text.of(text));
    }

    public void updateAlignment(SettingEnum<alignment> setting) {
        HorizontalAlignment alignment = switch (setting.value()) {
            case Left -> HorizontalAlignment.LEFT;
            case Center -> HorizontalAlignment.CENTER;
            case Right -> HorizontalAlignment.RIGHT;
        };
        this.label.horizontalTextAlignment(alignment);
        this.layout.horizontalAlignment(alignment);
    }

    public void updateShadow(SettingBool setting) {
        this.label.shadow(setting.value());
    }

    public enum alignment {
        Left,
        Center,
        Right
    }
}
