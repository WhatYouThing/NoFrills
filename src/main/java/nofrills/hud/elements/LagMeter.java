package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import nofrills.config.*;
import nofrills.hud.HudManager;
import nofrills.hud.HudSettings;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.Utils;

import java.util.List;

public class LagMeter extends SimpleTextElement {
    public final Feature instance = new Feature("lagMeterElement");

    public final SettingDouble x;
    public final SettingDouble y;
    public final SettingBool shadow = new SettingBool(true, "shadow", instance.key());
    public final SettingEnum<alignment> align = new SettingEnum<>(alignment.Left, alignment.class, "align", instance.key());
    public final SettingInt min = new SettingInt(500, "min", instance.key());

    private final Identifier identifier = Identifier.of("nofrills", "lag-meter-element");

    public long lastTick = 0;

    public LagMeter(String text, double x, double y) {
        super(Text.literal(text));
        this.x = new SettingDouble(x, "x", instance.key());
        this.y = new SettingDouble(y, "y", instance.key());
        this.options = new HudSettings(List.of(
                new Settings.Toggle("Shadow", shadow, "Adds a shadow to the element's text."),
                new Settings.Dropdown<>("Alignment", align, "The alignment of the element's text."),
                new Settings.SliderInt("Minimum Time", 0, 5000, 50, min, "The minimum amount of time (in milliseconds) since the last tick for the element to be visible.")
        ));
        this.options.setTitle(Text.of("Lag Meter Element"));
        HudManager.addNew(this);
    }

    public LagMeter(String text) {
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
            if (lastTick != 0) {
                long sinceTick = Util.getMeasuringTimeMs() - lastTick;
                if (sinceTick >= min.value()) {
                    this.setText(Utils.format("§cLast server tick was {}s ago", Utils.formatDecimal(sinceTick * 0.001)));
                } else {
                    return;
                }
            } else {
                return;
            }
        }
        this.updateShadow(shadow);
        this.updateAlignment(align);
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    public void setTickTime(long time) {
        this.lastTick = time;
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
