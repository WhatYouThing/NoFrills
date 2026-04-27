package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.network.chat.Component;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;

public class FPS extends SimpleTextElement {
    public final SettingBool average = new SettingBool(false, "average", instance.key());
    public int ticks = 20;
    public List<Integer> fpsList = new ArrayList<>();

    public FPS(String text) {
        super(Component.literal(text), new Feature("fpsElement"), "FPS Display");
        this.options = this.getBaseSettings(List.of(
                new Settings.Toggle("Average", average, "Tracks and adds the average FPS to the element.")
        ));
        this.setDesc("Displays your FPS.");
        this.setCategory(Category.Info);
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.shouldRender()) {
            super.draw(context, mouseX, mouseY, partialTicks, delta);
        }
    }

    public void setFps(int fps) {
        if (average.value()) {
            if (this.fpsList.size() > 30) {
                this.fpsList.removeFirst();
            }
            this.fpsList.add(fps);
            int avg = 0;
            for (int previous : this.fpsList) {
                avg += previous;
            }
            this.setText(Utils.format("FPS: §f{} §7{}", fps, avg / fpsList.size()));
        } else {
            this.setText(Utils.format("FPS: §f{}", fps));
        }
    }

    public void reset() {
        this.ticks = 20;
        this.fpsList.clear();
        this.setText("FPS: §f0");
    }
}
