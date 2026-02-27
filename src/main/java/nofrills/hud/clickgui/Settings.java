package nofrills.hud.clickgui;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.base.BaseUIComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import nofrills.config.*;
import nofrills.features.general.CommandKeybinds;
import nofrills.hud.ColorPickerScreen;
import nofrills.hud.clickgui.components.*;
import nofrills.misc.Rendering;
import nofrills.misc.Utils;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static nofrills.Main.mc;

public class Settings extends BaseOwoScreen<FlowLayout> {
    public static final ButtonComponent.Renderer buttonRenderer = (context, button, delta) -> {
        context.fill(button.getX(), button.getY(), button.getX() + button.getWidth(), button.getY() + button.getHeight(), 0xff101010);
        Rendering.drawBorder(context, button.getX(), button.getY(), button.getWidth(), button.getHeight(), 0xff5ca0bf);
    };
    public static final ButtonComponent.Renderer buttonRendererWhite = (context, button, delta) -> {
        context.fill(button.getX(), button.getY(), button.getX() + button.getWidth(), button.getY() + button.getHeight(), 0xff101010);
        Rendering.drawBorder(context, button.getX(), button.getY(), button.getWidth(), button.getHeight(), 0xffffffff);
    };
    public List<FlowLayout> settings;
    public Text title = Text.empty();
    public ScrollContainer<FlowLayout> scroll;

    public Settings(List<FlowLayout> settings) {
        this.settings = settings;
        for (FlowLayout setting : this.settings) {
            if (setting instanceof ColorPicker colorPicker) {
                colorPicker.previous = this;
            }
        }
    }

    public Settings(FlowLayout... settings) {
        this(List.of(settings));
    }

    private static ButtonComponent buildResetButton(Consumer<ButtonComponent> onPress) {
        ButtonComponent button = UIComponents.button(Text.literal("Reset").withColor(0xffffff), onPress);
        button.positioning(Positioning.relative(100, 0));
        button.renderer(buttonRendererWhite);
        return button;
    }

    private static double roundDouble(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }

    private static int getSettingsHeight(List<UIComponent> children) {
        int height = 0;
        for (UIComponent child : children) {
            int childHeight = switch (child) {
                case Description description -> 10 + ((PlainLabel) description.children().getLast()).getTextHeight();
                case Separator ignored -> 20;
                case CommandKeybinds.Setting ignored -> 51;
                default -> 30;
            };
            height += childHeight;
        }
        return (int) Math.clamp(height, 30, mc.getWindow().getScaledHeight() * 0.8);
    }

