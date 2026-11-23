package nofrills.hud.clickgui.settings;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.network.chat.Component;
import nofrills.config.SettingDouble;
import nofrills.hud.clickgui.SettingsScreen;
import nofrills.hud.clickgui.components.FlatSlider;
import nofrills.hud.clickgui.components.FlatTextbox;
import nofrills.hud.clickgui.components.PlainLabel;
import nofrills.misc.Utils;

import java.util.function.Consumer;

public final class SliderDouble extends FlowLayout {
    public final FlatSlider slider;

    public SliderDouble(String name, double min, double max, double step, double currentValue, double defaultValue, String tooltip, Consumer<Double> updateCallback) {
        super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
        this.padding(Insets.of(5));
        this.horizontalAlignment(HorizontalAlignment.LEFT);
        PlainLabel label = new PlainLabel(Component.literal(name).withColor(0xffffff));
        FlatTextbox text = new FlatTextbox(Sizing.fixed(50));
        this.slider = new FlatSlider(0xffdddddd, 0xff5ca0bf);
        this.slider.min(min).max(max).stepSize(step).value(currentValue).horizontalSizing(Sizing.fixed(100)).verticalSizing(Sizing.fixed(20));
        label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
        label.tooltip(Component.literal(tooltip));
        text.onChanged().subscribe(change -> {
            Utils.parseDouble(change).ifPresent(d -> {
                double value = Utils.roundDecimal(d);
                updateCallback.accept(value);
                this.slider.value(value);
            });
        });
        text.text(String.valueOf(currentValue));
        slider.onChanged().subscribe(change -> {
            double value = Utils.roundDecimal(change);
            updateCallback.accept(value);
            text.setValue(String.valueOf(value));
        });
        this.child(label);
        this.child(text);
        this.child(slider);
        this.child(SettingsScreen.buildResetButton(btn -> {
            updateCallback.accept(defaultValue);
            text.setValue(String.valueOf(Utils.roundDecimal(defaultValue)));
        }));
    }

    public SliderDouble(String name, double min, double max, double step, SettingDouble setting, String tooltip) {
        this(name, min, max, step, setting.value(), setting.getDefault().getAsDouble(), tooltip, setting::set);
    }
}
