package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
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


public class Day extends SimpleTextElement {
    public final Feature instance = new Feature("dayElement");

    public final SettingDouble x;
    public final SettingDouble y;
    public final SettingBool shadow = new SettingBool(true, "shadow", instance.key());
    public final SettingEnum<alignment> align = new SettingEnum<>(alignment.Left, alignment.class, "align", instance.key());

    private final Identifier identifier = Identifier.of("nofrills", "day-element");

    public Day(String text, double x, double y) {
        super(Text.literal(text));
        this.x = new SettingDouble(x, "x", instance.key());
        this.y = new SettingDouble(y, "y", instance.key());
        this.options = new HudSettings(List.of(
                new Settings.Toggle("Shadow", shadow, "Adds a shadow to the element's text."),
                new Settings.Dropdown<>("Alignment", align, "The alignment of the element's text.")
        ));
        this.options.setTitle(Text.of("Day Element"));
        HudManager.addNew(this);
    }

    public Day(String text) {
        this(text, HudManager.getDefaultX(), HudManager.getDefaultY());
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (HudManager.isEditingHud()) {
            this.updateSurface(instance.isActive());
        } else if (!instance.isActive()) {
            return;
        }
        this.updateShadow(shadow);
        this.updateAlignment(align);
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    public void setDay(long day) {
        this.setText(Utils.format("§bDay: §f{}", day));
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
