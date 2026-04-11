package nofrills.hud.elements;

import nofrills.config.Feature;
import nofrills.hud.TimerElement;

public class PickaxeAbilityTimer extends TimerElement {

    public PickaxeAbilityTimer() {
        super("Pickaxe Ability: {}", new Feature("pickaxeAbilityTimerElement"), "Pickaxe Ability Timer");
        this.options = this.getBaseSettings();
        this.setDesc("Displays the duration of your pickaxe ability cooldown.\nUsed by the Ability Alert feature.");
        this.setCategory(Category.Mining);
    }
}
