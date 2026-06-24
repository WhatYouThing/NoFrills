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

public final class TPS extends SimpleTextElement implements TickableHudElement {
    public final SettingBool average = new SettingBool(false, "average", instance.key());
    private final List<Integer> tpsList = new ArrayList<>();
    private int clientTicks = 20;
    private int serverTicks = 0;

    public TPS(String text) {
        super(Component.literal(text), new Feature("tpsElement"), "TPS Display");
        this.options = this.getBaseSettings(List.of(
                new Settings.Toggle("Average", average, "Tracks and adds the average TPS to the element.")
        ));
        this.setDesc("Displays the real time TPS of the server, and optionally the average TPS.");
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
        if (this.clientTicks > 0) {
            this.clientTicks -= 1;
            if (this.clientTicks == 0) {
                this.setTps(this.serverTicks);
                this.clientTicks = 20;
                this.serverTicks = 0;
            }
        }
    }

    @Override
    public void onServerTick() {
        this.serverTicks += 1;
    }

    @Override
    public void onReset() {
        this.clientTicks = 20;
        this.serverTicks = 0;
        this.tpsList.clear();
        this.setText("TPS: §f0");
    }

    public void setTps(int tps) {
        if (average.value()) {
            if (this.tpsList.size() > 30) {
                this.tpsList.removeFirst();
            }
            this.tpsList.add(Math.clamp(tps, 0, 20));
            int avg = 0;
            for (int previous : this.tpsList) {
                avg += previous;
            }
            this.setText(Utils.format("TPS: §f{} §7{}", tps, Utils.formatDecimal(avg / (double) tpsList.size())));
        } else {
            this.setText(Utils.format("TPS: §f{}", tps));
        }
    }
}
