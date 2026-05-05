package nofrills.hud.elements;

import nofrills.config.Feature;
import nofrills.hud.TimerElement;

public final class PickaxeAbilityTimer extends TimerElement {
    private String currentAbility = "";

    public PickaxeAbilityTimer() {
        super("Pickaxe Ability: {}", new Feature("pickaxeAbilityTimerElement"), "Pickaxe Ability Timer");
        this.options = this.getBaseSettings();
        this.setDesc("Displays the duration of your pickaxe ability cooldown.\nUsed by the Ability Alert feature.");
        this.setCategory(Category.Mining);
    }

    @Override
    public String getTimerText() {
        if (!this.currentAbility.isEmpty()) {
            return this.currentAbility + ": {}";
        }
        return super.getTimerText();
    }

    public void setCurrentAbility(String ability) {
        this.currentAbility = ability;
    }
}
