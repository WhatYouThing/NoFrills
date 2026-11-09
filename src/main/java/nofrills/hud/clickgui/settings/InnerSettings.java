package nofrills.hud.clickgui.settings;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import nofrills.hud.InnerSettingsScreen;
import nofrills.hud.clickgui.SettingsScreen;
import nofrills.hud.clickgui.components.PlainLabel;
import nofrills.misc.Utils;

import java.util.List;
import java.util.function.Supplier;

public class InnerSettings extends FlowLayout {
    public Screen previousScreen = null;

    public InnerSettings(Sizing horizontalSizing, Sizing verticalSizing, Algorithm algorithm) {
        super(horizontalSizing, verticalSizing, algorithm);
    }

    public InnerSettings(String name, Supplier<List<FlowLayout>> settings, String buttonText, String tooltip) {
        this(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
        this.padding(Insets.of(5));
        this.horizontalAlignment(HorizontalAlignment.LEFT);
        this.verticalAlignment(VerticalAlignment.CENTER);
        PlainLabel label = new PlainLabel(Component.literal(name).withColor(0xffffff));
        label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.right(5)).verticalSizing(Sizing.fixed(20));
        label.tooltip(Component.literal(tooltip));
        ButtonComponent editButton = UIComponents.button(Component.literal(buttonText), _ -> {
            InnerSettingsScreen inner = new InnerSettingsScreen(settings.get(), this.previousScreen);
            inner.setTitle(Component.literal(name));
            Utils.setScreen(inner);
        });
        editButton.horizontalSizing(Sizing.fixed(60));
        editButton.renderer(SettingsScreen.buttonRenderer);
        this.child(label);
        this.child(editButton);
    }

    public InnerSettings(String name, List<FlowLayout> settings, String buttonText, String tooltip) {
        this(name, () -> settings, buttonText, tooltip);
    }
}
