package nofrills.hud.clickgui.settings;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.network.chat.Component;
import nofrills.hud.clickgui.components.PlainLabel;

public final class Description extends FlowLayout {

    public Description(String name, String description) {
        super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
        this.padding(Insets.of(5));
        this.horizontalAlignment(HorizontalAlignment.LEFT);
        this.verticalAlignment(VerticalAlignment.CENTER);
        PlainLabel label = new PlainLabel(Component.literal(name).withColor(0xffffff));
        label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
        PlainLabel desc = new PlainLabel(Component.literal(description).withColor(0xffffff));
        desc.verticalTextAlignment(VerticalAlignment.CENTER).verticalSizing(Sizing.content()).horizontalSizing(Sizing.fixed(200));
        this.child(label);
        this.child(desc);
    }
}
