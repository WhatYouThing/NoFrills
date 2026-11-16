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
import nofrills.hud.clickgui.components.PlainLabel;

import static nofrills.Main.mc;

public final class Module extends FlowLayout {
    public boolean active = false;
    public Feature feature;
    public Component activeText;
    public Component inactiveText;
    public PlainLabel label;
    public SettingsScreen options;

    public Module(String name, Feature feature, String tooltip) {
        this(name, feature, tooltip, null);
    }

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
        this.active(this.feature.isActive());
        this.child(label);
    }

    @Override
    public boolean onMouseDown(MouseButtonEvent click, boolean doubled) {
        if (this.isInBoundingBox(click.x() + this.x(), click.y() + this.y())) {
            if (click.button() == InputConstants.MOUSE_BUTTON_LEFT) {
                this.active(!this.feature.isActive());
            } else if (click.button() == InputConstants.MOUSE_BUTTON_RIGHT && this.options != null) {
                mc.gui.setScreen(this.options);
            }
            return true;
        }
        return false;
    }

    private void active(boolean active) {
        if (active) {
            this.label.text(this.activeText);
        } else {
            this.label.text(this.inactiveText);
        }
        this.feature.setActive(active);
        this.active = active;
    }
}
