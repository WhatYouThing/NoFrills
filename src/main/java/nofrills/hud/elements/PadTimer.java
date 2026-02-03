package nofrills.hud.elements;

import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.hud.TickTimerElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.Utils;

import java.util.List;

public class PadTimer extends TickTimerElement {
    public SettingBool totalTime;
    private int totalTicks = 0;

    public PadTimer() {
        super("Pad: §f{}", new Feature("padTimerElement"), "Pad Timer Element");
        this.totalTime = new SettingBool(true, "totalTime", this.instance);
        this.setStartTicks(20);
        this.setRepeating(true);
        this.options = this.getBaseSettings(List.of(
                new Settings.Toggle("Total Time", this.totalTime, "Displays the total time spent in the Storm phase.")
        ));
        this.setDesc("Displays a tick timer for the pads in the F7/M7 Storm phase.");
    }

    @Override
    public void updateTimer() {
        String timer = Utils.format(this.timerText, this.ticksAsTime(this.ticks));
        if (this.totalTime.value()) {
            timer += " §7(" + this.ticksAsTime(this.totalTicks) + ")";
        }
        this.setText(timer);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.ticks > 0) {
            this.totalTicks++;
        }
    }

    @Override
    public void pause() {
        super.pause();
        this.totalTicks = 0;
    }
}
