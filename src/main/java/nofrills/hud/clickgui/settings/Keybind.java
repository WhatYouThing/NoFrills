package nofrills.hud.clickgui.settings;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.network.chat.Component;
import nofrills.config.SettingKeybind;
import nofrills.hud.clickgui.SettingsScreen;
import nofrills.hud.clickgui.components.KeybindButton;
import nofrills.hud.clickgui.components.PlainLabel;

import java.util.function.Consumer;

public final class Keybind extends FlowLayout {

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
        this.child(SettingsScreen.buildResetButton(_ -> {
            updateCallback.accept(defaultValue);
            button.bind(defaultValue);
        }));
    }

    public Keybind(String name, SettingKeybind setting, String tooltip) {
        this(name, setting.key(), setting.getDefault().getAsInt(), tooltip, setting::set);
    }
}
