package nofrills.hud;

import io.wispforest.owo.ui.container.FlowLayout;
import nofrills.hud.clickgui.SettingsScreen;

import java.util.List;

import static nofrills.Main.mc;

public class HudSettingsScreen extends SettingsScreen {
    public HudSettingsScreen(List<FlowLayout> settings) {
        super(settings);
    }

    @Override
    public void onClose() {
        mc.gui.setScreen(new HudEditorScreen());
    }
}