    private static boolean isBinding(List<FlowLayout> settings, int button) {
        for (FlowLayout setting : settings) {
            for (UIComponent child : setting.children()) {
                if (findKeybindButton(child, button)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean findKeybindButton(UIComponent child, int button) {
        if (child instanceof KeybindButton keybind) {
            if (keybind.isBinding) {
                keybind.bind(button);
                return true;
            }
        } else if (child instanceof FlowLayout layout) {
            for (UIComponent layoutChild : layout.children()) {
                if (findKeybindButton(layoutChild, button)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (isBinding(this.settings, input.key())) {
            return true;
        }
        if (input.key() == GLFW.GLFW_KEY_PAGE_UP || input.key() == GLFW.GLFW_KEY_PAGE_DOWN) {
            this.scroll.onMouseScroll(0, 0, input.key() == GLFW.GLFW_KEY_PAGE_UP ? 4 : -4);
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.scroll.onMouseScroll(0, 0, verticalAmount * 2);
        return true;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (isBinding(this.settings, click.button())) {
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        root.surface(Surface.VANILLA_TRANSLUCENT);
        root.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        FlowLayout parent = UIContainers.verticalFlow(Sizing.content(), Sizing.content());
        parent.padding(Insets.of(5));
        Color textColor = Color.ofArgb(0xffffffff);
        FlowLayout settings = UIContainers.verticalFlow(Sizing.content(), Sizing.content());
        settings.surface(Surface.flat(0xaa000000)).alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        int width = 300;
        List<FlowLayout> optionsMutable = new ArrayList<>(this.settings);
        for (FlowLayout option : optionsMutable) {
            option.horizontalSizing(Sizing.fixed(width));
            settings.child(option);
        }
        this.scroll = UIContainers.verticalScroll(Sizing.content(), Sizing.fixed(getSettingsHeight(settings.children())), settings);
        this.scroll.scrollbarThiccness(2).scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xffffffff)));
        BaseUIComponent label = new PlainLabel(this.title)
                .color(textColor)
                .horizontalTextAlignment(HorizontalAlignment.CENTER)
                .verticalTextAlignment(VerticalAlignment.CENTER);
        ParentUIComponent header = UIContainers.verticalFlow(Sizing.fixed(width), Sizing.content())
                .child(label)
                .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                .padding(Insets.of(3))
                .surface(Surface.flat(0xff5ca0bf));
        parent.child(header);
        parent.child(this.scroll);
        root.child(parent);
    }

    @Override
    public void close() {
        mc.setScreen(new ClickGui());
    }

    public Settings setTitle(Text title) {
        this.title = title;
        return this;
    }

    public static class Toggle extends FlowLayout {
        public SettingBool setting;
        public ToggleButton toggle;

        public Toggle(String name, SettingBool setting, String tooltip) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            this.setting = setting;
            PlainLabel label = new PlainLabel(Text.literal(name).withColor(0xffffff));
            label.tooltip(Text.literal(tooltip));
            this.toggle = new ToggleButton(this.setting.value());
            this.toggle.onToggled().subscribe(value -> this.setting.set(value));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            this.child(label);
            this.child(this.toggle);
            this.child(buildResetButton(btn -> {
                this.setting.reset();
                this.toggle.setToggle(this.setting.value());
            }));
        }
    }

    public static class SliderDouble extends FlowLayout {
        public SettingDouble setting;

        public SliderDouble(String name, double min, double max, double step, SettingDouble setting, String tooltip) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            this.setting = setting;
            PlainLabel label = new PlainLabel(Text.literal(name).withColor(0xffffff));
            FlatTextbox text = new FlatTextbox(Sizing.fixed(50));
            FlatSlider slider = new FlatSlider(0xffdddddd, 0xff5ca0bf);
            slider.min(min).max(max).stepSize(step).horizontalSizing(Sizing.fixed(100)).verticalSizing(Sizing.fixed(20));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            label.tooltip(Text.literal(tooltip));
            text.onChanged().subscribe(change -> {
                Optional<Double> value = Utils.parseDouble(text.getText());
                if (value.isPresent()) {
                    this.setting.set(value.get());
                    slider.value(value.get());
                }
            });
            text.text(String.valueOf(this.setting.value()));
            slider.onChanged().subscribe(change -> {
                double value = roundDouble(slider.value());
                this.setting.set(value);
                text.setText(String.valueOf(value));
            });
            this.child(label);
            this.child(text);
            this.child(slider);
            this.child(buildResetButton(btn -> {
                this.setting.reset();
                text.setText(String.valueOf(roundDouble(this.setting.value())));
            }));
        }
    }

    public static class SliderInt extends FlowLayout {
        public SettingInt setting;

        public SliderInt(String name, int min, int max, int step, SettingInt setting, String tooltip) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            this.setting = setting;
            PlainLabel label = new PlainLabel(Text.literal(name).withColor(0xffffff));
            FlatTextbox text = new FlatTextbox(Sizing.fixed(50));
            FlatSlider slider = new FlatSlider(0xffdddddd, 0xff5ca0bf);
            slider.min(min).max(max).stepSize(step).horizontalSizing(Sizing.fixed(100)).verticalSizing(Sizing.fixed(20));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            label.tooltip(Text.literal(tooltip));
            text.onChanged().subscribe(change -> {
                Optional<Integer> value = Utils.parseInt(text.getText());
                if (value.isPresent()) {
                    this.setting.set(value.get());
                    slider.value(value.get());
                }
            });
            text.text(String.valueOf(this.setting.value()));
            slider.onChanged().subscribe(change -> {
                int value = (int) slider.value();
                this.setting.set(value);
                text.setText(String.valueOf(value));
            });
            this.child(label);
            this.child(text);
            this.child(slider);
            this.child(buildResetButton(btn -> {
                this.setting.reset();
                text.setText(String.valueOf(this.setting.value()));
            }));
        }
    }

    public static class Dropdown<T extends Enum<T>> extends FlowLayout {
        public SettingEnum<T> setting;

        public Dropdown(String name, SettingEnum<T> setting, String tooltip) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            this.setting = setting;
            PlainLabel label = new PlainLabel(Text.literal(name).withColor(0xffffff));
            EnumCollapsible dropdown = new EnumCollapsible(this.setting.value().name());
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            label.tooltip(Text.literal(tooltip));
            dropdown.surface(Surface.flat(0xff101010).and(Surface.outline(0xff5ca0bf)));
            for (T value : this.setting.values) {
                ButtonComponent button = UIComponents.button(Text.of(value.name()), btn -> {
                    dropdown.setLabel(value.name());
                    this.setting.set(value);
                    dropdown.toggleExpansion();
                });
                button.sizing(Sizing.content(), Sizing.fixed(12));
                button.renderer((context, btn, delta) -> {
                });
                dropdown.child(button);
            }
            this.child(label);
            this.child(dropdown);
            this.child(buildResetButton(btn -> {
                this.setting.reset();
                dropdown.setLabel(this.setting.value().name());
            }));
        }
    }

    public static class ColorPicker extends FlowLayout {
        public SettingColor setting;
        public Screen previous;

        public ColorPicker(String name, boolean alpha, SettingColor setting, String tooltip) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            this.verticalAlignment(VerticalAlignment.CENTER);
            this.setting = setting;
            PlainLabel label = new PlainLabel(Text.literal(name).withColor(0xffffff));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.right(5)).verticalSizing(Sizing.fixed(20));
            label.tooltip(Text.literal(tooltip));
            FlowLayout colorDisplay = UIContainers.verticalFlow(Sizing.fixed(20), Sizing.fixed(20));
            colorDisplay.surface((context, component) -> context.fill(component.x(), component.y(), component.x() + component.width(), component.y() + component.height(), this.setting.value().argb)).margins(Insets.right(5));
            this.child(buildResetButton(btn -> this.setting.reset()).positioning(Positioning.relative(100, 50)));
            ButtonComponent editButton = UIComponents.button(Text.literal("Edit Color"), (btn) -> {
                ColorPickerScreen pickerScreen = ColorPickerScreen.build(this.setting, this.previous);
                pickerScreen.setTitle(Text.literal(!Utils.toLower(name).endsWith(" color") ? name + " Color" : name));
                mc.setScreen(pickerScreen);
            });
            editButton.horizontalSizing(Sizing.fixed(60));
            editButton.renderer(buttonRenderer);
            this.child(label);
            this.child(colorDisplay);
            this.child(editButton);
        }
    }

