package nofrills.hud.clickgui;

import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import nofrills.config.*;
import nofrills.hud.clickgui.components.*;
import nofrills.misc.RenderColor;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static nofrills.Main.mc;

public class Settings extends BaseOwoScreen<FlowLayout> {
    public List<FlowLayout> settings;
    public Text title = Text.empty();
    public ScrollContainer<FlowLayout> scroll;

    public Settings(List<FlowLayout> settings) {
        this.settings = settings;
    }

    private static ButtonComponent buildResetButton(Consumer<ButtonComponent> onPress) {
        ButtonComponent button = Components.button(Text.literal("Reset").withColor(0xffffff), onPress);
        button.positioning(Positioning.relative(100, 0));
        button.renderer((context, btn, delta) -> {
            context.fill(btn.getX(), btn.getY(), btn.getX() + btn.getWidth(), btn.getY() + btn.getHeight(), 0xff101010);
            context.drawBorder(btn.getX(), btn.getY(), btn.getWidth(), btn.getHeight(), 0xffffffff);
        });
        return button;
    }

    private static double roundDouble(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }

    private static int getSettingsHeight(List<Component> children) {
        int height = 0;
        for (Component child : children) {
            if (child instanceof ColorPicker picker) {
                if (picker.sliderList.size() == 4) {
                    height += 90;
                } else {
                    height += 70;
                }
                continue;
            }
            if (child instanceof Description description) {
                PlainLabel label = (PlainLabel) description.children().getLast();
                height += (10 + label.getTextHeight());
                continue;
            }
            height += 30;
        }
        return (int) Math.clamp(height, 30, mc.getWindow().getScaledHeight() * 0.8);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (FlowLayout setting : this.settings) {
            for (Component child : setting.children()) {
                if (child instanceof KeybindButton keybind) {
                    if (keybind.isBinding) {
                        keybind.bind(keyCode);
                        return true;
                    }
                }
            }
        }
        if (keyCode == GLFW.GLFW_KEY_PAGE_UP || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            this.scroll.onMouseScroll(0, 0, keyCode == GLFW.GLFW_KEY_PAGE_UP ? 4 : -4);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.scroll.onMouseScroll(0, 0, verticalAmount * 2);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (FlowLayout setting : this.settings) {
            for (Component child : setting.children()) {
                if (child instanceof KeybindButton keybind) {
                    if (keybind.isBinding) {
                        keybind.bind(button);
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
        Color textColor = Color.ofArgb(0xffffffff);
        FlowLayout settings = Containers.verticalFlow(Sizing.content(), Sizing.content());
        settings.surface(Surface.flat(0xaa000000)).alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        int width = 300;
        List<FlowLayout> optionsMutable = new ArrayList<>(this.settings);
        for (FlowLayout option : optionsMutable) {
            option.horizontalSizing(Sizing.fixed(width));
            settings.child(option);
        }
        this.scroll = Containers.verticalScroll(Sizing.content(), Sizing.fixed(getSettingsHeight(settings.children())), settings)
                .scrollbarThiccness(5)
                .scrollbar(ScrollContainer.Scrollbar.flat(textColor));
        BaseComponent label = new PlainLabel(this.title)
                .color(textColor)
                .horizontalTextAlignment(HorizontalAlignment.CENTER)
                .verticalTextAlignment(VerticalAlignment.CENTER);
        ParentComponent header = Containers.verticalFlow(Sizing.fixed(width), Sizing.content())
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
        public boolean active = false;
        public SettingBool setting;
        public Text enabledText;
        public Text disabledText;
        public ButtonComponent toggle;

        public Toggle(String name, SettingBool setting, String tooltip) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            this.setting = setting;
            this.enabledText = Text.literal("Enabled").withColor(0x55ff55);
            this.disabledText = Text.literal("Disabled").withColor(0xff5555);
            PlainLabel label = new PlainLabel(Text.literal(name).withColor(0xffffff));
            label.tooltip(Text.literal(tooltip));
            this.toggle = Components.button(Text.empty(), button -> {
                boolean value = this.setting.value();
                active(!value);
            });
            this.toggle.renderer((context, button, delta) -> {
                context.fill(button.getX(), button.getY(), button.getX() + button.getWidth(), button.getY() + button.getHeight(), 0xff101010);
                context.drawBorder(button.getX(), button.getY(), button.getWidth(), button.getHeight(), 0xff5ca0bf);
            });
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            this.active(this.setting.value());
            this.child(label);
            this.child(this.toggle);
            this.child(buildResetButton(btn -> {
                this.setting.reset();
                this.active(this.setting.value());
            }));
        }

        private void active(boolean active) {
            if (active) {
                this.toggle.setMessage(this.enabledText);
            } else {
                this.toggle.setMessage(this.disabledText);
            }
            this.setting.set(active);
            this.active = active;
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
                try {
                    double value = Double.parseDouble(text.getText());
                    this.setting.set(value);
                    slider.value(value);
                } catch (NumberFormatException ignored) {
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
                try {
                    int value = Integer.parseInt(text.getText());
                    this.setting.set(value);
                    slider.value(value);
                } catch (NumberFormatException ignored) {
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
                ButtonComponent button = Components.button(Text.of(value.name()), btn -> {
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
        public List<FlatSlider> sliderList = new ArrayList<>();

        public ColorPicker(String name, boolean alpha, SettingColor setting, String tooltip) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            this.verticalAlignment(VerticalAlignment.CENTER);
            this.setting = setting;
            PlainLabel label = new PlainLabel(Text.literal(name).withColor(0xffffff));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.right(5)).verticalSizing(Sizing.fixed(20));
            label.tooltip(Text.literal(tooltip));
            FlowLayout colorDisplay = Containers.verticalFlow(Sizing.fixed(20), Sizing.fixed(20));
            colorDisplay.surface(Surface.flat(this.setting.value().argb)).margins(Insets.right(10));
            FlowLayout colorSliders = Containers.verticalFlow(Sizing.content(), Sizing.content());
            for (int i = 0; i <= 3; i++) {
                if (i == 3 && !alpha) {
                    continue;
                }
                int id = i;
                FlowLayout row = Containers.horizontalFlow(Sizing.content(), Sizing.content());
                PlainLabel colorLabel = new PlainLabel(Text.literal(getColorLabel(id)).withColor(0xffffff));
                colorLabel.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.right(5)).verticalSizing(Sizing.fixed(20));
                FlatTextbox text = new FlatTextbox(Sizing.fixed(30));
                FlatSlider slider = new FlatSlider(0xffdddddd, 0xff5ca0bf);
                slider.min(0).max(255).stepSize(1).horizontalSizing(Sizing.fixed(60)).verticalSizing(Sizing.fixed(20));
                sliderList.add(slider);
                text.onChanged().subscribe(change -> {
                    try {
                        int value = Integer.parseInt(text.getText());
                        setColorValue(id, value / 255.0f);
                        slider.value(value);
                        colorDisplay.surface(Surface.flat(this.setting.value().argb));
                    } catch (NumberFormatException ignored) {
                    }
                });
                text.text(String.valueOf((int) (getColorValue(id) * 255.0f)));
                slider.onChanged().subscribe(change -> {
                    int value = (int) slider.value();
                    setColorValue(id, value / 255.0f);
                    text.setText(String.valueOf(value));
                    colorDisplay.surface(Surface.flat(this.setting.value().argb));
                });
                row.child(colorLabel);
                row.child(text);
                row.child(slider);
                colorSliders.child(row);
            }
            this.child(label);
            this.child(colorDisplay);
            this.child(colorSliders);
            this.child(buildResetButton(btn -> {
                this.setting.reset();
                for (int i = 0; i <= 3; i++) {
                    if (i == 3 && !alpha) {
                        continue;
                    }
                    sliderList.get(i).value((int) (getColorValue(i) * 255.0f));
                }
            }).positioning(Positioning.relative(100, 50)));
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
            RenderColor color = this.setting.value();
            return switch (id) {
                case 0 -> color.r;
                case 1 -> color.g;
                case 2 -> color.b;
                case 3 -> color.a;
                default -> 0;
            };
        }

        private void setColorValue(int id, double value) {
            RenderColor color = this.setting.value();
            RenderColor newColor = switch (id) { // quite scuffed but its either this or pasting the same code 4 times to build the color picker
                case 0 -> RenderColor.fromFloat((float) value, color.g, color.b, color.a);
                case 1 -> RenderColor.fromFloat(color.r, (float) value, color.b, color.a);
                case 2 -> RenderColor.fromFloat(color.r, color.g, (float) value, color.a);
                case 3 -> RenderColor.fromFloat(color.r, color.g, color.b, (float) value);
                default -> color;
            };
            this.setting.set(newColor);
        }
    }

    public static class Separator extends FlowLayout {
        public Separator(String name) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.CENTER);
            this.verticalAlignment(VerticalAlignment.CENTER);
            this.verticalSizing(Sizing.fixed(30));
            MutableText text = Text.literal(name);
            int textWidth = mc.textRenderer.getWidth(text) / 2;
            PlainLabel label = new PlainLabel(text.withColor(0xffffff));
            label.verticalTextAlignment(VerticalAlignment.CENTER).verticalSizing(Sizing.fixed(30));
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
            this.button = Components.button(Text.literal(name).withColor(0xffffff), onPress);
            this.button.horizontalSizing(Sizing.fixed(290));
            this.button.renderer((context, btn, delta) -> {
                context.fill(btn.getX(), btn.getY(), btn.getX() + btn.getWidth(), btn.getY() + btn.getHeight(), 0xff101010);
                context.drawBorder(btn.getX(), btn.getY(), btn.getWidth(), btn.getHeight(), 0xff5ca0bf);
            });
            this.child(this.button);
        }
    }
}
