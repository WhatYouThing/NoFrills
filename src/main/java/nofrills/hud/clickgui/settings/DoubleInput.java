package nofrills.hud.clickgui.settings;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.network.chat.Component;
import nofrills.config.SettingDouble;
import nofrills.hud.clickgui.SettingsScreen;
import nofrills.hud.clickgui.components.FlatTextbox;
import nofrills.hud.clickgui.components.PlainLabel;
import nofrills.misc.Utils;

import java.util.function.Consumer;

public final class DoubleInput extends FlowLayout {

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
        this.child(SettingsScreen.buildResetButton(_ -> {
            updateCallback.accept(defaultValue);
            text.setValue(String.valueOf(defaultValue));
        }));
    }

    public DoubleInput(String name, SettingDouble setting, String tooltip) {
        this(name, setting.value(), setting.getDefault().getAsDouble(), tooltip, setting::set);
    }
}
