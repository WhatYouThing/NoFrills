package nofrills.hud.elements;

import nofrills.config.Feature;
import nofrills.hud.TimerElement;

public final class BonzoMaskTimer extends TimerElement {

    public BonzoMaskTimer() {
        super("Bonzo Mask: {}", new Feature("bonzoMaskTimerElement"), "Bonzo Mask Timer");
        this.options = this.getBaseSettings();
        this.setDesc("Displays the cooldown of the Bonzo's Mask Clownin' Around ability.");
        this.setAutoPause();
        this.setCategory(Category.Dungeons);
    }
}
