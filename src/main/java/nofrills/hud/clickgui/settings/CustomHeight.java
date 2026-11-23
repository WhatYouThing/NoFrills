package nofrills.hud.clickgui.settings;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;

public final class CustomHeight extends FlowLayout {
    public int heightOverride;

    public CustomHeight(int height) {
        super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
        this.padding(Insets.of(5));
        this.horizontalAlignment(HorizontalAlignment.LEFT);
        this.verticalAlignment(VerticalAlignment.CENTER);
        this.heightOverride = height;
    }
}
