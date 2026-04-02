package nofrills.hud.elements;

import nofrills.config.Feature;
import nofrills.hud.TimerElement;

public class PhoenixPetTimer extends TimerElement {

    public PhoenixPetTimer() {
        super("Phoenix Pet: {}", new Feature("phoenixPetTimerElement"), "Phoenix Pet Timer");
        this.options = this.getBaseSettings();
        this.setDesc("Displays the cooldown of the Phoenix Pet Rekindle ability.");
    }
}
