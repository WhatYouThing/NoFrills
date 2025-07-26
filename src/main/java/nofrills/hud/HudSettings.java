package nofrills.hud;

import io.wispforest.owo.ui.container.FlowLayout;
import nofrills.hud.clickgui.Settings;

import java.util.List;

import static nofrills.Main.mc;

public class HudSettings extends Settings {
    public HudSettings(List<FlowLayout> settings) {
        super(settings);
    }

    @Override
    public void close() {
        mc.setScreen(new HudEditorScreen());
    }
}
