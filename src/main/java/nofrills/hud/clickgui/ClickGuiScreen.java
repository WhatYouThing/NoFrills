package nofrills.hud.clickgui;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class ClickGuiScreen extends BaseOwoScreen<FlowLayout> {
    private static final ButtonComponent.Renderer buttonOn = ButtonComponent.Renderer.flat(0xaaaaaa, 0x3e3ea9, 0x000000);
    private static final ButtonComponent.Renderer buttonOff = ButtonComponent.Renderer.flat(0x7a7a7a, 0x3e3ea9, 0x000000);

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        root.surface(Surface.VANILLA_TRANSLUCENT);
        FlowLayout parent = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        ParentComponent playerCategory = Containers.collapsible(Sizing.content(), Sizing.content(), Text.literal("Player"), true)
                .margins(Insets.of(5))
                .padding(Insets.of(2))
                .surface(Surface.flat(0xaa000000));
        parent.child(playerCategory);
        root.child(Containers.horizontalScroll(Sizing.fill(100), Sizing.content(), parent));
    }

    private static class Toggles {
    }
}
