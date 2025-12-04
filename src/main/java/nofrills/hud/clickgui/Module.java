package nofrills.hud.clickgui;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.Click;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.hud.clickgui.components.PlainLabel;
import org.lwjgl.glfw.GLFW;

import static nofrills.Main.mc;

public class Module extends FlowLayout {
    public boolean active = false;
    public Feature feature;
    public Text activeText;
    public Text inactiveText;
    public PlainLabel label;
    public Settings options;

    public Module(String name, Feature feature, String tooltip) {
        this(name, feature, tooltip, null);
    }

    public Module(String name, Feature feature, String tooltip, Settings options) {
        super(Sizing.content(), Sizing.content(), Algorithm.VERTICAL);
        this.activeText = Text.literal(name).withColor(0x5ca0bf);
        this.inactiveText = Text.literal(name).withColor(0xdddddd);
        this.label = new PlainLabel(Text.literal(name));
        this.label.horizontalTextAlignment(HorizontalAlignment.LEFT).verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(5));
        this.label.tooltip(Text.literal(tooltip));
        this.child(label);
        this.options = options;
        if (this.options != null) {
            this.options.setTitle(Text.literal(name).withColor(0xffffff));
        }
        this.feature = feature;
        this.active(this.feature.isActive());
    }

    @Override
    public boolean onMouseDown(Click click, boolean doubled) {
        if (click.y() <= (double) this.label.fullSize().height()) {
            if (click.button() == GLFW.GLFW_MOUSE_BUTTON_1) {
                this.active(!this.feature.isActive());
            } else if (click.button() == GLFW.GLFW_MOUSE_BUTTON_2 && this.options != null) {
                mc.setScreen(this.options);
            }
            return true;
        }
        return false;
    }

    private void active(boolean active) {
        if (active) {
            this.surface(Surface.flat(0xaa101010).and((context, component) -> {
                context.fill(component.x(), component.y(), component.x() + 2, component.y() + component.height(), 0xffffffff);
            }));
            this.label.text(this.activeText);
        } else {
            this.surface(Surface.flat(0xaa000000));
            this.label.text(this.inactiveText);
        }
        this.feature.setActive(active);
        this.active = active;
    }
}
