package nofrills.hud.clickgui;

import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;
import nofrills.hud.clickgui.components.PlainLabel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static nofrills.Main.mc;

public class Category extends FlowLayout {
    public List<Module> features;
    public ScrollContainer<FlowLayout> scroll;
    public int categoryWidth = 0;

    protected Category(String title, List<Module> children) {
        super(Sizing.content(), Sizing.content(), Algorithm.VERTICAL);
        this.margins(Insets.of(5, 0, 3, 0));
        Color color = Color.ofArgb(0xff5ca0bf);
        Color textColor = Color.ofArgb(0xffffffff);
        FlowLayout modules = Containers.verticalFlow(Sizing.content(), Sizing.content());
        this.features = new ArrayList<>(children);
        for (Module module : this.features) {
            this.categoryWidth = Math.max(this.categoryWidth, mc.textRenderer.getWidth(module.activeText.getString()) + 10);
        }
        this.features.sort(Comparator.comparing(module -> module.activeText.getString()));
        for (Module module : this.features) {
            module.horizontalSizing(Sizing.fixed(this.categoryWidth));
            modules.child(module);
        }
        ScrollContainer<FlowLayout> scroll = Containers.verticalScroll(Sizing.content(), Sizing.fill(75), modules)
                .scrollbarThiccness(2)
                .scrollbar(ScrollContainer.Scrollbar.flat(color));
        BaseComponent label = new PlainLabel(Text.literal(title))
                .color(textColor)
                .horizontalTextAlignment(HorizontalAlignment.CENTER)
                .verticalTextAlignment(VerticalAlignment.CENTER);
        ParentComponent header = Containers.verticalFlow(Sizing.fixed(this.categoryWidth), Sizing.content())
                .child(label)
                .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                .padding(Insets.of(3))
                .surface(Surface.flat(0xff5ca0bf));
        this.scroll = scroll;
        this.child(header);
        this.child(scroll);
    }
}
