package nofrills.hud.elements;

import nofrills.config.Feature;
import nofrills.hud.TickTimerElement;

public final class FreshToolsTimer extends TickTimerElement {

    public FreshToolsTimer() {
        super("Fresh Tools: {}", new Feature("freshToolsTimerElement"), "Fresh Tools Timer");
        this.setStartTicks(200);
        this.options = this.getBaseSettings();
        this.setDesc("Displays a timer for the Fresh Tools perk shop ability in Kuudra.\nUsed by the Fresh Timer feature.");
        this.setAutoPause();
        this.setCategory(Category.Kuudra);
    }
}
