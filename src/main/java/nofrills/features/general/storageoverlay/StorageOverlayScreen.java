package nofrills.features.general.storageoverlay;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import org.jetbrains.annotations.NotNull;

public class StorageOverlayScreen extends BaseOwoScreen<FlowLayout> {

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        FlowLayout row = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
        row.positioning(Positioning.relative(50, 10));
        for (int i = 0; i < 3; i++) {
            FlowLayout layout = UIContainers.horizontalFlow(Sizing.fixed(16 * 9), Sizing.fixed(16 * 3));
            layout.surface(Surface.PANEL);
            row.child(layout);
        }
        rootComponent.child(row);
    }
}
