package nofrills.hud.clickgui;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;
import nofrills.hud.clickgui.components.FlatSlider;
import nofrills.hud.clickgui.components.FlatTextbox;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static nofrills.Main.Config;
import static nofrills.Main.mc;

public class ClickGuiSettings extends BaseOwoScreen<FlowLayout> {
    public List<FlowLayout> settings;
    public Text title = Text.empty();

    public ClickGuiSettings(List<FlowLayout> settings) {
        this.settings = settings;
    }

    public static Object getKeyValue(Option.Key optionKey) {
        return Config.optionForKey(optionKey).value();
    }

    public static void setKeyValue(Option.Key optionKey, Object value) {
        Config.optionForKey(optionKey).set(value);
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        root.surface(Surface.VANILLA_TRANSLUCENT);
        root.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        FlowLayout parent = Containers.verticalFlow(Sizing.content(), Sizing.content());
        parent.padding(Insets.of(5));
        for (FlowLayout child : this.settings) {
            parent.child(child);
        }
        root.child(parent);
    }

    @Override
    public void close() {
        mc.setScreen(new ClickGuiScreen());
    }

    public BaseOwoScreen<FlowLayout> setTitle(Text title) {
        this.title = title;
        return this;
    }

    public static class Toggle extends FlowLayout {
        public boolean active = false;
        public Option.Key optionKey;
        public Text enabledText;
        public Text disabledText;
        public ButtonComponent toggle;

        public Toggle(String name, Option.Key optionKey) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.optionKey = optionKey;
            this.enabledText = Text.literal("Enabled").withColor(0x55ff55);
            this.disabledText = Text.literal("Disabled").withColor(0xff5555);
            LabelComponent label = Components.label(Text.literal(name).withColor(0xffffff));
            this.toggle = Components.button(Text.empty(), button -> {
                boolean value = (boolean) getKeyValue(this.optionKey);
                active(!value);
            });
            this.toggle.renderer((context, button, delta) -> context.drawBorder(button.getX(), button.getY(), button.getWidth(), button.getHeight(), 0xff5ca0bf));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            this.active((boolean) getKeyValue(this.optionKey));
            this.child(label);
            this.child(this.toggle);
        }

        private void active(boolean active) {
            if (active) {
                this.toggle.setMessage(this.enabledText);
            } else {
                this.toggle.setMessage(this.disabledText);
            }
            setKeyValue(this.optionKey, active);
            this.active = active;
        }
    }

    public static class Slider extends FlowLayout {
        public Slider(String name, double min, double max, double step, Option.Key optionKey) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.CENTER);
            LabelComponent label = Components.label(Text.literal(name).withColor(0xffffff));
            FlatTextbox textArea = new FlatTextbox(Sizing.fixed(40), Sizing.fixed(20));
            FlatSlider slider = new FlatSlider(0xaaffffff, 0xff5ca0bf);
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            textArea.maxLines(1).margins(Insets.of(0, 0, 0, 5));
            textArea.onChanged().subscribe(change -> {
                try {
                    double value = Double.parseDouble(textArea.getText());
                    setKeyValue(optionKey, value);
                    slider.value(value);
                } catch (NumberFormatException ignored) {
                }
            });
            slider.min(min).max(max).stepSize(step).horizontalSizing(Sizing.fixed(100)).verticalSizing(Sizing.fixed(20));
            slider.onChanged().subscribe(change -> {
                setKeyValue(optionKey, slider.value());
                textArea.text(String.valueOf(slider.value()));
            });
            this.child(label);
            this.child(textArea);
            this.child(slider);
        }
    }
}
