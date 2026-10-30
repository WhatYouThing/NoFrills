package nofrills.hud.elements;

import nofrills.config.Feature;
import nofrills.hud.TickTimerElement;

public final class CustomTitleContainer extends TickTimerElement {

    public CustomTitleContainer() {
        super("Custom Title", new Feature("customTitleContainer"), "Custom Title Container");
        this.options = this.getBaseSettings();
        this.setDesc("Replaces non-vanilla title rendering with itself while active.");
        this.setCategory(Category.Misc);
        this.setClientTick();
    }

    @Override
    public void updateTimer() {
        this.setText(this.text);
    }
}
