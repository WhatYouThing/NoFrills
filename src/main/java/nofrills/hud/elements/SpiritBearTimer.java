package nofrills.hud.elements;

import nofrills.config.Feature;
import nofrills.hud.TickTimerElement;

public final class SpiritBearTimer extends TickTimerElement {

    public SpiritBearTimer() {
        super("Spirit Bear: {}", new Feature("spiritBearTimerElement"), "Spirit Bear Timer");
        this.setStartTicks(68);
        this.options = this.getBaseSettings();
        this.setDesc("Displays a timer for the Spirit Bear spawning in F4/M4.");
        this.setAutoPause();
        this.setCategory(Category.Dungeons);
    }
}
