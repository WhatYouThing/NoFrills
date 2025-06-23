package nofrills.hud.clickgui;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.Config;

public class ClickGuiModule extends FlowLayout {
    public boolean expanded = false;
    public boolean active = false;
    public Option.Key optionKey;
    public Text activeText;
    public Text inactiveText;
    public LabelComponent label;
    public FlowLayout options;
    public List<Component> children;

    protected ClickGuiModule(String name, Option.Key optionKey) {
        this(name, optionKey, Containers.verticalFlow(Sizing.content(), Sizing.content()));
    }

    protected ClickGuiModule(String name, Option.Key optionKey, FlowLayout options) {
        super(Sizing.content(), Sizing.content(), Algorithm.VERTICAL);
        this.surface(Surface.flat(0xaa000000));
        this.activeText = Text.literal(name).withColor(0x5ca0bf);
        this.inactiveText = Text.literal(name).withColor(0xdddddd);
        this.label = Components.label(Text.literal(name));
        this.label.horizontalTextAlignment(HorizontalAlignment.LEFT).verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(5));
        this.child(label);
        this.options = options;
        this.children = new ArrayList<>();
        this.optionKey = optionKey;
        this.active(this.getKeyValue());
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        if (mouseY <= (double) this.label.fullSize().height()) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
                this.active(!this.getKeyValue());
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_2) {
                this.expanded(!this.expanded);
            }
            return true;
        }
        return false;
    }

    private boolean getKeyValue() {
        return (Boolean) Config.optionForKey(this.optionKey).value();
    }

    private void setKeyValue(boolean value) {
        Config.optionForKey(this.optionKey).set(value);
    }

    private void expanded(boolean expanded) {
        if (expanded) {
            this.options.children(this.children);
        } else {
            this.children = new ArrayList<>(this.options.children());
            this.options.clearChildren();
        }
        this.expanded = expanded;
    }

    private void active(boolean active) {
        if (active) {
            this.label.text(this.activeText);
        } else {
            this.label.text(this.inactiveText);
        }
        setKeyValue(active);
        this.active = active;
    }

    public ClickGuiModule setTooltip(String tooltip) {
        this.label.tooltip(Text.literal(tooltip));
        return this;
    }
}
