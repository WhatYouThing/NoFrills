package nofrills.hud.elements;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.entity.player.PlayerInventory;
import nofrills.config.Feature;
import nofrills.hud.HudElement;

import static nofrills.Main.mc;

public class Inventory extends HudElement {
    private final FlowLayout content;

    public Inventory() {
        super(new Feature("inventoryElement"), "Inventory Element");
        this.content = Containers.verticalFlow(Sizing.fixed(162), Sizing.fixed(54));
        for (int i = 0; i <= 2; i++) {
            this.content.child(Containers.horizontalFlow(Sizing.fixed(162), Sizing.fixed(18)));
        }
        this.layout.child(this.content);
        this.options = this.getBaseSettings();
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.shouldRender()) {
            super.draw(context, mouseX, mouseY, partialTicks, delta);
        }
    }

    public void updateInventory() {
        if (mc.player != null) {
            PlayerInventory inv = mc.player.getInventory();
            for (int i = 0; i <= 2; i++) {
                int row = (i + 1) * 9;
                FlowLayout child = (FlowLayout) this.content.children().get(i);
                child.clearChildren();
                for (int slot = row; slot < row + 9; slot++) {
                    ItemComponent itemComponent = Components.item(inv.getStack(slot)).showOverlay(true);
                    itemComponent.margins(Insets.of(1));
                    child.child(itemComponent);
                }
            }
        }
    }
}
