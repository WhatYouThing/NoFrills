package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.Utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Ping extends SimpleTextElement {
    public final SettingBool average = new SettingBool(false, "average", instance.key());
    public int ticks = 20;
    public long lastPing = 0;
    public List<Long> pingList = new CopyOnWriteArrayList<>();

    public Ping(String text) {
        super(Text.literal(text), new Feature("pingElement"), "Ping Element");
        this.options = this.getBaseSettings(List.of(
                new Settings.Toggle("Average", average, "Tracks and adds your average ping to the element.")
        ));
        this.setDesc("Displays your ping.");
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.shouldRender()) {
            if (average.value() && !this.pingList.isEmpty()) {
                long avg = 0;
                for (long previous : this.pingList) {
                    avg += previous;
                }
                this.setText(Utils.format("Ping: §f{}ms §7{}ms", this.lastPing, avg / this.pingList.size()));
            } else {
                this.setText(Utils.format("Ping: §f{}ms", this.lastPing));
            }
            super.draw(context, mouseX, mouseY, partialTicks, delta);
        }
    }

    public void setPing(long ping) {
        if (this.pingList.size() > 30) {
            this.pingList.removeFirst();
        }
        this.lastPing = ping;
        this.pingList.add(ping);
    }

    public void reset() {
        this.ticks = 20;
        this.lastPing = 0;
        this.pingList.clear();
    }
}
