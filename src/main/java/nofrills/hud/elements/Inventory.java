package nofrills.hud.elements;

import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import nofrills.config.Feature;
import nofrills.hud.HudElement;

import static nofrills.Main.mc;

public class Inventory extends HudElement {
    private final FlowLayout content;

    public Inventory() {
        super(new Feature("inventoryElement"), "Inventory Element");
        this.content = UIContainers.verticalFlow(Sizing.fixed(162), Sizing.fixed(54));
        for (int i = 0; i <= 2; i++) {
            FlowLayout container = UIContainers.horizontalFlow(Sizing.fixed(162), Sizing.fixed(18));
            for (int j = 0; j <= 8; j++) {
                ItemComponent component = UIComponents.item(ItemStack.EMPTY);
                component.showOverlay(true).margins(Insets.of(1));
                container.child(component);
            }
            this.content.child(container);
        }
        this.layout.child(this.content);
        this.options = this.getBaseSettings();
        this.setDesc("Displays the contents of your inventory.");
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
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
                for (int j = 0; j <= 8; j++) {
                    ((ItemComponent) child.children().get(j)).stack(inv.getStack(row + j));
                }
            }
        }
    }
}