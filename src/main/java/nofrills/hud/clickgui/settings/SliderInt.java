package nofrills.hud.clickgui.settings;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.network.chat.Component;
import nofrills.config.SettingInt;
import nofrills.hud.clickgui.SettingsScreen;
import nofrills.hud.clickgui.components.FlatSlider;
import nofrills.hud.clickgui.components.FlatTextbox;
import nofrills.hud.clickgui.components.PlainLabel;
import nofrills.misc.Utils;

import java.util.function.Consumer;

public final class SliderInt extends FlowLayout {

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
        this.child(SettingsScreen.buildResetButton(_ -> {
            updateCallback.accept(defaultValue);
            text.setValue(String.valueOf(defaultValue));
        }));
    }

    public SliderInt(String name, int min, int max, int step, SettingInt setting, String tooltip) {
        this(name, min, max, step, setting.value(), setting.getDefault().getAsInt(), tooltip, setting::set);
    }
}
