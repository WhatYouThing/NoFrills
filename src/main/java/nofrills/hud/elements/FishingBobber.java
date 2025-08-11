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

import java.text.DecimalFormat;
import java.util.List;

public class FishingBobber extends SimpleTextElement {
    public final Feature instance = new Feature("bobberElement");

    public final SettingDouble x = new SettingDouble(0.01, "x", instance.key());
    public final SettingDouble y = new SettingDouble(0.16, "y", instance.key());
    public final SettingBool shadow = new SettingBool(true, "shadow", instance.key());
    public final SettingEnum<alignment> align = new SettingEnum<>(alignment.Left, alignment.class, "align", instance.key());
    public final SettingBool inactive = new SettingBool(false, "inactive", instance.key());
    public final SettingBool timer = new SettingBool(false, "timer", instance.key());

    private final Identifier identifier = Identifier.of("nofrills", "bobber-element");
    private final DecimalFormat format = new DecimalFormat("0.0");
    public Entity hologram = null;
    public int timerTicks = 0;
    public boolean active = false;
    public String currentText = "§cBobber: §7Inactive";

    public FishingBobber(Text text) {
        super(text);
        this.options = new HudSettings(List.of(
                new Settings.Toggle("Shadow", shadow, "Adds a shadow to the element's text."),
                new Settings.Dropdown<>("Alignment", align, "The alignment of the element's text."),
                new Settings.Toggle("Hide If Inactive", inactive, "Hides the element if your fishing bobber is inactive."),
                new Settings.Toggle("Bobber Timer", timer, "Tracks how long your fishing bobber has existed for, useful for Slugfish.")
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
        if (timer.value()) {
            this.setText(Utils.format("{} §7[{}s]", this.currentText, format.format(this.timerTicks / 20.0)));
        } else {
            this.setText(this.currentText);
        }
        this.updateShadow(shadow);
        this.updateAlignment(align);
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    private void updateText(String text) {
        this.currentText = text;
    }

    public void setActive() {
        this.updateText("§cBobber: §aActive");
        this.active = true;
    }

    public void setInactive() {
        this.updateText("§cBobber: §7Inactive");
        this.timerTicks = 0;
        this.active = false;
    }

    public void setTimer(String timer) {
        if (timer.equals("!!!")) {
            this.updateText("§cBobber: §c§lReel!");
        } else {
            this.updateText(Utils.format("§cBobber: §e§l{}", timer));
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
