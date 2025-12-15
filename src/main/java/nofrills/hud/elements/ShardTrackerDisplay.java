package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.features.hunting.ShardTracker;
import nofrills.hud.HudSettings;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.clickgui.Settings;

import java.util.List;

public class ShardTrackerDisplay extends SimpleTextElement {
    public final SettingBool hideIfNone = new SettingBool(false, "hideIfNone", instance);

    public ShardTrackerDisplay() {
        super(ShardTracker.displayNone, new Feature("shardTrackerElement"), "Shard Tracker Display");
        this.options = new HudSettings(List.of(
                new Settings.Toggle("Shadow", this.textShadow, "Adds a shadow to the element's text."),
                new Settings.Dropdown<>("Alignment", this.textAlignment, "The alignment of the element's text."),
                new Settings.Toggle("Hide If None", hideIfNone, "Hides the element if you are not tracking any shards (or the Shard Tracker is disabled).")
        ));
        this.options.setTitle(this.elementLabel);
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
