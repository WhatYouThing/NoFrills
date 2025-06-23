package nofrills.hud.clickgui;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static nofrills.Main.Config;

public class ClickGuiScreen extends BaseOwoScreen<FlowLayout> {
    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        root.surface(Surface.VANILLA_TRANSLUCENT);
        FlowLayout parent = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        ClickGuiCategory playerCategory = new ClickGuiCategory("Player", List.of(
                new ClickGuiModule("ZZZ testing", Config.keys.autoSprint),
                new ClickGuiModule("Auto Sprint", Config.keys.autoSprint).setTooltip("hello this is a tooltip"),
                new ClickGuiModule("Epic Amogus Long Module", Config.keys.autoSprint)
        ));
        ClickGuiCategory inventoryCategory = new ClickGuiCategory("Inventory", List.of(
                new ClickGuiModule("Slot Binding", Config.keys.slotBinding,
                        Containers.verticalFlow(Sizing.content(), Sizing.content())
                                .child(new ClickGuiModule("test 123", Config.keys.slotBindingBorders))
                ),
                new ClickGuiModule("Sample Text", Config.keys.autoSprint)
        ));
        parent.child(playerCategory);
        parent.child(inventoryCategory);
        root.child(Containers.horizontalScroll(Sizing.fill(100), Sizing.content(), parent));
    }
}
