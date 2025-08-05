package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingDouble;
import nofrills.config.SettingEnum;
import nofrills.hud.HudManager;
import nofrills.hud.HudSettings;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.Utils;

import java.util.List;

public class FishingBobber extends SimpleTextElement {
    public final Feature instance = new Feature("bobberElement");

    public final SettingDouble x = new SettingDouble(0.01, "x", instance.key());
    public final SettingDouble y = new SettingDouble(0.16, "y", instance.key());
    public final SettingBool shadow = new SettingBool(true, "shadow", instance.key());
    public final SettingEnum<alignment> align = new SettingEnum<>(alignment.Left, alignment.class, "align", instance.key());
    public final SettingBool inactive = new SettingBool(false, "inactive", instance.key());

    private final Identifier identifier = Identifier.of("nofrills", "bobber-element");
    public Entity hologram = null;
    private boolean active = false;

    public FishingBobber(Text text) {
        super(text);
        this.options = new HudSettings(List.of(
                new Settings.Toggle("Shadow", shadow, "Adds a shadow to the element's text."),
                new Settings.Dropdown<>("Alignment", align, "The alignment of the element's text."),
                new Settings.Toggle("Hide If Inactive", inactive, "Hides the element if your fishing bobber is inactive.")
        ));
        this.options.setTitle(Text.of("Bobber Element"));
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (HudManager.isEditingHud()) {
            super.layout.surface(instance.isActive() ? Surface.BLANK : this.disabledSurface);
        } else {
            if (!instance.isActive()) {
                return;
            }
            if (inactive.value() && !this.active) {
                return;
            }
        }
        this.updateShadow(shadow);
        this.updateAlignment(align);
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    public void setActive() {
        this.setText("§cBobber: §aActive");
        this.active = true;
    }

    public void setInactive() {
        this.setText("§cBobber: §7Inactive");
        this.active = false;
    }

    public void setTimer(String timer) {
        if (timer.equals("!!!")) {
            this.setText("§cBobber: §c§lReel!");
        } else {
            this.setText(Utils.format("§cBobber: §e§l{}", timer));
        }
        this.active = true;
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
