package nofrills.hud.clickgui;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import static nofrills.Main.Config;
import static nofrills.Main.mc;

public class ClickGuiModule extends FlowLayout {
    public boolean active = false;
    public Option.Key optionKey;
    public Text activeText;
    public Text inactiveText;
    public LabelComponent label;
    public BaseOwoScreen<FlowLayout> options;

    protected ClickGuiModule(String name, Option.Key optionKey) {
        this(name, optionKey, null);
    }

    protected ClickGuiModule(String name, Option.Key optionKey, BaseOwoScreen<FlowLayout> options) {
        super(Sizing.content(), Sizing.content(), Algorithm.VERTICAL);
        this.surface(Surface.flat(0xaa000000));
        this.activeText = Text.literal(name).withColor(0x5ca0bf);
        this.inactiveText = Text.literal(name).withColor(0xdddddd);
        this.label = Components.label(Text.literal(name));
        this.label.horizontalTextAlignment(HorizontalAlignment.LEFT).verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(5));
        this.child(label);
        this.options = options;
        this.optionKey = optionKey;
        this.active(this.getKeyValue());
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        if (mouseY <= (double) this.label.fullSize().height()) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
                this.active(!this.getKeyValue());
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_2 && this.options != null) {
                mc.setScreen(this.options);
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