    public static class Separator extends FlowLayout {
        public Separator(String name) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.CENTER);
            this.verticalAlignment(VerticalAlignment.CENTER);
            this.verticalSizing(Sizing.fixed(20));
            MutableText text = Text.literal(name);
            int textWidth = mc.textRenderer.getWidth(text) / 2;
            PlainLabel label = new PlainLabel(text.withColor(0xffffff));
            label.verticalTextAlignment(VerticalAlignment.CENTER).verticalSizing(Sizing.fixed(20));
            this.surface((context, component) -> {
                int centerX = component.x() + component.width() / 2;
                int centerY = component.y() + component.height() / 2;
                context.fill(component.x(), centerY - 1, centerX - textWidth - 5, centerY + 1, 0xffffffff);
                context.fill(centerX + textWidth + 5, centerY - 1, component.x() + component.width(), centerY + 1, 0xffffffff);
            });
            this.child(label);
        }
    }

    public static class Description extends FlowLayout {

        public Description(String name, String description) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            this.verticalAlignment(VerticalAlignment.CENTER);
            PlainLabel label = new PlainLabel(Text.literal(name).withColor(0xffffff));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            PlainLabel desc = new PlainLabel(Text.literal(description).withColor(0xffffff));
            desc.verticalTextAlignment(VerticalAlignment.CENTER).verticalSizing(Sizing.content()).horizontalSizing(Sizing.fixed(200));
            this.child(label);
            this.child(desc);
        }
    }

    public static class TextInput extends FlowLayout {
        public SettingString setting;

        public TextInput(String name, SettingString setting, String tooltip) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            this.setting = setting;
            PlainLabel label = new PlainLabel(Text.literal(name).withColor(0xffffff));
            FlatTextbox text = new FlatTextbox(Sizing.fixed(150));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            label.tooltip(Text.literal(tooltip));
            text.onChanged().subscribe(change -> this.setting.set(text.getText()));
            text.text(String.valueOf(this.setting.value()));
            this.child(label);
            this.child(text);
            this.child(buildResetButton(btn -> {
                this.setting.reset();
                text.setText(String.valueOf(this.setting.value()));
            }));
        }
    }

    public static class Keybind extends FlowLayout {
        public SettingKeybind setting;
        public KeybindButton button;

        public Keybind(String name, SettingKeybind setting, String tooltip) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            this.setting = setting;
            PlainLabel label = new PlainLabel(Text.literal(name).withColor(0xffffff));
            label.tooltip(Text.literal(tooltip));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            this.button = new KeybindButton();
            this.button.bind(this.setting.value());
            this.button.onBound().subscribe(keycode -> this.setting.set(keycode));
            this.child(label);
            this.child(this.button);
            this.child(buildResetButton(btn -> {
                this.setting.reset();
                this.button.bind(this.setting.value());
            }));
        }
    }

    public static class BigButton extends FlowLayout {
        public ButtonComponent button;

        public BigButton(String name, Consumer<ButtonComponent> onPress) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.CENTER);
            this.button = UIComponents.button(Text.literal(name).withColor(0xffffff), onPress);
            this.button.horizontalSizing(Sizing.fixed(290));
            this.button.renderer(buttonRenderer);
            this.child(this.button);
        }
    }

    public static class DoubleInput extends FlowLayout {
        public SettingDouble setting;

        public DoubleInput(String name, SettingDouble setting, String tooltip) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            this.setting = setting;
            PlainLabel label = new PlainLabel(Text.literal(name).withColor(0xffffff));
            FlatTextbox text = new FlatTextbox(Sizing.fixed(150));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            label.tooltip(Text.literal(tooltip));
            text.onChanged().subscribe(change -> Utils.parseDouble(change).ifPresent(value -> this.setting.set(value)));
            text.text(String.valueOf(this.setting.value()));
            this.child(label);
            this.child(text);
            this.child(buildResetButton(btn -> {
                this.setting.reset();
                text.setText(String.valueOf(this.setting.value()));
            }));
        }
    }
}
