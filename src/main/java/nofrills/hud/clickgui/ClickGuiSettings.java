package nofrills.hud.clickgui;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import nofrills.hud.clickgui.components.FlatSlider;
import nofrills.hud.clickgui.components.FlatTextbox;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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
        Color color = Color.ofArgb(0xff5ca0bf);
        Color textColor = Color.ofArgb(0xffffffff);
        FlowLayout settings = Containers.verticalFlow(Sizing.content(), Sizing.content());
        settings.surface(Surface.flat(0xaa000000)).alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        int width = 300;
        List<FlowLayout> optionsMutable = new ArrayList<>(this.settings);
        for (FlowLayout option : optionsMutable) {
            option.horizontalSizing(Sizing.fixed(width));
            settings.child(option);
        }
        ParentComponent scroll = Containers.verticalScroll(Sizing.content(), Sizing.fixed(Math.clamp(30L * settings.children().size(), 30, 400)), settings)
                .scrollbarThiccness(3)
                .scrollbar(ScrollContainer.Scrollbar.flat(color));
        BaseComponent label = Components.label(this.title)
                .color(textColor)
                .horizontalTextAlignment(HorizontalAlignment.CENTER)
                .verticalTextAlignment(VerticalAlignment.CENTER);
        ParentComponent header = Containers.verticalFlow(Sizing.fixed(width), Sizing.content())
                .child(label)
                .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                .padding(Insets.of(3))
                .surface(Surface.flat(0xff5ca0bf));
        parent.child(header);
        parent.child(scroll);
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
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            this.optionKey = optionKey;
            this.enabledText = Text.literal("Enabled").withColor(0x55ff55);
            this.disabledText = Text.literal("Disabled").withColor(0xff5555);
            LabelComponent label = Components.label(Text.literal(name).withColor(0xffffff));
            this.toggle = Components.button(Text.empty(), button -> {
                boolean value = (boolean) getKeyValue(this.optionKey);
                active(!value);
            });
            this.toggle.renderer((context, button, delta) -> {
                context.fill(button.getX(), button.getY(), button.getX() + button.getWidth(), button.getY() + button.getHeight(), 0xff101010);
                context.drawBorder(button.getX(), button.getY(), button.getWidth(), button.getHeight(), 0xff5ca0bf);
            });
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
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            LabelComponent label = Components.label(Text.literal(name).withColor(0xffffff));
            FlatTextbox text = new FlatTextbox(Sizing.fixed(50));
            FlatSlider slider = new FlatSlider(0xffdddddd, 0xff5ca0bf);
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            text.onChanged().subscribe(change -> {
                try {
                    double value = Double.parseDouble(text.getText());
                    setKeyValue(optionKey, value);
                    slider.value(value);
                } catch (NumberFormatException ignored) {
                }
            });
            slider.min(min).max(max).stepSize(step).horizontalSizing(Sizing.fixed(100)).verticalSizing(Sizing.fixed(20));
            slider.onChanged().subscribe(change -> {
                double value = BigDecimal.valueOf(slider.value()).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                setKeyValue(optionKey, value);
                text.text(String.valueOf(value));
            });
            this.child(label);
            this.child(text);
            this.child(slider);
        }
    }
}
