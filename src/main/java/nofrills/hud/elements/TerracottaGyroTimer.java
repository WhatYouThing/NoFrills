package nofrills.hud.elements;

import nofrills.config.Feature;
import nofrills.hud.TickTimerElement;

public class TerracottaGyroTimer extends TickTimerElement {

    public TerracottaGyroTimer() {
        super("Gyro: {}", new Feature("terracottaGyroTimerElement"), "Terracotta Gyro Timer");
        this.options = this.getBaseSettings();
        this.setDesc("Displays a timer for the first and the ultimate giant gyro in F6/M6.\nUsed by the Terracotta Timer feature.");
        this.setAutoPause();
    }
}
