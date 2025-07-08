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
import net.minecraft.text.Text;
import nofrills.hud.clickgui.components.EnumCollapsible;
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

    public static void setKeyDefault(Option.Key optionKey) {
        Config.optionForKey(optionKey).set(Config.optionForKey(optionKey).defaultValue());
    }

    private static ButtonComponent buildResetButton(Option.Key optionKey) {
        ButtonComponent button = Components.button(Text.literal("Reset").withColor(0xffffff), btn -> {
            setKeyValue(optionKey, Config.optionForKey(optionKey).defaultValue());
        });
        button.positioning(Positioning.relative(100, 0));
        button.renderer((context, btn, delta) -> {
            context.fill(btn.getX(), btn.getY(), btn.getX() + btn.getWidth(), btn.getY() + btn.getHeight(), 0xff101010);
            context.drawBorder(btn.getX(), btn.getY(), btn.getWidth(), btn.getHeight(), 0xffffffff);
        });
        return button;
    }

    private static double roundDouble(double value) {
        return BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_EVEN).doubleValue();
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
            this.child(buildResetButton(optionKey));
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
            text.text(String.valueOf((double) getKeyValue(optionKey)));
            slider.min(min).max(max).stepSize(step).horizontalSizing(Sizing.fixed(100)).verticalSizing(Sizing.fixed(20));
            slider.onChanged().subscribe(change -> {
                double value = roundDouble(slider.value());
                setKeyValue(optionKey, value);
                text.setText(String.valueOf(value));
            });
            this.child(label);
            this.child(text);
            this.child(slider);
            this.child(buildResetButton(optionKey));
        }
    }

    public static class Dropdown extends FlowLayout {
        public <T extends Enum<T>> Dropdown(String name, Class<T> values, Option.Key optionKey) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            LabelComponent label = Components.label(Text.literal(name).withColor(0xffffff));
            EnumCollapsible dropdown = new EnumCollapsible(((T) getKeyValue(optionKey)).name());
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            dropdown.surface(Surface.flat(0xff101010).and(Surface.outline(0xff5ca0bf)));
            for (T value : values.getEnumConstants()) {
                ButtonComponent button = Components.button(Text.of(value.name()), btn -> {
                    dropdown.setLabel(value.name());
                    setKeyValue(optionKey, value);
                    dropdown.toggleExpansion();
                });
                button.sizing(Sizing.content(), Sizing.fixed(12));
                button.renderer((context, btn, delta) -> {
                });
                dropdown.child(button);
            }
            this.child(label);
            this.child(dropdown);
            this.child(buildResetButton(optionKey));
        }
    }

    public static class ColorPicker extends FlowLayout {
        public Option.Key key;

        public ColorPicker(String name, boolean showAlpha, Option.Key optionKey) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.key = optionKey;
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            this.verticalAlignment(VerticalAlignment.CENTER);
            LabelComponent label = Components.label(Text.literal(name).withColor(0xffffff));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.right(5)).verticalSizing(Sizing.fixed(20));
            Color currentColor = (Color) getKeyValue(optionKey);
            FlowLayout colorDisplay = Containers.verticalFlow(Sizing.fixed(20), Sizing.fixed(20));
            colorDisplay.surface(Surface.flat(currentColor.argb())).margins(Insets.right(10));
            FlowLayout colorSliders = Containers.verticalFlow(Sizing.content(), Sizing.content());
            for (int i = 0; i <= 3; i++) {
                if (i == 3 && !showAlpha) {
                    continue;
                }
                int id = i;
                FlowLayout row = Containers.horizontalFlow(Sizing.content(), Sizing.content());
                LabelComponent colorLabel = Components.label(Text.literal(getColorLabel(id)).withColor(0xffffff));
                colorLabel.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.right(5)).verticalSizing(Sizing.fixed(20));
                FlatTextbox text = new FlatTextbox(Sizing.fixed(30));
                FlatSlider slider = new FlatSlider(0xffdddddd, 0xff5ca0bf);
                text.onChanged().subscribe(change -> {
                    try {
                        int value = Integer.parseInt(text.getText());
                        setColorValue(id, value / 255.0f);
                        slider.value(value);
                        colorDisplay.surface(Surface.flat(((Color) getKeyValue(this.key)).argb()));
                    } catch (NumberFormatException ignored) {
                    }
                });
                text.text(String.valueOf((int) (getColorValue(id) * 255.0f)));
                slider.min(0).max(255).stepSize(1).horizontalSizing(Sizing.fixed(50)).verticalSizing(Sizing.fixed(20));
                slider.onChanged().subscribe(change -> {
                    int value = (int) slider.value();
                    setColorValue(id, value / 255.0f);
                    text.setText(String.valueOf(value));
                    colorDisplay.surface(Surface.flat(((Color) getKeyValue(this.key)).argb()));
                });
                row.child(colorLabel);
                row.child(text);
                row.child(slider);
                colorSliders.child(row);
            }
            this.child(label);
            this.child(colorDisplay);
            this.child(colorSliders);
            this.child(buildResetButton(optionKey).positioning(Positioning.relative(100, 50)));
        }

        private String getColorLabel(int id) {
            return switch (id) {
                case 0 -> "R";
                case 1 -> "G";
                case 2 -> "B";
                case 3 -> "A";
                default -> "";
            };
        }

        private double getColorValue(int id) {
            Color color = (Color) getKeyValue(this.key);
            return switch (id) {
                case 0 -> color.red();
                case 1 -> color.green();
                case 2 -> color.blue();
                case 3 -> color.alpha();
                default -> 0;
            };
        }

        private void setColorValue(int id, double value) {
            Color color = (Color) getKeyValue(this.key);
            Color newColor = switch (id) { // quite scuffed but its either this or pasting the same code 4 times to build the color picker
                case 0 -> new Color((float) value, color.green(), color.blue(), color.alpha());
                case 1 -> new Color(color.red(), (float) value, color.blue(), color.alpha());
                case 2 -> new Color(color.red(), color.green(), (float) value, color.alpha());
                case 3 -> new Color(color.red(), color.green(), color.blue(), (float) value);
                default -> color;
            };
            setKeyValue(this.key, newColor);
        }
    }
}
