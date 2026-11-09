package nofrills.hud.clickgui.settings;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.network.chat.Component;
import nofrills.config.SettingString;
import nofrills.hud.clickgui.SettingsScreen;
import nofrills.hud.clickgui.components.FlatTextbox;
import nofrills.hud.clickgui.components.PlainLabel;

import java.util.function.Consumer;

public final class TextInput extends FlowLayout {

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
        this.child(SettingsScreen.buildResetButton(_ -> {
            updateCallback.accept(defaultValue);
            text.setValue(defaultValue);
        }));
    }

    public TextInput(String name, SettingString setting, String tooltip) {
        this(name, setting.value(), setting.getDefault().getAsString(), tooltip, setting::set);
    }
}
