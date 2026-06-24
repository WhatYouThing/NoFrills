package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.network.chat.Component;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.TickableHudElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public final class FPS extends SimpleTextElement implements TickableHudElement {
    public final SettingBool average = new SettingBool(false, "average", instance.key());
    private final List<Integer> fpsList = new ArrayList<>();
    private int ticks = 20;

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

    @Override
    public void onClientTick() {
        if (this.ticks > 0) {
            this.ticks -= 1;
            if (this.ticks == 0) {
                this.setFps(mc.getFps());
                this.ticks = 20;
            }
        }
    }

    @Override
    public void onReset() {
        this.ticks = 20;
        this.fpsList.clear();
        this.setText("FPS: §f0");
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
}
