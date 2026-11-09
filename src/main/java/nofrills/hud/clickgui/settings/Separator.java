package nofrills.hud.clickgui.settings;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import nofrills.hud.clickgui.components.PlainLabel;

import static nofrills.Main.mc;

public final class Separator extends FlowLayout {
    public Separator(String name) {
        super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
        this.padding(Insets.of(5));
        this.horizontalAlignment(HorizontalAlignment.CENTER);
        this.verticalAlignment(VerticalAlignment.CENTER);
        this.verticalSizing(Sizing.fixed(20));
        MutableComponent text = Component.literal(name);
        int textWidth = mc.font.width(text) / 2;
        PlainLabel label = new PlainLabel(text.withColor(0xffffff));
        label.verticalTextAlignment(VerticalAlignment.CENTER).verticalSizing(Sizing.fixed(20));
        this.surface((context, component) -> {
            int centerX = component.x() + component.width() / 2;
            int centerY = component.y() + component.height() / 2;
            context.fill(component.x(), centerY - 1, centerX - textWidth - 5, centerY + 1, 0xffffffff);
            context.fill(centerX + textWidth + 5, centerY - 1, component.x() + component.width(), centerY + 1, 0xffffffff);
        });
        this.child(label);
    }
}
