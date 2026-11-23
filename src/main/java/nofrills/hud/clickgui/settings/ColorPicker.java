package nofrills.hud.clickgui.settings;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import net.minecraft.network.chat.Component;
import nofrills.config.SettingColor;
import nofrills.hud.ColorPickerScreen;
import nofrills.hud.clickgui.SettingsScreen;
import nofrills.hud.clickgui.components.PlainLabel;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static nofrills.Main.mc;

public final class ColorPicker extends InnerSettings {

    public ColorPicker(String name, RenderColor currentValue, RenderColor defaultValue, String tooltip, Consumer<RenderColor> updateCallback) {
        super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
        this.padding(Insets.of(5));
        this.horizontalAlignment(HorizontalAlignment.LEFT);
        this.verticalAlignment(VerticalAlignment.CENTER);
        AtomicReference<RenderColor> reference = new AtomicReference<>(currentValue);
        PlainLabel label = new PlainLabel(Component.literal(name).withColor(0xffffff));
        label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.right(5)).verticalSizing(Sizing.fixed(20));
        label.tooltip(Component.literal(tooltip));
        FlowLayout colorDisplay = UIContainers.verticalFlow(Sizing.fixed(20), Sizing.fixed(20));
        colorDisplay.surface((context, component) ->
                context.fill(component.x(), component.y(), component.x() + component.width(), component.y() + component.height(), reference.get().argb())
        ).margins(Insets.right(5));
        ButtonComponent editButton = UIComponents.button(Component.literal("Edit Color"), _ -> {
            ColorPickerScreen pickerScreen = ColorPickerScreen.build(reference.get(), this.previousScreen, color -> {
                reference.set(color);
                updateCallback.accept(color);
            });
            pickerScreen.setTitle(Component.literal(!Utils.toLower(name).endsWith("color") ? name + " Color" : name));
            mc.gui.setScreen(pickerScreen);
        });
        editButton.horizontalSizing(Sizing.fixed(60));
        editButton.renderer(SettingsScreen.buttonRenderer);
        this.child(label);
        this.child(colorDisplay);
        this.child(editButton);
        this.child(SettingsScreen.buildResetButton(_ -> {
            reference.set(defaultValue);
            updateCallback.accept(defaultValue);
        }).positioning(Positioning.relative(100, 50)));
    }

    public ColorPicker(String name, SettingColor setting, String tooltip) {
        this(name, setting.value(), RenderColor.fromArgb(setting.getDefault().getAsInt()), tooltip, setting::set);
    }
}
