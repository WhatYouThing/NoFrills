package nofrills.hud.clickgui;

import com.mojang.blaze3d.platform.InputConstants;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import nofrills.config.*;
import nofrills.hud.ColorPickerScreen;
import nofrills.hud.clickgui.components.*;
import nofrills.misc.MutableReference;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.Utils;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    public Component title = Component.empty();
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

    protected static ButtonComponent buildResetButton(Consumer<ButtonComponent> onPress) {
        ButtonComponent button = UIComponents.button(Component.literal("Reset").withColor(0xffffff), onPress);
        button.positioning(Positioning.relative(100, 0));
        button.renderer(buttonRendererWhite);
        return button;
    }

    protected static double roundDouble(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }

    protected static int getSettingsHeight(List<UIComponent> children) {
        int height = 0;
        for (UIComponent child : children) {
            int childHeight = switch (child) {
                case Description description -> 10 + ((PlainLabel) description.children().getLast()).getTextHeight();
                case Separator ignored -> 20;
                case CustomHeight customHeight -> customHeight.heightOverride;
                default -> 30;
            };
            height += childHeight;
        }
        return (int) Math.clamp(height, 30, mc.getWindow().getGuiScaledHeight() * 0.8);
    }

    protected static boolean isBinding(List<FlowLayout> settings, int button) {
        for (FlowLayout setting : settings) {
            for (UIComponent child : setting.children()) {
                if (findKeybindButton(child, button)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected static boolean findKeybindButton(UIComponent child, int button) {
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
    public boolean keyPressed(KeyEvent input) {
        if (isBinding(this.settings, input.key())) {
            return true;
        }
        if (input.key() == InputConstants.KEY_PAGEUP || input.key() == InputConstants.KEY_PAGEDOWN) {
            this.scroll.onMouseScroll(0, 0, input.key() == InputConstants.KEY_PAGEUP ? 4 : -4);
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
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
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
        PlainLabel label = new PlainLabel(this.title);
        label.color(textColor).horizontalTextAlignment(HorizontalAlignment.CENTER).verticalTextAlignment(VerticalAlignment.CENTER);
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
    public void onClose() {
        mc.gui.setScreen(new ClickGui());
    }

    public Settings setTitle(Component title) {
        this.title = title;
        return this;
    }

    public static final class Toggle extends FlowLayout {

        public Toggle(String name, boolean currentValue, boolean defaultValue, String tooltip, Consumer<Boolean> updateCallback) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            PlainLabel label = new PlainLabel(Component.literal(name).withColor(0xffffff));
            label.tooltip(Component.literal(tooltip));
            ToggleButton toggle = new ToggleButton(currentValue);
            toggle.onToggled().subscribe(updateCallback::accept);
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            this.child(label);
            this.child(toggle);
            this.child(buildResetButton(_ -> {
                updateCallback.accept(defaultValue);
                toggle.setToggle(defaultValue);
            }));
        }

        public Toggle(String name, SettingBool setting, String tooltip) {
            this(name, setting.value(), setting.getDefault().getAsBoolean(), tooltip, setting::set);
        }
    }

    public static final class SliderDouble extends FlowLayout {

        public SliderDouble(String name, double min, double max, double step, double currentValue, double defaultValue, String tooltip, Consumer<Double> updateCallback) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            PlainLabel label = new PlainLabel(Component.literal(name).withColor(0xffffff));
            FlatTextbox text = new FlatTextbox(Sizing.fixed(50));
            FlatSlider slider = new FlatSlider(0xffdddddd, 0xff5ca0bf);
            slider.min(min).max(max).stepSize(step).value(currentValue).horizontalSizing(Sizing.fixed(100)).verticalSizing(Sizing.fixed(20));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            label.tooltip(Component.literal(tooltip));
            text.onChanged().subscribe(change -> {
                Utils.parseDouble(change).ifPresent(d -> {
                    double value = roundDouble(d);
                    updateCallback.accept(value);
                    slider.value(value);
                });
            });
            text.text(String.valueOf(currentValue));
            slider.onChanged().subscribe(change -> {
                double value = roundDouble(change);
                updateCallback.accept(value);
                text.setValue(String.valueOf(value));
            });
            this.child(label);
            this.child(text);
            this.child(slider);
            this.child(buildResetButton(btn -> {
                updateCallback.accept(defaultValue);
                text.setValue(String.valueOf(roundDouble(defaultValue)));
            }));
        }

        public SliderDouble(String name, double min, double max, double step, SettingDouble setting, String tooltip) {
            this(name, min, max, step, setting.value(), setting.getDefault().getAsDouble(), tooltip, setting::set);
        }
    }

    public static final class SliderInt extends FlowLayout {

        public SliderInt(String name, int min, int max, int step, int currentValue, int defaultValue, String tooltip, Consumer<Integer> updateCallback) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            PlainLabel label = new PlainLabel(Component.literal(name).withColor(0xffffff));
            FlatTextbox text = new FlatTextbox(Sizing.fixed(50));
            FlatSlider slider = new FlatSlider(0xffdddddd, 0xff5ca0bf);
            slider.min(min).max(max).stepSize(step).value(currentValue).horizontalSizing(Sizing.fixed(100)).verticalSizing(Sizing.fixed(20));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            label.tooltip(Component.literal(tooltip));
            text.onChanged().subscribe(change -> {
                Utils.parseInt(change).ifPresent(i -> {
                    updateCallback.accept(i);
                    slider.value(i);
                });
            });
            text.text(String.valueOf(currentValue));
            slider.onChanged().subscribe(change -> {
                int value = (int) change;
                updateCallback.accept(value);
                text.setValue(String.valueOf(value));
            });
            this.child(label);
            this.child(text);
            this.child(slider);
            this.child(buildResetButton(_ -> {
                updateCallback.accept(defaultValue);
                text.setValue(String.valueOf(defaultValue));
            }));
        }

        public SliderInt(String name, int min, int max, int step, SettingInt setting, String tooltip) {
            this(name, min, max, step, setting.value(), setting.getDefault().getAsInt(), tooltip, setting::set);
        }
    }

    public static final class EnumToggle<T extends Enum<T>> extends FlowLayout {

        public EnumToggle(String name, T currentValue, T defaultValue, Class<T> values, String tooltip, Consumer<T> updateCallback) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            PlainLabel label = new PlainLabel(Component.literal(name).withColor(0xffffff));
            EnumButton<T> button = new EnumButton<>(currentValue.name(), defaultValue, values);
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            label.tooltip(Component.literal(tooltip));
            button.setMessage(Component.literal(currentValue.toString()));
            T[] constants = values.getEnumConstants();
            button.onChanged().subscribe(value -> updateCallback.accept(Utils.toEnumConstant(value, constants, defaultValue)));
            int maxWidth = Arrays.stream(constants).mapToInt(constant -> mc.font.width(constant.toString())).max().orElse(0);
            button.horizontalSizing(Sizing.fixed(maxWidth + 10));
            this.child(label);
            this.child(button);
            this.child(buildResetButton(_ -> {
                updateCallback.accept(defaultValue);
                button.setMessage(Component.literal(defaultValue.name()));
            }));
        }

        public EnumToggle(String name, SettingEnum<T> setting, String tooltip) {
            this(name, setting.value(), setting.defaultValue(), setting.values, tooltip, setting::set);
        }
    }

    public static final class ColorPicker extends FlowLayout {
        public Screen previous;

        public ColorPicker(String name, RenderColor currentValue, RenderColor defaultValue, String tooltip, Consumer<RenderColor> updateCallback) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            this.verticalAlignment(VerticalAlignment.CENTER);
            MutableReference<RenderColor> reference = new MutableReference<>(currentValue);
            PlainLabel label = new PlainLabel(Component.literal(name).withColor(0xffffff));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.right(5)).verticalSizing(Sizing.fixed(20));
            label.tooltip(Component.literal(tooltip));
            FlowLayout colorDisplay = UIContainers.verticalFlow(Sizing.fixed(20), Sizing.fixed(20));
            colorDisplay.surface((context, component) ->
                    context.fill(component.x(), component.y(), component.x() + component.width(), component.y() + component.height(), reference.get().getArgb())
            ).margins(Insets.right(5));
            ButtonComponent editButton = UIComponents.button(Component.literal("Edit Color"), _ -> {
                ColorPickerScreen pickerScreen = ColorPickerScreen.build(reference.get(), this.previous, color -> {
                    reference.set(color);
                    updateCallback.accept(color);
                });
                pickerScreen.setTitle(Component.literal(!Utils.toLower(name).endsWith("color") ? name + " Color" : name));
                mc.gui.setScreen(pickerScreen);
            });
            editButton.horizontalSizing(Sizing.fixed(60));
            editButton.renderer(buttonRenderer);
            this.child(label);
            this.child(colorDisplay);
            this.child(editButton);
            this.child(buildResetButton(_ -> {
                reference.set(defaultValue);
                updateCallback.accept(defaultValue);
            }).positioning(Positioning.relative(100, 50)));
        }

        public ColorPicker(String name, SettingColor setting, String tooltip) {
            this(name, setting.value(), RenderColor.fromArgb(setting.getDefault().getAsInt()), tooltip, setting::set);
        }
    }

    public static final class Separator extends FlowLayout {
        public Separator(String name) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.CENTER);
            this.verticalAlignment(VerticalAlignment.CENTER);
            this.verticalSizing(Sizing.fixed(20));
            MutableComponent text = Component.literal(name);
            int textWidth = mc.font.width(text) / 2;
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

    public static final class Description extends FlowLayout {

        public Description(String name, String description) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            this.verticalAlignment(VerticalAlignment.CENTER);
            PlainLabel label = new PlainLabel(Component.literal(name).withColor(0xffffff));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            PlainLabel desc = new PlainLabel(Component.literal(description).withColor(0xffffff));
            desc.verticalTextAlignment(VerticalAlignment.CENTER).verticalSizing(Sizing.content()).horizontalSizing(Sizing.fixed(200));
            this.child(label);
            this.child(desc);
        }
    }

    public static final class TextInput extends FlowLayout {

        public TextInput(String name, String currentValue, String defaultValue, String tooltip, Consumer<String> updateCallback) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            PlainLabel label = new PlainLabel(Component.literal(name).withColor(0xffffff));
            FlatTextbox text = new FlatTextbox(Sizing.fixed(150));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            label.tooltip(Component.literal(tooltip));
            text.onChanged().subscribe(updateCallback::accept);
            text.text(currentValue);
            this.child(label);
            this.child(text);
            this.child(buildResetButton(_ -> {
                updateCallback.accept(defaultValue);
                text.setValue(defaultValue);
            }));
        }

        public TextInput(String name, SettingString setting, String tooltip) {
            this(name, setting.value(), setting.getDefault().getAsString(), tooltip, setting::set);
        }
    }

    public static final class Keybind extends FlowLayout {

        public Keybind(String name, int currentValue, int defaultValue, String tooltip, Consumer<Integer> updateCallback) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5)).horizontalAlignment(HorizontalAlignment.LEFT);
            PlainLabel label = new PlainLabel(Component.literal(name).withColor(0xffffff));
            label.tooltip(Component.literal(tooltip));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            KeybindButton button = new KeybindButton();
            button.bind(currentValue);
            button.onBound().subscribe(updateCallback::accept);
            this.child(label);
            this.child(button);
            this.child(buildResetButton(_ -> {
                updateCallback.accept(defaultValue);
                button.bind(defaultValue);
            }));
        }

        public Keybind(String name, SettingKeybind setting, String tooltip) {
            this(name, setting.key(), setting.getDefault().getAsInt(), tooltip, setting::set);
        }
    }

    public static final class BigButton extends FlowLayout {
        public ButtonComponent button;

        public BigButton(String name, Consumer<ButtonComponent> onPress) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.CENTER);
            this.button = UIComponents.button(Component.literal(name).withColor(0xffffff), onPress);
            this.button.horizontalSizing(Sizing.fixed(290));
            this.button.renderer(buttonRenderer);
            this.child(this.button);
        }
    }

    public static final class DoubleInput extends FlowLayout {

        public DoubleInput(String name, double currentValue, double defaultValue, String tooltip, Consumer<Double> updateCallback) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            PlainLabel label = new PlainLabel(Component.literal(name).withColor(0xffffff));
            FlatTextbox text = new FlatTextbox(Sizing.fixed(150));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            label.tooltip(Component.literal(tooltip));
            text.onChanged().subscribe(change -> Utils.parseDouble(change).ifPresent(updateCallback));
            text.text(String.valueOf(currentValue));
            this.child(label);
            this.child(text);
            this.child(buildResetButton(_ -> {
                updateCallback.accept(defaultValue);
                text.setValue(String.valueOf(defaultValue));
            }));
        }

        public DoubleInput(String name, SettingDouble setting, String tooltip) {
            this(name, setting.value(), setting.getDefault().getAsDouble(), tooltip, setting::set);
        }
    }

    public static final class CustomHeight extends FlowLayout {
        public int heightOverride;

        public CustomHeight(int height) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            this.verticalAlignment(VerticalAlignment.CENTER);
            this.heightOverride = height;
        }
    }
}
