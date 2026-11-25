package nofrills.hud.clickgui;

import com.mojang.blaze3d.platform.InputConstants;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import nofrills.config.Feature;
import nofrills.hud.HudElement;
import nofrills.hud.clickgui.components.PlainLabel;

public final class Module extends FlowLayout {
    public boolean active = false;
    public Feature feature;
    public HudElement element;
    public Component activeText;
    public Component inactiveText;
    public PlainLabel label;
    public SettingsScreen options;

    public Module(String name, Feature feature, String tooltip, SettingsScreen options) {
        super(Sizing.content(), Sizing.content(), Algorithm.VERTICAL);
        this.margins(Insets.of(2, 2, 0, 0));
        this.activeText = Component.literal(name).withColor(0x5ca0bf);
        this.inactiveText = Component.literal(name).withColor(0xdddddd);
        this.label = new PlainLabel(Component.literal(name));
        this.label.horizontalTextAlignment(HorizontalAlignment.LEFT).verticalTextAlignment(VerticalAlignment.CENTER);
        this.label.tooltip(Component.literal(tooltip));
        this.options = options;
        if (this.options != null) {
            this.options.setTitle(Component.literal(name).withColor(0xffffff));
        }
        this.feature = feature;
        this.toggleFeature(this.feature.isActive(), false);
        this.child(label);
    }

    public Module(String name, Feature feature, String tooltip) {
        this(name, feature, tooltip, null);
    }

    public Module(HudElement element) {
        this(element.elementLabel.getString(), element.instance, element.elementDesc.getString(), element.options);
        this.element = element;
        this.toggleFeature(element.isAdded(), false);
    }

    @Override
    public boolean onMouseDown(MouseButtonEvent click, boolean doubled) {
        if (this.isInBoundingBox(click.x() + this.x(), click.y() + this.y())) {
            if (click.button() == InputConstants.MOUSE_BUTTON_LEFT) {
                this.toggleFeature(this.element != null ? !this.element.isAdded() : !this.feature.isActive(), true);
            } else if (click.button() == InputConstants.MOUSE_BUTTON_RIGHT && this.options != null) {
                this.options.open();
            }
            return true;
        }
        return false;
    }

    private void toggleFeature(boolean active, boolean write) {
        this.label.text(active ? this.activeText : this.inactiveText);
        this.active = active;
        if (write) {
            if (this.element != null) {
                if (active && !this.element.instance.isActive()) {
                    this.element.instance.setActive(true);
                }
                this.element.added.set(active);
            } else {
                this.feature.setActive(active);
            }
        }
    }
}
