package nofrills.hud.clickgui;

import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;

import java.util.List;

public class ClickGuiCategory extends FlowLayout {
    protected ClickGuiCategory(String title, List<ClickGuiModule> children) {
        super(Sizing.content(), Sizing.content(), Algorithm.VERTICAL);
        Color color = new Color(0.25f, 0.25f, 0.52f, 0.67f);
        BaseComponent label = Components.label(Text.literal(title))
                .color(color)
                .horizontalTextAlignment(HorizontalAlignment.CENTER)
                .verticalTextAlignment(VerticalAlignment.CENTER)
                .margins(Insets.of(10));
        this.child(label);
        this.margins(Insets.of(5));
        this.padding(Insets.of(3));
        ParentComponent scroll = Containers.verticalScroll(Sizing.content(), Sizing.fill(), Containers.verticalFlow(Sizing.content(), Sizing.content())
                        .child(Components.label(Text.literal("Item 1")))
                        .child(Components.label(Text.literal("Item 2")))
                        .child(Components.label(Text.literal("Item 2.1")))
                        .child(Components.label(Text.literal("Item 2.2")))
                        .child(Components.label(Text.literal("Item 2.3")))
                        .child(Components.label(Text.literal("Item 2.4")))
                        .child(Components.label(Text.literal("Item 3"))))
                .scrollbarThiccness(3)
                .scrollbar(ScrollContainer.Scrollbar.flat(color))
                .padding(Insets.of(10))
                .surface(Surface.flat(0xaa000000));
    }
}
