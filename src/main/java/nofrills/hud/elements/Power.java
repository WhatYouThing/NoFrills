package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Surface;
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

public class Power extends SimpleTextElement {
    public final Feature instance = new Feature("powerElement");

    public final SettingDouble x;
    public final SettingDouble y;
    public final SettingBool shadow = new SettingBool(true, "shadow", instance.key());
    public final SettingEnum<alignment> align = new SettingEnum<>(alignment.Left, alignment.class, "align", instance.key());
    public final SettingBool dungeon = new SettingBool(true, "dungeon", instance.key());

    private final Identifier identifier = Identifier.of("nofrills", "power-element");

    public Power(String text, double x, double y) {
        super(Text.literal(text));
        this.x = new SettingDouble(x, "x", instance.key());
        this.y = new SettingDouble(y, "y", instance.key());
        this.options = new HudSettings(List.of(
                new Settings.Toggle("Shadow", shadow, "Adds a shadow to the element's text."),
                new Settings.Dropdown<>("Alignment", align, "The alignment of the element's text."),
                new Settings.Toggle("Dungeons Only", dungeon, "Hides the element outside of Dungeons.")
        ));
        this.options.setTitle(Text.of("Power Element"));
        HudManager.addNew(this);
    }

    public Power(String text) {
        this(text, HudManager.getDefaultX(), HudManager.getDefaultY());
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (HudManager.isEditingHud()) {
            super.layout.surface(instance.isActive() ? Surface.BLANK : this.disabledSurface);
        } else {
            if (!instance.isActive()) {
                return;
            }
            if (dungeon.value() && !Utils.isInDungeons()) {
                return;
            }
        }
        this.updateShadow(shadow);
        this.updateAlignment(align);
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    public void setPower(double power) {
        this.setText(Utils.format("§bPower: §f{}", power));
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
