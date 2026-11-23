package nofrills.hud.clickgui.settings;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.network.chat.Component;
import nofrills.hud.clickgui.SettingsScreen;

import java.util.function.Consumer;

public final class BigButton extends FlowLayout {
    public ButtonComponent button;

    public BigButton(String name, Consumer<ButtonComponent> onPress) {
        super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
        this.padding(Insets.of(5));
        this.horizontalAlignment(HorizontalAlignment.CENTER);
        this.button = UIComponents.button(Component.literal(name).withColor(0xffffff), onPress);
        this.button.horizontalSizing(Sizing.fixed(290));
        this.button.renderer(SettingsScreen.buttonRenderer);
        this.child(this.button);
    }
}
