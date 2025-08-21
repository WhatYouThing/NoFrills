package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingDouble;
import nofrills.config.SettingEnum;
import nofrills.features.hunting.ShardTracker;
import nofrills.hud.HudManager;
import nofrills.hud.HudSettings;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.clickgui.Settings;

import java.util.List;

public class ShardTrackerDisplay extends SimpleTextElement {
    public final Feature instance = new Feature("shardTrackerElement");

    public final SettingDouble x = new SettingDouble(0.01, "x", instance.key());
    public final SettingDouble y = new SettingDouble(0.36, "y", instance.key());
    public final SettingBool shadow = new SettingBool(true, "shadow", instance.key());
    public final SettingEnum<alignment> align = new SettingEnum<>(alignment.Left, alignment.class, "align", instance.key());
    public final SettingBool hideIfNone = new SettingBool(false, "hideIfNone", instance.key());


    private final Identifier identifier = Identifier.of("nofrills", "shard-tracker-element");
    private MutableText lastText = ShardTracker.displayNone;

    public ShardTrackerDisplay() {
        super(ShardTracker.displayNone);
        this.options = new HudSettings(List.of(
                new Settings.Toggle("Shadow", shadow, "Adds a shadow to the element's text."),
                new Settings.Dropdown<>("Alignment", align, "The alignment of the element's text."),
                new Settings.Toggle("Hide If None", hideIfNone, "Hides the element if you are not tracking any shards (or the Shard Tracker is disabled).")
        ));
        this.options.setTitle(Text.of("Shard Tracker Element"));
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.focusHandler() == null) {
            return;
        }
        boolean trackerActive = ShardTracker.instance.isActive();
        MutableText text = trackerActive ? ShardTracker.display : ShardTracker.displayNone;
        if (HudManager.isEditingHud()) {
            this.updateSurface(instance.isActive());
        } else {
            if (!instance.isActive()) {
                return;
            }
            if (hideIfNone.value()) {
                if (!trackerActive || text.equals(ShardTracker.displayNone)) {
                    return;
                }
            }
        }
        this.updateShadow(shadow);
        this.updateAlignment(align);
        if (!this.lastText.equals(text)) {
            this.label.text(text);
            this.lastText = text.copy();
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    @Override
    public void toggle() {
        instance.setActive(!instance.isActive());
    }

    @Override
    public void updatePosition() {
        this.updatePosition(x, y);
    }

    @Override
    public void savePosition(double x, double y) {
        this.x.set(x);
        this.y.set(y);
    }

    @Override
    public Identifier getIdentifier() {
        return this.identifier;
    }
}
