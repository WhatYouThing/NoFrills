package nofrills.hud.elements;

import nofrills.config.Feature;
import nofrills.hud.TimerElement;

public final class SpiritMaskTimer extends TimerElement {

    public SpiritMaskTimer() {
        super("Spirit Mask: {}", new Feature("spiritMaskTimerElement"), "Spirit Mask Timer");
        this.options = this.getBaseSettings();
        this.setDesc("Displays the cooldown of the Spirit Mask Second Wind ability.");
        this.setAutoPause();
        this.setCategory(Category.Dungeons);
    }
}
