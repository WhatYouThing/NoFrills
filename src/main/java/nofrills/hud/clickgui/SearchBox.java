package nofrills.hud.clickgui;

import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;
import nofrills.hud.clickgui.components.FlatTextbox;
import nofrills.hud.clickgui.components.PlainLabel;

public class SearchBox extends FlowLayout {
    public FlatTextbox input;

    public SearchBox() {
        super(Sizing.content(), Sizing.content(), Algorithm.VERTICAL);
        this.margins(Insets.of(5, 0, 5, 5));
        Color textColor = Color.ofArgb(0xffffffff);
        this.input = new FlatTextbox(Sizing.fixed(100));
        this.input.margins(Insets.of(1));
        FlowLayout layout = Containers.verticalFlow(Sizing.fixed(108), Sizing.content());
        layout.alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        layout.surface(Surface.flat(0xaa000000));
        layout.child(this.input);
        BaseComponent label = new PlainLabel(Text.literal("Search"))
                .color(textColor)
                .horizontalTextAlignment(HorizontalAlignment.CENTER)
                .verticalTextAlignment(VerticalAlignment.CENTER);
        ParentComponent header = Containers.verticalFlow(Sizing.fixed(108), Sizing.content())
                .child(label)
                .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                .padding(Insets.of(3))
                .surface(Surface.flat(0xff5ca0bf));
        this.child(header);
        this.child(layout);
    }
}
