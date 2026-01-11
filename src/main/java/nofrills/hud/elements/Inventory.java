package nofrills.hud.elements;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import nofrills.config.Feature;
import nofrills.config.SettingEnum;
import nofrills.hud.HudElement;
import nofrills.hud.clickgui.Settings;

import java.util.List;

import static nofrills.Main.mc;

public class Inventory extends HudElement {
    public final SettingEnum<HideMode> hideMode;
    private final FlowLayout content;

    public Inventory() {
        super(new Feature("inventoryElement"), "Inventory Element");
        this.content = Containers.verticalFlow(Sizing.fixed(162), Sizing.fixed(54));
        for (int i = 0; i <= 2; i++) {
            FlowLayout container = Containers.horizontalFlow(Sizing.fixed(162), Sizing.fixed(18));
            for (int j = 0; j <= 8; j++) {
                ItemComponent component = Components.item(ItemStack.EMPTY);
                component.showOverlay(true).margins(Insets.of(1));
                container.child(component);
            }
            this.content.child(container);
        }
        this.layout.child(this.content);
        this.hideMode = new SettingEnum<>(HideMode.Disabled, HideMode.class, "hideMode", this.instance);
        this.options = this.getBaseSettings(List.of(
                new Settings.Dropdown<>("Hide In Screen", this.hideMode, "Automatically hides the element while a screen is open.\n\nDisabled: The element will appear regardless of screen.\nInventory: The element will be hidden in screens that have item slots (player inventory, containers etc).\nAny: The element will be hidden if any type of screen is present.")
        ));
        this.setDesc("Displays the contents of your inventory.");
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.shouldRender()) {
            if (this.shouldHideInScreen() && !this.isEditingHud()) {
                return;
            }
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

    public boolean shouldHideInScreen() {
        return switch (this.hideMode.value()) {
            case Disabled -> false;
            case Inventory -> mc.currentScreen instanceof HandledScreen<?>;
            case Any -> mc.currentScreen != null;
        };
    }

    public enum HideMode {
        Disabled,
        Inventory,
        Any
    }
}