package nofrills.hud.clickgui.settings;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.network.chat.Component;
import nofrills.config.SettingBool;
import nofrills.hud.clickgui.SettingsScreen;
import nofrills.hud.clickgui.components.PlainLabel;
import nofrills.hud.clickgui.components.ToggleButton;

import java.util.function.Consumer;

public final class Toggle extends FlowLayout {

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
        this.child(SettingsScreen.buildResetButton(_ -> {
            updateCallback.accept(defaultValue);
            toggle.setToggle(defaultValue);
        }));
    }

    public Toggle(String name, SettingBool setting, String tooltip) {
        this(name, setting.value(), setting.getDefault().getAsBoolean(), tooltip, setting::set);
    }
}
