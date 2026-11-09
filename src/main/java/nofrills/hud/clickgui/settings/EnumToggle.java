package nofrills.hud.clickgui.settings;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.network.chat.Component;
import nofrills.config.SettingEnum;
import nofrills.hud.clickgui.SettingsScreen;
import nofrills.hud.clickgui.components.EnumButton;
import nofrills.hud.clickgui.components.PlainLabel;
import nofrills.misc.Utils;

import java.util.Arrays;
import java.util.function.Consumer;

import static nofrills.Main.mc;

public final class EnumToggle<T extends Enum<T>> extends FlowLayout {

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
        this.child(SettingsScreen.buildResetButton(_ -> {
            updateCallback.accept(defaultValue);
            button.setValue(defaultValue);
            button.setMessage(Component.literal(defaultValue.toString()));
        }));
    }

    public EnumToggle(String name, SettingEnum<T> setting, String tooltip) {
        this(name, setting.value(), setting.defaultValue(), setting.values, tooltip, setting::set);
    }
}
