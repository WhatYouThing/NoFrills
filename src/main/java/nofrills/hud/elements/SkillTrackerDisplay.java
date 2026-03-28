package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import nofrills.config.Feature;
import nofrills.features.general.SkillTracker;
import nofrills.hud.SimpleTextElement;

public class SkillTrackerDisplay extends SimpleTextElement {

    public SkillTrackerDisplay() {
        super(SkillTracker.getText(), new Feature("skillTrackerElement"), "Skill Tracker Display");
        this.options = this.getBaseSettings();
        this.setDesc("Displays information about your skill EXP gain. Used by the Skill Tracker feature.");
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender()) {
            return;
        } else if (!this.isEditingHud() && !SkillTracker.instance.isActive()) {
            return;
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    public void tick() {
        if (SkillTracker.instance.isActive()) {
            this.setText(SkillTracker.getText());
        }
    }
}
