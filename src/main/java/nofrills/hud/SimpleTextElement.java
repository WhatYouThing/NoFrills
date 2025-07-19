package nofrills.hud;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.text.Text;

public class SimpleTextElement extends HudElement {
    public Text text;
    public Text defaultText;
    public LabelComponent label;

    public SimpleTextElement(double posX, double posY, Text text) {
        super(posX, posY, Containers.verticalFlow(Sizing.content(), Sizing.content()));
        this.text = text;
        this.defaultText = text;
        this.label = Components.label(text);
        this.layout.child(this.label);
    }

    public void setText(String text) {
        this.label.text(Text.of(text));
    }

    public HorizontalAlignment getAlignment(alignment value) {
        return switch (value) {
            case Left -> HorizontalAlignment.LEFT;
            case Center -> HorizontalAlignment.CENTER;
            case Right -> HorizontalAlignment.RIGHT;
        };
    }

    public enum alignment {
        Left,
        Center,
        Right
    }
}
