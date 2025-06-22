package nofrills.hud.clickgui;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;

public class ClickGuiModule extends FlowLayout {
    protected ClickGuiModule(String name) {
        super(Sizing.content(), Sizing.content(), Algorithm.VERTICAL);
    }
}
