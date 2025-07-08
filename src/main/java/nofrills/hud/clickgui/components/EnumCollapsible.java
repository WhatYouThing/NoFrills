package nofrills.hud.clickgui.components;

import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.CollapsibleContainer;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.text.Text;

public class EnumCollapsible extends CollapsibleContainer {

    public EnumCollapsible(String title) {
        super(Sizing.content(), Sizing.content(), Text.empty(), false);
        this.contentLayout.padding(Insets.left(10));
        setLabel(title); // removes underline
    }

    public void setLabel(String label) {
        ((LabelComponent) this.titleLayout.children().getFirst()).text(Text.of(label));
    }
}
