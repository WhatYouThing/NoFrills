package nofrills.hud;

import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.Utils;

import java.util.List;

import static nofrills.Main.mc;

public class InnerSettingsScreen extends Settings {
    public final Screen previous;

    public InnerSettingsScreen(List<FlowLayout> settings, Screen previous) {
        super(settings);
        this.previous = previous;
    }

    public InnerSettingsScreen(List<FlowLayout> settings, Component title, Screen previous) {
        this(settings, previous);
        this.setTitle(title);
    }

    public static void rebuild(List<FlowLayout> settings) {
        if (mc.screen instanceof InnerSettingsScreen inner) {
            Utils.setScreen(new InnerSettingsScreen(settings, inner.title, inner.previous));
        }
    }

    @Override
    public void onClose() {
        mc.setScreen(this.previous);
    }
}
