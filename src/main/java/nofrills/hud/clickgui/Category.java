package nofrills.hud.clickgui;

import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static nofrills.Main.mc;

public class Category extends FlowLayout {
    protected Category(String title, List<Module> children) {
        super(Sizing.content(), Sizing.content(), Algorithm.VERTICAL);
        this.margins(Insets.of(5, 0, 5, 0));
        Color color = Color.ofArgb(0xff5ca0bf);
        Color textColor = Color.ofArgb(0xffffffff);
        FlowLayout modules = Containers.verticalFlow(Sizing.content(), Sizing.content());
        int categoryWidth = 0;
        List<Module> childrenMutable = new ArrayList<>(children);
        for (Module module : childrenMutable) {
            categoryWidth = Math.max(categoryWidth, mc.textRenderer.getWidth(module.activeText.getString()) + 10);
        }
        childrenMutable.sort(Comparator.comparing(module -> module.activeText.getString()));
        for (Module module : childrenMutable) {
            module.horizontalSizing(Sizing.fixed(categoryWidth));
            modules.child(module);
        }
        ParentComponent scroll = Containers.verticalScroll(Sizing.content(), Sizing.fill(80), modules)
                .scrollbarThiccness(3)
                .scrollbar(ScrollContainer.Scrollbar.flat(color));
        BaseComponent label = Components.label(Text.literal(title))
                .color(textColor)
                .horizontalTextAlignment(HorizontalAlignment.CENTER)
                .verticalTextAlignment(VerticalAlignment.CENTER);
        ParentComponent header = Containers.verticalFlow(Sizing.fixed(categoryWidth), Sizing.content())
                .child(label)
                .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                .padding(Insets.of(3))
                .surface(Surface.flat(0xff5ca0bf));
        this.child(header);
        this.child(scroll);
    }
}
