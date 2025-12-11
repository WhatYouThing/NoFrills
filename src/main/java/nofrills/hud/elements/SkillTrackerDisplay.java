package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import nofrills.config.Feature;
import nofrills.features.general.SkillTracker;
import nofrills.hud.SimpleTextElement;

public class SkillTrackerDisplay extends SimpleTextElement {

    public SkillTrackerDisplay() {
        super(SkillTracker.getText(), new Feature("skillTrackerElement"), "Skill Tracker Display");
        this.options = this.getBaseSettings();
        this.setDesc("Displays information about tracked attribute shards. Used by the Shard Tracker feature.");
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.shouldRender()) {
            super.draw(context, mouseX, mouseY, partialTicks, delta);
        }
    }
}
