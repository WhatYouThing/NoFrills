package nofrills.hud.elements;

import nofrills.config.Feature;
import nofrills.hud.TickTimerElement;

public class GoldorTickTimer extends TickTimerElement {

    public GoldorTickTimer() {
        super("Goldor Tick: §f{}", new Feature("goldorTickTimerElement"), "Goldor Tick Timer Element");
        this.setStartTicks(60);
        this.setRepeating(true);
        this.options = this.getBaseSettings();
        this.setDesc("Displays a tick timer for the death tick in the F7/M7 Goldor phase.");
    }
}
