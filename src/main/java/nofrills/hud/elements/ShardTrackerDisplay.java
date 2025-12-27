package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.features.hunting.ShardTracker;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.clickgui.Settings;

import java.util.List;

public class ShardTrackerDisplay extends SimpleTextElement {
    public final SettingBool hideIfNone = new SettingBool(false, "hideIfNone", instance);

    public ShardTrackerDisplay() {
        super(ShardTracker.displayNone, new Feature("shardTrackerElement"), "Shard Tracker Display");
        this.options = this.getBaseSettings(List.of(
                new Settings.Toggle("Hide If None", hideIfNone, "Hides the element if you are not tracking any shards (or the Shard Tracker is disabled).")
        ));
        this.setDesc("Displays information about tracked attribute shards. Used by the Shard Tracker feature.");
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender()) {
            return;
        } else if (!this.isEditingHud()) {
            if (hideIfNone.value() && (!ShardTracker.instance.isActive() || this.label.text().equals(ShardTracker.displayNone))) {
                return;
            }
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }
}
